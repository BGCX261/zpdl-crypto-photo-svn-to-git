package com.zpdl.api.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;

public class TdialogTask extends Tdialog {
    private static final String FRAGMENT_TAG  = "TdialogTask";

    private TdialogRunnable mTdialogRunnable;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0x3490)  {
                dismiss();
            }
        }
    };

    public TdialogTask() {
        super();

        mTdialogRunnable = null;
    }

    public TdialogTask(int id) {
        this(id, null);
    }

    public TdialogTask(int id, String callingfragmentTag) {
        super(id, callingfragmentTag);

        mTdialogRunnable = null;
    }

    public void show(FragmentManager fm) {
        _show(fm, FRAGMENT_TAG);
    }

    public void setRunnable(TdialogRunnable t) {
        mTdialogRunnable = t;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateView(Context context, ViewGroup container, Bundle savedInstanceState) {
        ProgressBar pb = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
        pb.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        pb.setIndeterminate(true);

        return pb;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(mTdialogRunnable != null) {
            mTdialogRunnable.setContext(activity);
            mTdialogRunnable.setTdialogRunnableCallBack(new TdialogRunnable.TdialogRunnableCallBack() {
                @Override
                public void TdialogRunnableDone() {
                    mHandler.sendEmptyMessage(0x3490);
                }
            });
            new Thread(mTdialogRunnable).start();
        }
    }

    @Override
    public void onDetach() {
        if(mTdialogRunnable != null) {
            mTdialogRunnable.stop();
        }

        super.onDetach();
    }
}
