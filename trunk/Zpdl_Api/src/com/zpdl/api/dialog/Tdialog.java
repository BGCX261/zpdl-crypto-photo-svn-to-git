package com.zpdl.api.dialog;

import com.zpdl.api.R;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

abstract class Tdialog extends Adialog {

    public Tdialog() {
        super();
    }

    public Tdialog(int id) {
        this(id, null);
    }

    public Tdialog(int id, String callingfragmentTag) {
        super(id, callingfragmentTag);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tdialog_base, container);

        TdialogContainer dialogContainer = (TdialogContainer) view.findViewById(R.id.tdialog_conteainer);

        String title = getArguments().getString(ARG_TITLE, null);
        if(title != null)
            dialogContainer.setTitle(title);
        dialogContainer.setView(onCreateView(getActivity(), dialogContainer, savedInstanceState));
        dialogContainer.setButton(getArguments().getString(ARG_BTN_POSITIVE, null),
                                  getArguments().getString(ARG_BTN_NEUTRAL, null),
                                  getArguments().getString(ARG_BTN_NEGATIVE, null),
                                  new TdialogContainer.onDialogButtonClickListener() {
            @Override
            public void onDialogButtonClick(int which) {
                if(mListener != null) mListener.onDialogEvent(mDialog, which);
            }
        });

        return view;
    }

    protected abstract View onCreateView(Context context, ViewGroup container, Bundle savedInstanceState);
}
