package com.zpdl.api.dialog;

public interface AdialogListener {
    public void onDialogAttach(Adialog dialog);

    public void onDialogDetach(Adialog dialog);

    public void onDialogEvent(Adialog dialog, int which);
}