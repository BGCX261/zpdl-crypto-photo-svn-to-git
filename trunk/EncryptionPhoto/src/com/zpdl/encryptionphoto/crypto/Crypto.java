package com.zpdl.encryptionphoto.crypto;

import java.io.File;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import com.zpdl.api.util.Alog;
import com.zpdl.encryptionphoto.HpConfigure;

public class Crypto {
    public static final String WORKING_DIRECTORY = HpConfigure.WORKING_DIRECTORY + File.separator + "crypto";
    public static final String WORKING_EXTENTION = "txt";

    public static final String CERTIFICATION = "HP";
    public static final int    CERTIFICATION_LENGTH = 255;

    public static final String ALGORITM = "AES";
    public static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    public static final int RESULT_OK       = 0x0000;
    public static final int RESULT_CANCEL   = 0x0001;

    public static Cipher getCipher(int opmode, Key key) {
        Cipher c = null;
        try {
            c = Cipher.getInstance(TRANSFORMATION);
            c.init(opmode, key);
        } catch (NoSuchAlgorithmException e) {
            Alog.w("Crypto / getCipher : NoSuchAlgorithmException");
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            Alog.w("Crypto / getCipher : NoSuchPaddingException");
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            Alog.w("Crypto / getCipher : InvalidKeyException");
            e.printStackTrace();
        }

        return c;
    }

    public static EncryptoARunnable getEncryptoRunnable(String[] in) {
        return new EncryptoARunnable(in);
    }

    public static DecryptoARunnable getDecryptoARunnable(String[] in) {
        return new DecryptoARunnable(in);
    }
}
