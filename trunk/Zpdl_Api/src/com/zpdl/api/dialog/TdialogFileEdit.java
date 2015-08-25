package com.zpdl.api.dialog;

import java.io.File;

import com.zpdl.api.R;
import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class TdialogFileEdit extends Tdialog {
    private static final String FRAGMENT_TAG        = "TdialogFileEdit";

    private static final String ARG_HINT            = "TdialogFileEdit_hint";
    private static final String ARG_PARENT          = "TdialogFileEdit_parent";
    private static final String ARG_NAME            = "TdialogFileEdit_name";
    private static final String ARG_EDIT            = "TdialogFileEdit_edit";
    private static final String ARG_SELECTION_START = "TdialogFileEdit_selection_start";
    private static final String ARG_SELECTION_END   = "TdialogFileEdit_selection_end";

    private EditText    mEditTextView;

    public TdialogFileEdit() {
        super();

        mEditTextView = null;
    }

    public TdialogFileEdit(int id) {
        this(id, null);
    }

    public TdialogFileEdit(int id, String callingfragmentTag) {
        super(id, callingfragmentTag);

        mEditTextView = null;
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

    public void setParentPath(String path) {
        getArguments().putString(ARG_PARENT, path);
    }

    public void setFileName(String filename) {
        getArguments().putString(ARG_NAME, filename);

        if(mEditTextView != null) {
            mEditTextView.setText(filename);
        }
    }

    public String getPath() {
        if(mEditTextView != null) {
            return getArguments().getString(ARG_PARENT, null) + File.separator + mEditTextView.getText().toString();
        }
        return null;
    }

    public String getOriginalPath() {
        String path = null;
        String parent = getArguments().getString(ARG_PARENT, null);
        String name = getArguments().getString(ARG_NAME, null);
        if(parent != null) {
            if(name != null) {
                path = parent + File.separator + name;
            } else {
                path = name;
            }
        } else {
            if(name != null) {
                path = name;
            }
        }
        return path;
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

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateView(Context context, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(context).inflate(R.layout.tdialog_fileeditor, null);

        mEditTextView = (EditText) view.findViewById(R.id.tdialog_file_edit);
        String hint = getArguments().getString(ARG_HINT, null);
        if(hint != null) mEditTextView.setHint(hint);
        mEditTextView.requestFocus();

        if (savedInstanceState != null) {
            String editText = savedInstanceState.getString(ARG_EDIT, null);
            int selectionStart = savedInstanceState.getInt(ARG_SELECTION_START, -1);
            int selectionEnd = savedInstanceState.getInt(ARG_SELECTION_END, -1);
            if(editText != null) {
                mEditTextView.setText(editText);
                mEditTextView.setSelection(selectionStart, selectionEnd);
            }
        } else {
            String editText = getArguments().getString(ARG_NAME, null);
            if(editText != null) {
                int separatorIndex = editText.lastIndexOf('.');
                separatorIndex = (separatorIndex < 0) ? editText.length() : separatorIndex;
                mEditTextView.setText(editText);
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
}
