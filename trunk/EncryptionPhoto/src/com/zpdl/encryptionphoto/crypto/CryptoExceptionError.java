package com.zpdl.encryptionphoto.crypto;

public class CryptoExceptionError extends Exception {

    private static final long serialVersionUID = -6800315940685314381L;

    public CryptoExceptionError(String msg) {
        super(msg);
    }

    public CryptoExceptionError() {
    }

    public CryptoExceptionError(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptoExceptionError(Throwable cause) {
        super(cause);
    }
}
