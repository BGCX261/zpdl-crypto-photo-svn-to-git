package com.zpdl.api.service;

import android.content.Context;


public abstract class ARunnable implements Runnable {
    private ARunnableServiceCallback mServiceCallback;
    protected Context mContext;

    public interface ARunnableServiceCallback {
        void onPostExecute();
    }

    public void setOnARunnableServiceCallback(ARunnableServiceCallback callback) {
        mServiceCallback = callback;
    }

    public ARunnable() {
        mServiceCallback = null;
        mContext = null;
    }

    public void setContext(Context c) {
        mContext = c;
    }

    @Override
    public void run() {
        aRun();

        if(mServiceCallback != null) mServiceCallback.onPostExecute();
    }

    protected abstract void aRun();
}
