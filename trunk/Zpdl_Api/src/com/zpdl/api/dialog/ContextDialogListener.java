package com.zpdl.api.dialog;

import java.util.ArrayList;

public interface ContextDialogListener {

    public void onCreateContextDialog(ArrayList<ContextDialogMenu> menu, String selectedItem);

    public void onContextDialogItemSelected(int id, String selectedItem);

}