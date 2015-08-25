package com.zpdl.encryptionphoto.cache;

import android.graphics.Bitmap;

public class TnParam {
    public Bitmap thumbnail;
    public int orientation;
    public String path;

    public boolean isError;

    public TnParam() {
        thumbnail = null;
        orientation = 0;
        path = null;

        isError = false;
    }
}
