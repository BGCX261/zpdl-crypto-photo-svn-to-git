package com.zpdl.api.util;

import android.util.Log;

public class Alog {
    private static final int VERBOSE   = 1;
    private static final int DEBUG     = 2;
    private static final int INFO      = 3;
    private static final int WARM      = 4;

    private static final String TAG = "HiddenPhoto";

    private static final int LEVEL = VERBOSE;

//    @SuppressWarnings("unused")
    public static void v(String format, Object... args) {
        if(LEVEL <= VERBOSE) Log.v(TAG, String.format(format, args));
    }

    public static void d(String format, Object... args) {
        if(LEVEL <= DEBUG) Log.d(TAG, String.format(format, args));
    }

    public static void i(String format, Object... args) {
        if(LEVEL <= INFO) Log.i(TAG, String.format(format, args));
    }

    public static void w(String format, Object... args) {
        if(LEVEL <= WARM) Log.w(TAG, String.format(format, args));
    }

    public static void e(String format, Object... args) {
        Log.e(TAG, String.format(format, args));
    }
}
