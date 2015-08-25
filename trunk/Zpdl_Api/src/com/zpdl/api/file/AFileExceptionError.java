package com.zpdl.api.file;

public class AFileExceptionError extends Exception {

    private static final long serialVersionUID = -4118123594482745644L;

    public AFileExceptionError(String msg) {
        super(msg);
    }

    public AFileExceptionError() {
    }

    public AFileExceptionError(String message, Throwable cause) {
        super(message, cause);
    }

    public AFileExceptionError(Throwable cause) {
        super(cause);
    }
}
