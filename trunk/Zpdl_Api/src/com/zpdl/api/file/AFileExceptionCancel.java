package com.zpdl.api.file;

public class AFileExceptionCancel extends Exception {

    private static final long serialVersionUID = -4388489093044933379L;

    public AFileExceptionCancel(String msg) {
        super(msg);
    }

    public AFileExceptionCancel() {
    }

    public AFileExceptionCancel(String message, Throwable cause) {
        super(message, cause);
    }

    public AFileExceptionCancel(Throwable cause) {
        super(cause);
    }
}
