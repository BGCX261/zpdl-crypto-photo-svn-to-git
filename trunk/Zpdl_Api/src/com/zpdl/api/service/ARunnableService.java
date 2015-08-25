package com.zpdl.api.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.zpdl.api.util.Alog;

public class ARunnableService extends Service implements ARunnable.ARunnableServiceCallback {

    private final IBinder myBinder = new AServiceBinder();
    private ARunnable mRunnable;

    @Override
    public IBinder onBind(Intent intent) {
        Alog.i("ASingleService : onBind");
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Alog.i("ASingleService : onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Alog.i("ASingleService : onRebind");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Alog.i("ASingleService : onCreate");

        mRunnable = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Alog.i("ASingleService : onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Alog.i("ASingleService : onDestroy");
    }

    public class AServiceBinder extends Binder {
        public ARunnableService getService() {
            return ARunnableService.this;
        }
    }

    @Override
    public void onPostExecute() {
        mRunnable = null;
        stopSelf();
    }

    public ARunnable getRunnable() {
        return mRunnable;
    }

    public boolean execute(ARunnable r) {
        if(mRunnable == null) {
            mRunnable = r;
            mRunnable.setOnARunnableServiceCallback(this);
            mRunnable.setContext(this);
            (new Thread(mRunnable)).start();
            return true;
        }
        return false;
    }

}