package com.zpdl.api.dialog;

import android.content.Context;


public abstract class TdialogRunnable implements Runnable {
    protected Context mContext = null;
    protected boolean running = false;

    private TdialogRunnableCallBack mTdialogRunnableCallBack = null;
    private Object stopSyncObject = new Object();

    public interface TdialogRunnableCallBack {
        public void TdialogRunnableDone();
    }

    @Override
    public void run() {
        running = true;
        runnable();
        synchronized (stopSyncObject) {
            if(running)
                if(mTdialogRunnableCallBack != null) mTdialogRunnableCallBack.TdialogRunnableDone();
            running = false;
            stopSyncObject.notifyAll();
        }
    }

    public void stop() {
        if(running) {
            synchronized (stopSyncObject) {
                running = false;
                try {
                    stopSyncObject.wait(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setContext(Context c) {
        mContext = c;
    }

    public void setTdialogRunnableCallBack(TdialogRunnableCallBack callback) {
        mTdialogRunnableCallBack = callback;
    }

    public abstract void runnable();

}
