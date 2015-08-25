package com.zpdl.api.dialog;

import com.zpdl.api.R;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class TdialogAlert extends Tdialog {
    private static final String ARG_FRAGMENT_TAG    = "TdialogAlert";
    private static final String ARG_MESSAGE         = "TdialogAlert_message";

    public TdialogAlert() {
        super();
    }

    public TdialogAlert(int id) {
        this(id, null);
    }

    public TdialogAlert(int id, String callingfragmentTag) {
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
    protected View onCreateView(Context context, ViewGroup container, Bundle savedInstanceState) {
        float dip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getActivity().getResources().getDisplayMetrics());

        TextView tv = new TextView(context);
        tv.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setPadding((int)(10 * dip), (int)(5 * dip), (int)(10 * dip), (int)(5 * dip));
        tv.setGravity(Gravity.START);
        tv.setTextAppearance(context, R.style.TextAppearance_tdialog_large);
        tv.setText(getArguments().getString(ARG_MESSAGE, ""));

        return tv;
    }
}
