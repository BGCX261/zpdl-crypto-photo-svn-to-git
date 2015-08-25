package com.zpdl.api.dialog;

import com.zpdl.api.R;
import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TdialogProgress extends Tdialog {
    private static final String ARG_FRAGMENT_TAG    = "TdialogProgress";

    private static final String ARG_MESSAGE     = "TdialogProgress_message";
    private static final String ARG_MAX_COUNT   = "TdialogProgress_max_count";
    private static final String ARG_COUNT       = "TdialogProgress_count";
    private static final String ARG_PROGRESS    = "TdialogProgress_progress";

    private ProgressBar mProgressBar;
    private TextView    mMessageView;
    private TextView    mPercentView;
    private TextView    mCountView;

    private String      mMessage;
    private int         mMaxCount;
    private int         mCount;
    private int         mProgress;

    public TdialogProgress() {
        super();

        mProgressBar = null;
        mMessageView = null;
        mPercentView = null;
        mCountView = null;

        mMaxCount = 0;
        mCount = 0;
        mProgress = 0;
    }

    public TdialogProgress(int id) {
        this(id, null);
    }

    public TdialogProgress(int id, String callingfragmentTag) {
        super(id, callingfragmentTag);

        mProgressBar = null;
        mMessageView = null;
        mPercentView = null;
        mCountView = null;

        mMaxCount = 0;
        mCount = 0;
        mProgress = 0;
    }

    public void show(FragmentManager fm) {
        _show(fm, ARG_FRAGMENT_TAG);
    }


    public void setMessage(String message) {
        mMessage = message;
        if(mMessage == null) {
            if(mMessageView != null && mMessageView.getVisibility() != View.GONE) {
                mMessageView.setVisibility(View.GONE);
            }
        } else {
            if(mMessageView != null) {
                if(mMessageView.getVisibility() != View.VISIBLE)
                    mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(mMessage);
            }
        }
    }

    public void setMaxCount(int maxcount) {
        mMaxCount = maxcount;
        if(mMaxCount == 0) {
            if(mCountView != null && mCountView.getVisibility() != View.GONE) {
                mCountView.setVisibility(View.GONE);
            }
        } else {
            if(mCountView != null) {
                if(mCountView.getVisibility() != View.VISIBLE)
                    mCountView.setVisibility(View.VISIBLE);
                _setCount();
            }
        }
    }

    public void setCount(int count) {
        if(count < 0) {
            mCount = 0;
        } else if(count > mMaxCount) {
            mCount = mMaxCount;
        } else {
            mCount = count;
        }
        _setCount();
    }

    public void setProgress(int progress) {
        if(progress < 0) {
            mProgress = 0;
        } else if(progress > 100) {
            mProgress = 100;
        } else {
            mProgress = progress;
        }
        _setProgress();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mMessage  = savedInstanceState.getString(ARG_MESSAGE, null);
            mMaxCount = savedInstanceState.getInt(ARG_MAX_COUNT, 0);
            mCount    = savedInstanceState.getInt(ARG_COUNT, 0);
            mProgress = savedInstanceState.getInt(ARG_PROGRESS, 0);
        }
    }

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateView(Context context, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(context).inflate(R.layout.tdialog_progress, null);

        mProgressBar = (ProgressBar) view.findViewById(R.id.tdialog_progress_bar);

        mMessageView = (TextView) view.findViewById(R.id.tdialog_progress_message);
        if(mMessage != null) {
            mMessageView.setText(mMessage);
        } else {
            mMessageView.setVisibility(View.GONE);
        }

        mCountView = (TextView) view.findViewById(R.id.tdialog_progress_count);
        if(mMaxCount > 0) {
            _setCount();
        } else {
            mCountView.setVisibility(View.GONE);
        }

        mPercentView = (TextView) view.findViewById(R.id.tdialog_progress_percent);
        _setProgress();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(ARG_MESSAGE, mMessageView.getText().toString());
        outState.putInt(ARG_MAX_COUNT, mMaxCount);
        outState.putInt(ARG_COUNT, mCount);
        outState.putInt(ARG_PROGRESS, mProgress);
    }

    private void _setProgress() {
        if(mProgressBar != null) {
            mProgressBar.setProgress(mProgress);
        }
        if(mPercentView != null) {
            mPercentView.setText(String.format("%d %%", mProgress));
        }
    }

    private void _setCount() {
        if(mCountView != null && mCountView.getVisibility() == View.VISIBLE) {
            mCountView.setText(String.format("%d / %d", mCount, mMaxCount));
        }
    }
}
