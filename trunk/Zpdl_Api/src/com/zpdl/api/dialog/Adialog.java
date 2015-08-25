package com.zpdl.api.dialog;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;

public class Adialog extends DialogFragment {
    public static final int EVENT_POSITIVE                  = 0x0000;
    public static final int EVENT_NEGATIVE                  = 0x0001;
    public static final int EVENT_NEUTRAL                   = 0x0002;
    public static final int EVENT_CANCEL                    = 0x0003;

    private static final String ARG_ID                      = "adialog_id";
    private static final String ARG_CALLING_FRAGMENT_TAG    = "adialog_fragment_tag";

    protected static final String ARG_TITLE                 = "adialog_title";
    protected static final String ARG_BTN_POSITIVE          = "adialog_btnpositive";
    protected static final String ARG_BTN_NEGATIVE          = "adialog_btnnegative";
    protected static final String ARG_BTN_NEUTRAL           = "adialog_btnneutral";

    protected Adialog           mDialog;
    protected AdialogListener   mListener;

    public Adialog() {
        super();

        mDialog = this;
        mListener = null;
    }

    public Adialog(int id) {
        this(id, null);
    }

    public Adialog(int id, String callingfragmentTag) {
        super();

        mDialog = this;
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);

        if(callingfragmentTag != null) {
            args.putString(ARG_CALLING_FRAGMENT_TAG, callingfragmentTag);
        }
        setArguments(args);
    }

    public int getDialogId() {
        return getArguments().getInt(ARG_ID, 0);
    }

    public void setTitle(String title) {
        if(title == null) {
            getArguments().putString(ARG_TITLE, "");
        } else {
            getArguments().putString(ARG_TITLE, title);
        }
    }

    public void setPositiveButton(String title) {
        if(title == null) {
            getArguments().putString(ARG_BTN_POSITIVE, "");
        } else {
            getArguments().putString(ARG_BTN_POSITIVE, title);
        }
    }

    public void setNegativeButton(String title) {
        if(title == null) {
            getArguments().putString(ARG_BTN_NEGATIVE, "");
        } else {
            getArguments().putString(ARG_BTN_NEGATIVE, title);
        }
    }

    public void setNeutralButton(String title) {
        if(title == null) {
            getArguments().putString(ARG_BTN_NEUTRAL, "");
        } else {
            getArguments().putString(ARG_BTN_NEUTRAL, title);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            String callingFragmentTag = getArguments().getString(ARG_CALLING_FRAGMENT_TAG, null);
            if(callingFragmentTag == null) {
                mListener = (AdialogListener) activity;
            } else {
                Fragment f = activity.getFragmentManager().findFragmentByTag(callingFragmentTag);
                mListener = (AdialogListener) f;
            }
            mListener.onDialogAttach(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ADialogListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.onDialogDetach(this);
        mListener = null;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if(mListener != null) {
            mListener.onDialogEvent(this, EVENT_CANCEL);
        }
    }

    protected void _show(FragmentManager fm, String tag) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(tag);

        if (prev != null) {
            ft.remove(prev);
        }

        show(ft, tag);
    }
}
