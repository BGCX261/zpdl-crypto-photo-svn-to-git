package com.zpdl.encryptionphoto;

import java.io.File;

import android.os.Environment;


public class HpConfigure {
    public static final String SHARED_PREFERRENCES_NAME = "Hidden photo";

    public static final String WORKING_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Crypto Photo";
}
