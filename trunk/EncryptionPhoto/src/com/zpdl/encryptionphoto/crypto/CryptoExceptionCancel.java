package com.zpdl.encryptionphoto.crypto;

public class CryptoExceptionCancel extends Exception {

    private static final long serialVersionUID = 6183610420523050229L;

    public CryptoExceptionCancel(String msg) {
        super(msg);
    }

    public CryptoExceptionCancel() {
    }

    public CryptoExceptionCancel(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptoExceptionCancel(Throwable cause) {
        super(cause);
    }
}
