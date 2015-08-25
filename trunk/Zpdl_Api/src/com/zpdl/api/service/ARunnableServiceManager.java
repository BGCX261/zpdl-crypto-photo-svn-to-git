package com.zpdl.api.service;

import java.util.ArrayList;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.zpdl.api.service.ARunnableService.AServiceBinder;
import com.zpdl.api.util.Alog;

public class ARunnableServiceManager {
    private static final int BIND_STATE_UNBIND  = 0x0000;
    private static final int BIND_STATE_BINDING = 0x0001;
    private static final int BIND_STATE_BOUND   = 0x0002;

    private Context mContext;
    private ARunnableServiceConnection mConnection;

    private ARunnableService mService;
    private int              mBindState;

    public ARunnableServiceManager(Context c) {
        mContext = c;
        mConnection = new ARunnableServiceConnection();

        mService    = null;
        mBindState  = BIND_STATE_UNBIND;
    }

    public boolean isBound() {
        return mBindState == BIND_STATE_BOUND;
    }

    public boolean isServiceRunning() {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (ARunnableService.class.getName().equals(runningServiceInfo.service.getClassName())) {
                return true;
           }
        }
        return false;
    }

    public interface onGetRunningRunnableListener {
        void onGetRunningRunnable(ARunnable runnable);
    }

    public synchronized void getRunningRunnable(final onGetRunningRunnableListener l) {
        if(mBindState == BIND_STATE_BOUND) {
            if(l != null) l.onGetRunningRunnable(_getRunningRunnable());
        } else {
            mConnection.addOnBindListener(new onPostBindListener() {
                @Override
                public void onPostBind() {
                    l.onGetRunningRunnable(_getRunningRunnable());
                }
            });

            if(mBindState == BIND_STATE_UNBIND) {
                mBindState = BIND_STATE_BINDING;

                Intent intent = new Intent(mContext, ARunnableService.class);
                mContext.startService(intent);
                mContext.bindService(intent, mConnection, Context.BIND_ADJUST_WITH_ACTIVITY);
            }
        }
    }

    private ARunnable _getRunningRunnable() {
        if(mService != null)
            return mService.getRunnable();
        return null;
    }

    public synchronized void exectue(final ARunnable r) {
        if(mBindState == BIND_STATE_BOUND) {
            _exectue(r);
        } else {
            mConnection.addOnBindListener(new onPostBindListener() {
                @Override
                public void onPostBind() {
                    _exectue(r);
                }
            });

            if(mBindState == BIND_STATE_UNBIND) {
                mBindState = BIND_STATE_BINDING;

                Intent intent = new Intent(mContext, ARunnableService.class);
                mContext.startService(intent);
                mContext.bindService(intent, mConnection, Context.BIND_ADJUST_WITH_ACTIVITY);
            }
        }
    }

    private void _exectue(ARunnable r) {
        if(mService != null) {
            mService.execute(r);
        }
    }

    public synchronized void unbindService() {
        if(mBindState == BIND_STATE_BOUND || mBindState == BIND_STATE_BINDING) {
            mContext.unbindService(mConnection);
            mBindState = BIND_STATE_UNBIND;
//        } else if(mBindState == BIND_STATE_BINDING) {
//            mConnection.setOnBindListener(new onPostBindListener() {
//                @Override
//                public void onPostBind() {
//                    mContext.unbindService(mConnection);
//                    mBindState = BIND_STATE_UNBIND;
//                }
//            });
        }
    }

    public synchronized void stopService() {
        unbindService();

        Intent intent = new Intent(mContext, ARunnableService.class);
        mContext.stopService(intent);
    }

    /* Service bind listener */
    public interface onPostBindListener {
        void onPostBind();
    }

    /* Service Connection */
    private class ARunnableServiceConnection implements ServiceConnection {
        private ArrayList<onPostBindListener> mPostBintListenerList;

        public ARunnableServiceConnection() {
            mPostBintListenerList = new ArrayList<onPostBindListener>();
        }

        public void addOnBindListener(onPostBindListener l) {
            synchronized(mPostBintListenerList) {
                mPostBintListenerList.add(l);
            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Alog.d("ARunnableServiceConnection : onServiceConnected className = "+name);

            AServiceBinder binder = (AServiceBinder) service;
            mService = binder.getService();

            mBindState = BIND_STATE_BOUND;

            synchronized(mPostBintListenerList) {
                for(onPostBindListener l : mPostBintListenerList) {
                    l.onPostBind();
                }
                mPostBintListenerList.clear();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Alog.d("ARunnableServiceConnection : onServiceDisconnected className = "+name);
            mContext.unbindService(mConnection);

            mPostBintListenerList.clear();

            mService = null;
            mBindState = BIND_STATE_UNBIND;
        }
    }
}
