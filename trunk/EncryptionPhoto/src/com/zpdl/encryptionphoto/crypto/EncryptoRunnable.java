package com.zpdl.encryptionphoto.crypto;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;

import com.zpdl.api.drawable.AbitmapFactory;
import com.zpdl.api.util.AByteUtils;
import com.zpdl.api.util.Alog;

public class EncryptoRunnable implements Runnable {
    private boolean running;

    private String inPath;
    private String outPath;
    private ContentResolver mContentResolver;
    private CryptoListener mListener;

    public EncryptoRunnable(String in, String out, ContentResolver cr, CryptoListener listener) {
        running = true;

        inPath = in;
        outPath = out;
        mContentResolver = cr;
        mListener = listener;
    }

    public void stop() throws CryptoExceptionCancel {
        running = false;
    }

    @Override
    public void run() {
        Key key = CryptoKey.getInstance().getKey();
        Cipher c = Crypto.getCipher(Cipher.ENCRYPT_MODE, key);

        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(outPath));

            if(!encryptoCertification(c, bos)) {
                _error("encryptoCertification");
                return;
            }

            if(!encryptoThumbnail(c, bos)) {
                _error("encryptoThumbnail");
                return;
            }

            if(!encryptoPhoto(c, bos)) {
                _error("encryptoPhoto");
                return;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (CryptoExceptionCancel e) {
            ;
        } finally {
            try {
                if(bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        _complete();
    }

    private boolean encryptoCertification(Cipher c, BufferedOutputStream bos) throws CryptoExceptionCancel {
        try {
            byte[] outBuf = new byte[Crypto.CERTIFICATION_LENGTH];
            byte[] encBuf = c.doFinal((Crypto.CERTIFICATION+CryptoKey.getInstance().getPassword()).getBytes());
            if(encBuf == null) {
                Alog.e("encryptoCertification : encrypto buffer is null");
                return false;
            } else if(encBuf.length > outBuf.length - 1) {
                Alog.e("encryptoCertification : out of range encrypyo buffer enc = %d out = %d", encBuf.length, outBuf.length);
                return false;
            }
            outBuf[0] = (byte) encBuf.length;
            System.arraycopy(encBuf, 0, outBuf, 1, encBuf.length);
            bos.write(outBuf);

            _progress(2);
            _stopCheck();
            return true;
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean encryptoThumbnail(Cipher c, BufferedOutputStream bos) throws CryptoExceptionCancel {
        byte[] outBuf = null;

        try {
            /* Original Path */
            outBuf = inPath.getBytes();
            bos.write(AByteUtils.toBytes(outBuf.length));
            bos.write(outBuf);
            _progress(3);

            /* Thumb */
            String[] imageColumns = {MediaStore.Images.Media._ID,
                                     MediaStore.Images.Media.ORIENTATION};
            String where = MediaStore.Images.Media.DATA + "=?";
            String whereArgs[] = {inPath};

            Cursor cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                    imageColumns,
                                                    where,
                                                    whereArgs,
                                                    null);

            if(!cursor.moveToFirst()) {
                Alog.e("encryptoThumbnail image cursor is empty , path = "+inPath);
                return false;
            }

            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
            int orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));

            Bitmap thumbnail = AbitmapFactory.ScaledBitmap(Images.Thumbnails.getThumbnail(mContentResolver,
                                                                                          id,
                                                                                          Images.Thumbnails.MINI_KIND,
                                                                                          null),
                                                           AbitmapFactory.ScaleType.CENTER_CROP);
            _stopCheck();

            bos.write(AByteUtils.toBytes(thumbnail.getWidth()));
            bos.write(AByteUtils.toBytes(thumbnail.getHeight()));
            bos.write(AByteUtils.toBytes(orientation));
            _progress(4);

            outBuf = thumbnail.getConfig().toString().getBytes();
            bos.write(AByteUtils.toBytes(outBuf.length));
            bos.write(outBuf);
            _progress(5);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(thumbnail.getByteCount());
            byteBuffer.rewind();
            thumbnail.copyPixelsToBuffer(byteBuffer);
            _progress(7);

            outBuf = c.doFinal(byteBuffer.array());
            bos.write(AByteUtils.toBytes(outBuf.length));
            bos.write(outBuf);
            byteBuffer.clear();
            _progress(10);

            _stopCheck();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean encryptoPhoto(Cipher c, BufferedOutputStream bos) throws CryptoExceptionCancel {
        BufferedInputStream input = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(inPath);
            int tsize = fis.available();

            input = new BufferedInputStream(fis);

            int read = 0;
            float tread = 0;
            int progress = 0;

            byte[] inBuf = new byte[1024];
            byte[] outBuf = null;

            while ((read = input.read(inBuf)) != -1) {
                tread += read;
                outBuf = c.update(inBuf, 0, read);
                if (outBuf != null) {
                    bos.write(outBuf);
                }
                int nprogress = (int) (tread / tsize * 89);
                if(nprogress > progress) {
                    progress = nprogress;
                    _progress(10 + progress);
                }
                _stopCheck();
                Alog.d("encryptoPhoto read = %d tread = %f", read, tread);
            }
            outBuf = c.doFinal();
            if (outBuf != null) {
                bos.write(outBuf);
            }
            _stopCheck();
            _progress(100);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } finally {
            try {
                if(input != null) input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private void _stopCheck() throws CryptoExceptionCancel {
        if(!running) {
            throw new CryptoExceptionCancel("EncryptoRunnable : Stop");
        }
    }

    private void _progress(int i) {
        Alog.d("encryptoPhoto _progress = %d", i);
    }

    private void _complete() {
//      if(mListener != null) mListener.onEncryptoComplete();
    }

    private void _error(String error) {
        Alog.e("EncryptoRunnable error : %s", error);
//        if(mListener != null) mListener.onEncryptoError();
    }

//    FileOutputStream fos = null;
//    try {
//        fos = new FileOutputStream(outPath);
//    } catch (FileNotFoundException e) {
//        Alog.w("EncryptoRunnable FileOutputStream : FileNotFoundException");
//        e.printStackTrace();
//        if(mListener != null) mListener.onEncryptoComplete(false);
//        return;
//    }
//
//    Cipher c = Crypto.getCipher(Cipher.ENCRYPT_MODE, key);
//    CipherOutputStream cos = new CipherOutputStream(fos, c);
//
//    try {
//        /* Certification */
//        cos.write(Crypto.CERTIFICATION);
//        cos.write(AByteUtils.toBytes(27));
//
//        cos.close();
//    } catch (IOException e) {
//        Alog.w("EncryptoRunnable CipherOutputStream : IOException");
//        e.printStackTrace();
//    }
}