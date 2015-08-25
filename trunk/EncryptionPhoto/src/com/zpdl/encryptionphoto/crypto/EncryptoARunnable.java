package com.zpdl.encryptionphoto.crypto;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
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
import com.zpdl.api.file.AFileScanner;
import com.zpdl.api.file.AFileUtil;
import com.zpdl.api.service.ARunnable;
import com.zpdl.api.util.AByteUtils;
import com.zpdl.api.util.Alog;

public class EncryptoARunnable extends ARunnable {
    public static final int RESULT_ERR_ENCRYPTO      = 0x0002;

    private AFileScanner mAFileScanner;

    private boolean running;
    private boolean mInDelete;

    private String[] mIn;
    private int      mInCnt;

    private String[] mOut;

    private CryptoListener mListener;

    private int mProgressCnt;
    private int mProgressPer;

    public EncryptoARunnable(String[] in) {
        super();

        mAFileScanner = null;

        running = true;
        mInDelete = false;

        mListener = null;

        mIn = in;
        mInCnt = mIn.length;

        mOut = new String[mInCnt];
        for(int i = 0; i < mOut.length; i++) {
            mOut[i] = null;
        }

        mProgressCnt = 0;
        mProgressPer = 0;
    }

    public synchronized void setCryptoListener(CryptoListener l) {
        mListener = l;
    }

    public void setIsInFileDelete(boolean b) {
        mInDelete = b;
    }

    public void cancel() {
        running = false;
    }

    public int getProgressCount() {
        return mProgressCnt;
    }

    public int getProgressPercent() {
        return mProgressPer;
    }

    public String getProgressFileString() {
        return mIn[mProgressCnt];
    }

