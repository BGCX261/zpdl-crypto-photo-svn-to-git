package com.zpdl.api.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;

class Ddialog extends Adialog implements DialogInterface.OnClickListener {

    public Ddialog() {
        super();
    }

    public Ddialog(int id) {
        this(id, null);
    }

    public Ddialog(int id, String callingfragmentTag) {
        super(id, callingfragmentTag);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if(mListener != null) {
            if(which == DialogInterface.BUTTON_POSITIVE) {
                mListener.onDialogEvent(this, EVENT_POSITIVE);
            } else if(which == DialogInterface.BUTTON_NEGATIVE) {
                mListener.onDialogEvent(this, EVENT_NEGATIVE);
            } else if(which == DialogInterface.BUTTON_NEUTRAL) {
                mListener.onDialogEvent(this, EVENT_NEUTRAL);
            }
        }
    }

    protected AlertDialog.Builder builder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String arg = getArguments().getString(ARG_TITLE, null);
        if(arg != null) {
            builder.setTitle(arg);
        }

        arg = getArguments().getString(ARG_BTN_POSITIVE, null);
        if(arg != null) {
            builder.setPositiveButton(arg, this);
        }

        arg = getArguments().getString(ARG_BTN_NEGATIVE, null);
        if(arg != null) {
            builder.setNegativeButton(arg, this);
        }

        arg = getArguments().getString(ARG_BTN_NEUTRAL, null);
        if(arg != null) {
            builder.setNeutralButton(arg, this);
        }

        return builder;
    }
}
