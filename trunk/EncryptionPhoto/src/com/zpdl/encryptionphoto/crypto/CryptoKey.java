package com.zpdl.encryptionphoto.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.zpdl.api.file.AFileUtil;
import com.zpdl.api.util.AByteUtils;
import com.zpdl.api.util.Alog;

public class CryptoKey {
    private static final String PASSWORD_FILE_PATH = Crypto.WORKING_DIRECTORY + File.separator + ".key" + File.separator +"key.txt";

    private static final String DEFAULT_KEY = "Hidden photo";
    private static final String ALGORITM = Crypto.ALGORITM;

    private volatile static CryptoKey uniqueInstance;

    public static CryptoKey getInstance() {
        if(uniqueInstance == null) {
            synchronized (CryptoKey.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new CryptoKey();
                }
            }
        }

        return uniqueInstance;
    }

    private String mPassword;
    private Key mKey;

    private CryptoKey() {
        mPassword = null;
        mKey = null;
    }

    public Key generateKey(String password) {
        return _generateKey(password);
    }

    public synchronized Key getKey() {
        String password = getPassword();

        if(password != null && mKey == null) {
            mKey = _generateKey(password);
        }

        return mKey;
    }

    public synchronized void setPassword(String password) {
        mPassword = password;
        mKey = null;

        File passFile = new File(PASSWORD_FILE_PATH);
        AFileUtil.getInstance().createDirectory(passFile.getParent());

        if(passFile != null) {
            passFile.delete();
        }

        Alog.d("CryptoKey / setPassword : password byte size = %d", mPassword.getBytes().length);
        Alog.d("CryptoKey / setPassword : password = %s", mPassword);
        try {
            Cipher c = Crypto.getCipher(Cipher.ENCRYPT_MODE, _generateKey(DEFAULT_KEY));

            CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(passFile), c);

            cos.write(AByteUtils.toBytes(mPassword.getBytes().length));
            cos.write(mPassword.getBytes());

            cos.close();
        } catch (FileNotFoundException e) {
            Alog.w("CryptoKey / setPassword : FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            Alog.w("CryptoKey / setPassword : IOException");
            e.printStackTrace();
        }

//        String str = "1234567890KKH";
//
//        Cipher c = Crypto.getCipher(Cipher.ENCRYPT_MODE, _generateKey(DEFAULT_KEY));
//        byte[] plain = str.getBytes();
//
//        try {
//            encrypt = c.doFinal(plain);
//        } catch (IllegalBlockSizeException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (BadPaddingException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        Alog.i("cipher : plain = %s %s", new String(plain), String.valueOf(plain));
//        Alog.i("cipher : encrypt = %s", String.valueOf(encrypt));

//        mPassword = null; // Temp
    }

    public synchronized String getPassword() {
        if(mPassword == null) {
            File passFile = new File(PASSWORD_FILE_PATH);
            if(passFile.exists()) {
                try {
                    Cipher c = Crypto.getCipher(Cipher.DECRYPT_MODE, _generateKey(DEFAULT_KEY));

                    CipherInputStream cis = new CipherInputStream(new FileInputStream(passFile), c);

                    byte[] readIntByte = new byte[AByteUtils.INT_SIZE];
                    cis.read(readIntByte);
                    int passSize = AByteUtils.toInt(readIntByte);

                    byte[] passStringByte = new byte[passSize];
                    cis.read(passStringByte);
                    String passString = new String(passStringByte);

                    cis.close();

                    mPassword = passString;

                    Alog.d("CryptoKey / getPassword : password byte size = %d", mPassword.getBytes().length);
                    Alog.d("CryptoKey / getPassword : password = %s", mPassword);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

//            Cipher c = Crypto.getCipher(Cipher.DECRYPT_MODE, _generateKey(DEFAULT_KEY));
//            try {
//                byte[] decrypt = c.doFinal(encrypt);
//                Alog.i("cipher : encrypt = %s", String.valueOf(encrypt));
//                Alog.i("cipher : decrypt = %s %s", new String(decrypt), String.valueOf(decrypt));
//            } catch (IllegalBlockSizeException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } catch (BadPaddingException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }

            } else {
                mPassword = DEFAULT_KEY;
                Alog.d("CryptoKey / getPassword : DEFAULT_KEY = %s", mPassword);
            }
        }
        return mPassword;
    }

    private Key _generateKey(String password) {
        byte[] passwordBytes = password.getBytes();
        int len = passwordBytes.length;

        byte[] keyBytes = new byte[16];

        if (len >= 16) {
            System.arraycopy(passwordBytes, 0, keyBytes, 0, 16);
        } else {
            System.arraycopy(passwordBytes, 0, keyBytes, 0, len);

            for (int i = 0; i < (16 - len); i++) {
                keyBytes[len + i] = passwordBytes[i % len];
            }
        }

        return _generateKey(ALGORITM, keyBytes);
    }

//    private static Key _generateKey(String algorithm) throws NoSuchAlgorithmException {
//        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
//        SecretKey key = keyGenerator.generateKey();
//        return key;
//    }

    private Key _generateKey(String algorithm, byte[] keyData) {
        String upper = algorithm.toUpperCase();
        if ("DES".equals(upper)) {
            SecretKey secretKey = null;
            try {
                KeySpec keySpec = new DESKeySpec(keyData);
                SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
                secretKey = secretKeyFactory.generateSecret(keySpec);
            } catch (InvalidKeyException e) {
                Alog.w("CryptoKey / _generateKey : DES - InvalidKeyException");
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                Alog.w("CryptoKey / _generateKey : DES - NoSuchAlgorithmException");
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                Alog.w("CryptoKey / _generateKey : DES - InvalidKeySpecException");
                e.printStackTrace();
            }

            return secretKey;
        } else if ("DESede".equals(upper) || "TripleDES".equals(upper)) {
            SecretKey secretKey = null;
            try {
                KeySpec keySpec = new DESedeKeySpec(keyData);
                SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
                secretKey = secretKeyFactory.generateSecret(keySpec);
            } catch (InvalidKeyException e) {
                Alog.w("CryptoKey / _generateKey : DESede - InvalidKeyException");
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                Alog.w("CryptoKey / _generateKey : DESede - NoSuchAlgorithmException");
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                Alog.w("CryptoKey / _generateKey : DESede - InvalidKeySpecException");
                e.printStackTrace();
            }
            return secretKey;
        } else {
            SecretKeySpec keySpec = new SecretKeySpec(keyData, algorithm);
            return keySpec;
        }
    }

}