    @Override
    protected void aRun() {
        mAFileScanner = new AFileScanner(mContext);

        int result = Crypto.RESULT_OK;

        if(AFileUtil.getInstance().createDirectory(Crypto.WORKING_DIRECTORY) < 0) {
            _complete(result);
            return;
        }
        Key key = CryptoKey.getInstance().getKey();
        Cipher c = Crypto.getCipher(Cipher.ENCRYPT_MODE, key);

        String out = null;
        try {
            for(int i = 0; i < mIn.length; i++) {
                _progressCount(i);
                String in = mIn[i];
                out = String.format("%s%s%d.%s", Crypto.WORKING_DIRECTORY, File.separator, System.currentTimeMillis(), Crypto.WORKING_EXTENTION);
                out = AFileUtil.getInstance().distinctFileString(out);

                _encrypto(c, in, out);
                mOut[i] = out;
                try {
                    mAFileScanner.insertFile(out);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(mInDelete) {
                    _delete(in);
                    mAFileScanner.deleteFile(in);
                }
            }
        } catch (CryptoExceptionCancel e) {
            e.printStackTrace();
            result = Crypto.RESULT_CANCEL;
            _delete(out);
        } catch (CryptoExceptionError e) {
            e.printStackTrace();
            result = RESULT_ERR_ENCRYPTO;
            _delete(out);
        } finally {
            if(mAFileScanner != null) {
                mAFileScanner.release();
                mAFileScanner = null;
            }
            _complete(result);
        }
    }

    private void _encrypto(Cipher c, String in, String out) throws CryptoExceptionCancel, CryptoExceptionError {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(out));

            if(!encryptoCertification(c, bos)) {
                throw new CryptoExceptionError("encryptoCertification error : in = "+in);
            }

            if(!encryptoThumbnail(c, in, bos)) {
                throw new CryptoExceptionError("encryptoThumbnail error : in = "+in);
            }

            if(!encryptoPhoto(c, in, bos)) {
                throw new CryptoExceptionError("encryptoPhoto error : in = "+in);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new CryptoExceptionError("FileNotFoundException error : in = "+in);
        } finally {
            try {
                if(bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new CryptoExceptionError("IOException close error : in = "+in);
            }
        }
    }

    private boolean encryptoCertification(Cipher c, BufferedOutputStream bos) throws CryptoExceptionCancel {
        try {
            byte[] outBuf = new byte[Crypto.CERTIFICATION_LENGTH];
            byte[] encBuf = c.doFinal((Crypto.CERTIFICATION+CryptoKey.getInstance().getPassword()).getBytes());

            if(encBuf == null) {
                return false;
            } else if(encBuf.length > outBuf.length - 1) {
                return false;
            }
            outBuf[0] = (byte) encBuf.length;
            System.arraycopy(encBuf, 0, outBuf, 1, encBuf.length);
            bos.write(outBuf);

            _progressPercent(2);
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

    private boolean encryptoThumbnail(Cipher c, String in, BufferedOutputStream bos) throws CryptoExceptionCancel {
        byte[] outBuf = null;
        ContentResolver cr = mContext.getContentResolver();

        try {
            /* Original Path */
            outBuf = in.getBytes();
            bos.write(AByteUtils.toBytes(outBuf.length));
            bos.write(outBuf);
            _progressPercent(3);

            /* Thumb */
            String[] imageColumns = {MediaStore.Images.Media._ID,
                                     MediaStore.Images.Media.ORIENTATION};
            String where = MediaStore.Images.Media.DATA + "=?";
            String whereArgs[] = {in};

            Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                    imageColumns,
                                                    where,
                                                    whereArgs,
                                                    null);

            if(!cursor.moveToFirst()) {
                Alog.e("encryptoThumbnail image cursor is empty , path = "+in);
                return false;
            }

            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
            int orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));

            Bitmap thumbnail = AbitmapFactory.ScaledBitmap(Images.Thumbnails.getThumbnail(cr,
                                                                                          id,
                                                                                          Images.Thumbnails.MINI_KIND,
                                                                                          null),
                                                           AbitmapFactory.ScaleType.CENTER_CROP);
            _stopCheck();

            bos.write(AByteUtils.toBytes(thumbnail.getWidth()));
            bos.write(AByteUtils.toBytes(thumbnail.getHeight()));
            bos.write(AByteUtils.toBytes(orientation));
            _progressPercent(4);

            outBuf = thumbnail.getConfig().toString().getBytes();
            bos.write(AByteUtils.toBytes(outBuf.length));
            bos.write(outBuf);
            _progressPercent(5);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(thumbnail.getByteCount());
            byteBuffer.rewind();
            thumbnail.copyPixelsToBuffer(byteBuffer);
            _progressPercent(7);

            outBuf = c.doFinal(byteBuffer.array());
            bos.write(AByteUtils.toBytes(outBuf.length));
            bos.write(outBuf);
            byteBuffer.clear();
            _progressPercent(10);

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

    private boolean encryptoPhoto(Cipher c, String in, BufferedOutputStream bos) throws CryptoExceptionCancel {
        BufferedInputStream input = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(in);
            int tsize = fis.available();

            input = new BufferedInputStream(fis);

            int read = 0;
            float tread = 0;
            int progress = 0;

            byte[] inBuf = new byte[8096];
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
                    _progressPercent(10 + progress);
                }
                _stopCheck();
//                Alog.d("encryptoPhoto read = %d tread = %f", read, tread);
            }
            outBuf = c.doFinal();
            if (outBuf != null) {
                bos.write(outBuf);
            }
            _stopCheck();
            _progressPercent(100);

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

    private void _delete(String path) {
        File file = new File(path);
        if(file.exists()) {
            file.delete();
        }
    }

    private void _stopCheck() throws CryptoExceptionCancel {
        if(!running) {
            throw new CryptoExceptionCancel("EncryptoRunnable : Stop");
        }
    }

    private synchronized void _progressCount(int cnt) {
        mProgressCnt = cnt;
        mProgressPer = 0;
        if(mListener != null) mListener.onCryptoProgress(mIn[mProgressCnt], mProgressCnt, mProgressPer);
    }

    private synchronized void _progressPercent(int per) {
        mProgressPer = per;
        if(mListener != null) mListener.onCryptoProgress(null, mProgressCnt, mProgressPer);
    }

    private synchronized void _complete(int result) {
        if(mListener != null) mListener.onCryptoComplete(result, mIn, mOut);
    }
}