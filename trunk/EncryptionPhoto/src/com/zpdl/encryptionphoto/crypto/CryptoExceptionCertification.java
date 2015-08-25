package com.zpdl.encryptionphoto.crypto;

public class CryptoExceptionCertification extends Exception {

    private static final long serialVersionUID = 84975194742421801L;

    public CryptoExceptionCertification(String msg) {
        super(msg);
    }

    public CryptoExceptionCertification() {
    }

    public CryptoExceptionCertification(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptoExceptionCertification(Throwable cause) {
        super(cause);
    }
}
