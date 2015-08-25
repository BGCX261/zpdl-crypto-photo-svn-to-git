package com.zpdl.encryptionphoto.crypto;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.security.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import com.zpdl.api.file.AFileScanner;
import com.zpdl.api.file.AFileUtil;
import com.zpdl.api.service.ARunnable;
import com.zpdl.api.util.AByteUtils;
import com.zpdl.api.util.Alog;
import com.zpdl.encryptionphoto.HpConfigure;
import com.zpdl.encryptionphoto.cache.TnParam;

public class DecryptoARunnable extends ARunnable {
    public static final int RESULT_ERR_DECRYPTO      = 0x0002;

    private AFileScanner mAFileScanner;

    private boolean running;

    private String[] mIn;
    private int      mInCnt;

    private String[] mOut;

    private CryptoListener mListener;

    private int mProgressCnt;
    private int mProgressPer;

    public DecryptoARunnable(String[] in) {
        super();

        mAFileScanner = null;

        running = true;
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

    public static TnParam decryptoThumbnail(String encryptedPath)
            throws CryptoExceptionCertification, CryptoExceptionError {
        Key key = CryptoKey.getInstance().getKey();
        Cipher c = Crypto.getCipher(Cipher.DECRYPT_MODE, key);
        if(c == null) {
            throw new CryptoExceptionError("getCipher error");
        }

        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(encryptedPath));
            String password = _decryptoPassword(c, bis);
            if(password == null) {
                throw new CryptoExceptionError("decryptoThumbnail password is null");
            } else if(!password.equals(CryptoKey.getInstance().getPassword())) {
                c = Crypto.getCipher(Cipher.DECRYPT_MODE, CryptoKey.getInstance().generateKey(password));
                if(c == null) {
                    throw new CryptoExceptionError("generateKey - getCipher error");
                }
            }

            String path = _decryptoOriginalPath(c, bis);

            TnParam decryptedTn = _decryptoThumbnail(c, bis);
            if(decryptedTn == null) {
                throw new CryptoExceptionError("decryptoThumbnail password is null");
            }

            decryptedTn.path = path;
            return decryptedTn;
        } catch (FileNotFoundException e) {
            throw new CryptoExceptionError(e);
        } finally {
            try {
                if(bis != null) bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void aRun() {
        mAFileScanner = new AFileScanner(mContext);

        int result = Crypto.RESULT_OK;

        Key key = CryptoKey.getInstance().getKey();
        Cipher c = Crypto.getCipher(Cipher.DECRYPT_MODE, key);

        String out = null;
        try {
            for(int i = 0; i < mIn.length; i++) {
                _progressCount(i);
                String in = mIn[i];

                out = _decrypto(c, in);
                mOut[i] = out;
                try {
                    mAFileScanner.insertFile(out);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                _delete(in);
                mAFileScanner.deleteFile(in);
            }
        } catch (CryptoExceptionCancel e) {
            e.printStackTrace();
            result = Crypto.RESULT_CANCEL;
        } catch (CryptoExceptionError e) {
            e.printStackTrace();
            result = RESULT_ERR_DECRYPTO;
        } catch (CryptoExceptionCertification e) {
            e.printStackTrace();
            result = RESULT_ERR_DECRYPTO;
        } finally {
            if(mAFileScanner != null) {
                mAFileScanner.release();
                mAFileScanner = null;
            }
            _complete(result);
        }
    }

    private String _decrypto(Cipher cipher, String in) throws CryptoExceptionCancel, CryptoExceptionError, CryptoExceptionCertification {
        BufferedInputStream bis = null;
        Cipher c = cipher;
        try {
            bis = new BufferedInputStream(new FileInputStream(in));
            String password = _decryptoPassword(c, bis);
            if(password == null) {
                throw new CryptoExceptionError("decryptoThumbnail password is null");
            } else if(!password.equals(CryptoKey.getInstance().getPassword())) {
                c = Crypto.getCipher(Cipher.DECRYPT_MODE, CryptoKey.getInstance().generateKey(password));
                if(c == null) {
                    throw new CryptoExceptionError("generateKey - getCipher error");
                }
            }

            String out = _decryptoOriginalPath(c, bis);
            if(out == null) {
                throw new CryptoExceptionError("path is null");
            }

            _skipThumbnail(c, bis);

            return _decryptoPhoto(c, bis, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new CryptoExceptionError("FileNotFoundException error : in = "+in);
        } finally {
            try {
                if(bis != null) bis.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new CryptoExceptionError("IOException close error : in = "+in);
            }
        }
    }

    private static String _decryptoPassword(Cipher c, BufferedInputStream bis) throws CryptoExceptionCertification {
        try {
            byte[] readBuf = new byte[Crypto.CERTIFICATION_LENGTH];
            bis.read(readBuf);

            int encbufLength = readBuf[0];
            byte[] decryptoBuf = c.doFinal(readBuf, 1, encbufLength);

            String decryptoString = new String(decryptoBuf);
            String certification = decryptoString.substring(0, Crypto.CERTIFICATION.length());
            if(!Crypto.CERTIFICATION.equals(certification)) {
                throw new CryptoExceptionCertification("Distict Certification code : "+certification);
            }

            String password = decryptoString.substring(Crypto.CERTIFICATION.length(), decryptoString.length());

            return password;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String _decryptoOriginalPath(Cipher c, BufferedInputStream bis) {
        byte[] readBuf = null;
        byte[] intBuf = new byte[AByteUtils.INT_SIZE];

        String resultPath = null;

        try {
            bis.read(intBuf);
            readBuf = new byte[AByteUtils.toInt(intBuf)];
            bis.read(readBuf);

            resultPath = new String(readBuf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultPath;
    }

    private static TnParam _decryptoThumbnail(Cipher c, BufferedInputStream bis) {
        TnParam decyptedTn = new TnParam();

        byte[] encryptoBuf = null;
        byte[] decryptoBuf = null;
        byte[] intBuf = new byte[AByteUtils.INT_SIZE];

        try {
            bis.read(intBuf); // width
            int width = AByteUtils.toInt(intBuf);
            bis.read(intBuf); // height
            int height = AByteUtils.toInt(intBuf);
            bis.read(intBuf); // orientation
            decyptedTn.orientation = AByteUtils.toInt(intBuf);
            Alog.d("decryptoThumbnail width = %d height = %d orientation = %d", width, height, decyptedTn.orientation);

            /* Config */
            bis.read(intBuf);
            int configSize = AByteUtils.toInt(intBuf);

            decryptoBuf = new byte[configSize];
            bis.read(decryptoBuf);
            Field f = Config.class.getDeclaredField(new String(decryptoBuf));
            Config config = (Config) f.get(Config.class);

            Alog.d("decryptoThumbnail config = "+config);

            /* Thumbnail */
            bis.read(intBuf);
            encryptoBuf = new byte[AByteUtils.toInt(intBuf)];
            bis.read(encryptoBuf);
            decryptoBuf = c.doFinal(encryptoBuf);

            ByteBuffer byteBuffer = ByteBuffer.wrap(decryptoBuf);
            byteBuffer.rewind();

            decyptedTn.thumbnail  = Bitmap.createBitmap(width, height, config);
            decyptedTn.thumbnail.copyPixelsFromBuffer(byteBuffer);

            return decyptedTn;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void _skipThumbnail(Cipher c, BufferedInputStream bis) {
        byte[] intBuf = new byte[AByteUtils.INT_SIZE];

        try {
            bis.skip(AByteUtils.INT_SIZE * 3); // width, height, orientation

            /* Config */
            bis.read(intBuf);
            bis.skip(AByteUtils.toInt(intBuf));

            /* Thumbnail */
            bis.read(intBuf);
            bis.skip(AByteUtils.toInt(intBuf));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String _decryptoPhoto(Cipher c, BufferedInputStream bis, String outPath) throws CryptoExceptionError, CryptoExceptionCancel {
        String out = outPath;
        if(!AFileUtil.getInstance().isSdCard(out)) {
            out = String.format("%s%s%s", HpConfigure.WORKING_DIRECTORY, File.separator, new File(out).getName());
        }
        if(AFileUtil.getInstance().createDirectory(new File(out).getParent()) < 0) {
            throw new CryptoExceptionError("is not create out directory , "+new File(out).getParent());
        }
        out = AFileUtil.getInstance().distinctFileString(out);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(out);
            int tsize = bis.available();

            int read = 0;
            float tread = 0;
            int progress = 0;

            byte[] inBuf = new byte[8096];
            byte[] outBuf = null;

            while ((read = bis.read(inBuf)) != -1) {
                tread += read;
                outBuf = c.update(inBuf, 0, read);
                if (outBuf != null) {
                    fos.write(outBuf);
                }
                int nprogress = (int) (tread / tsize * 89);
                if(nprogress > progress) {
                    progress = nprogress;
                    _progressPercent(10 + progress);
                }
                if(!_stopCheck()) {
                    _delete(out);
                    throw new CryptoExceptionCancel("DecryptoRunnable : Stop");
                }
            }
            outBuf = c.doFinal();
            if (outBuf != null) {
                fos.write(outBuf);
            }
            if(!_stopCheck()) {
                _delete(out);
                throw new CryptoExceptionCancel("DecryptoRunnable : Stop");
            }
            _progressPercent(100);

            return out;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            _delete(out);
        } catch (IOException e) {
            e.printStackTrace();
            _delete(out);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
            _delete(out);
        } catch (BadPaddingException e) {
            e.printStackTrace();
            _delete(out);
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new CryptoExceptionError("_decryptoPhoto : FileOutputStream close error");
            }
        }
        return null;
    }

    private void _delete(String path) {
        if(path != null) {
            File file = new File(path);
            if(file.exists()) {
                file.delete();
            }
        }
    }

    private boolean _stopCheck() {
        return running;
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
//    FileInputStream fis = null;
//    try {
//        fis = new FileInputStream(inPath);
//    } catch (FileNotFoundException e) {
//        Alog.w("DecryptoRunnable FileInputStream : FileNotFoundException");
//        e.printStackTrace();
//        if(mListener != null) mListener.onDecryptoComplete(false);
//        return;
//    }
//
//    Cipher c = Crypto.getCipher(Cipher.DECRYPT_MODE, key);
//    CipherInputStream cis = new CipherInputStream(fis, c);
//
//    try {
//        /* Certification */
//        byte[] readCertificationByte = new byte[Crypto.CERTIFICATION.length];
//        cis.read(readCertificationByte);
//        Alog.d("DecryptoRunnable readCertificationByte = %s",new String(readCertificationByte));
//
//        byte[] readIntByte = new byte[AByteUtils.INT_SIZE];
//        cis.read(readIntByte);
//        Alog.d("DecryptoRunnable readIntByte = %d",AByteUtils.toInt(readIntByte));
//
//        cis.close();
//    } catch (IOException e) {
//        Alog.w("DecryptoRunnable CipherInputStream : IOException");
//        e.printStackTrace();
//        if(mListener != null) mListener.onDecryptoComplete(false);
//        return;
//    }
}