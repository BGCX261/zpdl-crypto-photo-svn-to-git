package com.zpdl.api.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.os.Bundle;

public class DdialogAlert extends Ddialog {
    private static final String ARG_FRAGMENT_TAG    = "DdialogAlert";
    private static final String ARG_MESSAGE         = "DdialogAlert_message";

    public DdialogAlert() {
        super();
    }

    public DdialogAlert(int id) {
        this(id, null);
    }

    public DdialogAlert(int id, String callingfragmentTag) {
        super(id, callingfragmentTag);
    }

    public void show(FragmentManager fm) {
        _show(fm, ARG_FRAGMENT_TAG);
    }

    public void setMessage(String message) {
        if(message == null) {
            getArguments().putString(ARG_MESSAGE, "");
        } else {
            getArguments().putString(ARG_MESSAGE, message);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = builder();

        String msg = getArguments().getString(ARG_MESSAGE, null);
        if(msg != null) {
            builder.setMessage(msg);
        }

        return builder.create();
    }
}
