package com.zpdl.encryptionphoto.crypto;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import android.graphics.Bitmap.Config;

import com.zpdl.api.util.AByteUtils;
import com.zpdl.api.util.Alog;

public class DecryptoRunnable implements Runnable {
    String inPath;

    public DecryptoRunnable(String in) {
        inPath = in;
    }

    @Override
    public void run() {
        Key key = CryptoKey.getInstance().getKey();
        Cipher c = Crypto.getCipher(Cipher.DECRYPT_MODE, key);

        BufferedInputStream bis = null;
        byte[] encryptoBuf = null;
        byte[] decryptoBuf = null;
        byte[] intBuf = new byte[AByteUtils.INT_SIZE];

        try {
            bis = new BufferedInputStream(new FileInputStream(inPath));
            String password = getPasswordFromCertification(c, bis);
            if(password == null) {
                Alog.e("DecryptoRunnable : getPasswordFromCertification fail");
                return;
            }

            decryptoThumbnail(c, bis);
            Alog.d("DecryptoRunnable intBuf = %d",AByteUtils.toInt(intBuf));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(bis != null) bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private String getPasswordFromCertification(Cipher c, BufferedInputStream bis) {
        byte[] encryptoBuf = null;
        byte[] decryptoBuf = null;
        byte[] intBuf = new byte[AByteUtils.INT_SIZE];

        try {
            bis.read(intBuf);
            encryptoBuf = new byte[AByteUtils.toInt(intBuf)];
            bis.read(encryptoBuf);
            decryptoBuf = c.doFinal(encryptoBuf);

            String decryptoString = new String(decryptoBuf);
            String certification = decryptoString.substring(0, Crypto.CERTIFICATION.length());
            if(!Crypto.CERTIFICATION.equals(certification)) {
                Alog.e("getPasswordFromCertification CERTIFICATION is different. %s",certification);
                return null;
            }

            String password = decryptoString.substring(Crypto.CERTIFICATION.length(), decryptoString.length());
            Alog.d("getPasswordFromCertification decryptoString = %s", decryptoString);
            Alog.d("getPasswordFromCertification certification = %s", certification);
            Alog.d("getPasswordFromCertification password = %s", password);

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

    private void decryptoThumbnail(Cipher c, BufferedInputStream bis) {
        byte[] encryptoBuf = null;
        byte[] decryptoBuf = null;
        byte[] intBuf = new byte[AByteUtils.INT_SIZE];

        try {
            bis.read(intBuf); // total size
            Alog.d("decryptoThumbnail thhumbail bloack size = %d",AByteUtils.toInt(intBuf));

            bis.read(intBuf); // path
            decryptoBuf = new byte[AByteUtils.toInt(intBuf)];
            bis.read(decryptoBuf);
            Alog.d("decryptoThumbnail path = %s",new String(decryptoBuf));

            bis.read(intBuf); // width
            int width = AByteUtils.toInt(intBuf);
            bis.read(intBuf); // height
            int height = AByteUtils.toInt(intBuf);
            bis.read(intBuf); // orientation
            int orientation = AByteUtils.toInt(intBuf);
            Alog.d("decryptoThumbnail width = %d height = %d orientation = %d", width, height, orientation);

            Config config = null;
            try {
                bis.read(intBuf);
                int configSize = AByteUtils.toInt(intBuf);

                decryptoBuf = new byte[configSize];
                bis.read(decryptoBuf);
                Field f = Config.class.getDeclaredField(new String(decryptoBuf));
                config = (Config) f.get(Config.class);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            Alog.d("decryptoThumbnail config = "+config);

        } catch (IOException e) {
            e.printStackTrace();
        }
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