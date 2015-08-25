package com.zpdl.test.singleservice;

import com.zpdl.test.MainActivity;
import com.zpdl.test.R;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.zpdl.api.dialog.Adialog;
import com.zpdl.api.dialog.AdialogListener;
import com.zpdl.api.dialog.DdialogProgress;
import com.zpdl.api.service.ARunnable;
import com.zpdl.api.service.ARunnableServiceManager;
import com.zpdl.api.util.Alog;

public class ASingleServiceFragment extends Fragment implements AdialogListener {
    private ARunnableServiceManager mServiceManager;

    public static ASingleServiceFragment newInstance() {
        ASingleServiceFragment fragment = new ASingleServiceFragment();
        return fragment;
    }

    public ASingleServiceFragment() {
        mServiceManager = null;
        mTestThread = null;
        mProgressDialog = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_singleservice, container, false);

        Button btn = (Button) rootView.findViewById(R.id.adialog_singleservice_1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog = new DdialogProgress(1, "ASingleServiceFragment");
                mProgressDialog.setTitle("ASingleServiceFragment");
                mProgressDialog.setMessage("Test \nSee the UI");
                mProgressDialog.setMaxCount(20);
//                mProgressDialog.setPositiveButton("Positive");
                mProgressDialog.setNegativeButton("Negative");
//                mProgressDialog.setNeutralButton("Neutral");
                mProgressDialog.show(getFragmentManager());

                mTestThread = new TestThread(new testListener() {
                    @Override
                    public void onProgress(int progress) {
                        if(progress % 10 == 0)
                        Alog.d("ASingleServiceFragment : mTestThread - onProgress = %d",progress);

                        mHandler.sendEmptyMessage(progress);
                    }
                });

                mServiceManager.exectue(mTestThread);
            }
        });

        btn = (Button) rootView.findViewById(R.id.adialog_singleservice_2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mServiceManager.unbindService();
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached("ASingleServiceFragment");

        mServiceManager = new ARunnableServiceManager(activity);

//        AServiceTask aServiceTask = new AServiceTask(activity) {
//            @Override
//            public void run() {
//
//            }
//        };
//        if(aServiceTask.isServiceRunning()) {
//            Alog.i("ASingleServiceFragment : isServiceRunning - true");
//        } else {
//            Alog.i("ASingleServiceFragment : isServiceRunning - false");
//        }
        Alog.i("ASingleServiceFragment : onAttach - bindService");
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Alog.i("ASingleServiceFragment : onDetach - unbindService");
        if(mServiceManager != null) {
            mServiceManager.unbindService();
        }
    }

    private DdialogProgress mProgressDialog;

    @Override
    public void onDialogAttach(Adialog dialog) {
        Alog.i("ASingleServiceFragment : onDialogLoaded");
        mProgressDialog = (DdialogProgress) dialog;
        if(mTestThread == null) {
            if(mServiceManager.isServiceRunning()) {
                mServiceManager.getRunningRunnable(new ARunnableServiceManager.onGetRunningRunnableListener() {
                    @Override
                    public void onGetRunningRunnable(ARunnable runnable) {
                        if(runnable == null) {
                            mProgressDialog.dismiss();
                        } else {
                            mTestThread = (TestThread) runnable;
                            mTestThread.mListener = new testListener() {
                                @Override
                                public void onProgress(int progress) {
                                    if(progress % 10 == 0)
                                        Alog.d("ASingleServiceFragment : mTestThread - onProgress = %d",progress);

                                    mHandler.sendEmptyMessage(progress);
                                }
                            };
                        }
                    }
                });
            } else {
                mProgressDialog.dismiss();
            }
        }
    }

    @Override
    public void onDialogDetach(Adialog dialog) {
        Alog.i("ASingleServiceFragment : onDialogUnLoaded "+dialog);
        mProgressDialog = null;
    }

    @Override
    public void onDialogEvent(Adialog dialog, int which) {
        Alog.i("ASingleServiceFragment : onDialogEvent "+dialog);
        if(which == Adialog.EVENT_NEGATIVE || which == Adialog.EVENT_CANCEL) {
            mTestThread.stop();
        }
    }

    TestThread mTestThread = null;

    private interface testListener {
        void onProgress(int progress);
    }

    private class TestThread extends ARunnable {
        testListener mListener;
        int progress;
        boolean running;

        TestThread(testListener l) {
            super();

            mListener = l;
            progress = 0;
            running = false;
        }

        @Override
        protected void aRun() {
            running = true;
            while(progress < 100 && running) {
                mListener.onProgress(++progress);
//                Alog.d("ASingleServiceFragment : testThread progress = %d", progress);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            mServiceManager.unbindService_();
        }

        protected void stop() {
            running = false;
        }

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            Alog.d("handleMessage = "+msg);
            if(mProgressDialog != null) {
                int progress = msg.what;

                mProgressDialog.setProgress(progress);
                if(progress % 5 == 0) {
                    mProgressDialog.setCount(progress/5);
                }
            }
        }
    };
}
