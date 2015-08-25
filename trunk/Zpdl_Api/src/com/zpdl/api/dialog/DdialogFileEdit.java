package com.zpdl.api.dialog;

import com.zpdl.api.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class DdialogFileEdit extends Ddialog {
    private static final String FRAGMENT_TAG        = "DdialogFileEdit";

    private static final String ARG_HINT            = "DdialogFileEdit_hint";
    private static final String ARG_EDIT            = "DdialogFileEdit_edit";
    private static final String ARG_SELECTION_START = "DdialogFileEdit_selection_start";
    private static final String ARG_SELECTION_END   = "DdialogFileEdit_selection_end";

    private EditText    mEditTextView;
    private String      mEditText;


    public DdialogFileEdit() {
        super();

        mEditTextView = null;
        mEditText = null;
    }

    public DdialogFileEdit(int id) {
        this(id, null);
    }

    public DdialogFileEdit(int id, String callingfragmentTag) {
        super(id, callingfragmentTag);

        mEditTextView = null;
        mEditText = null;
    }

    public void show(FragmentManager fm) {
        _show(fm, FRAGMENT_TAG);
    }

    public void setHint(String hint) {
        if(hint == null) {
            getArguments().putString(ARG_HINT, "");
        } else {
            getArguments().putString(ARG_HINT, hint);
        }
    }

    public void setEditText(String text) {
        mEditText = (text == null) ? "" : text;
        if(mEditTextView != null) {
            mEditTextView.setText(mEditText);
        }
    }

    public String getEditText() {
        if(mEditTextView != null)
            return mEditTextView.getText().toString();
        return null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = builder();
        builder.setView(onCreateView(getActivity().getLayoutInflater(), savedInstanceState));

        return builder.create();
    }

    @SuppressLint("InflateParams")
    private View onCreateView(LayoutInflater inflater,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fileeditor, null);

        mEditTextView = (EditText) view.findViewById(R.id.dialog_file_edit);
        String hint = getArguments().getString(ARG_HINT, null);
        if(hint != null) mEditTextView.setHint(hint);
        mEditTextView.requestFocus();

        if (savedInstanceState != null) {
            mEditText = savedInstanceState.getString(ARG_EDIT, null);
            int selectionStart = savedInstanceState.getInt(ARG_SELECTION_START, -1);
            int selectionEnd = savedInstanceState.getInt(ARG_SELECTION_END, -1);
            if(mEditText != null) {
                mEditTextView.setText(mEditText);
                mEditTextView.setSelection(selectionStart, selectionEnd);
            }
        } else {
            if(mEditText != null) {
                int separatorIndex = mEditText.lastIndexOf('.');
                separatorIndex = (separatorIndex < 0) ? mEditText.length() : separatorIndex;
                mEditTextView.setText(mEditText);
                mEditTextView.setSelection(0, separatorIndex);
            }
        }

        new Handler().postDelayed(new Runnable(){
            public void run(){
                InputMethodManager mgr = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.showSoftInput(mEditTextView, InputMethodManager.SHOW_IMPLICIT);
            }
            }, 100 );

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mEditTextView != null) {
            outState.putString(ARG_EDIT, mEditTextView.getText().toString());
            outState.putInt(ARG_SELECTION_START, mEditTextView.getSelectionStart());
            outState.putInt(ARG_SELECTION_END, mEditTextView.getSelectionEnd());
        }
    }
}
