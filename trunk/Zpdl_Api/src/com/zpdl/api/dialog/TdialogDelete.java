package com.zpdl.api.dialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;

import com.zpdl.api.file.AFileScanner;

public class TdialogDelete extends Tdialog {
    public  static final int     EVENT_DELETE_DONE   = 0x0010;

    private static final String FRAGMENT_TAG        = "TdialogDelete";

    private static final String ARG_PATH            = "TdialogFileEdit_path";

    private DeleteThread mDeleteThread;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == EVENT_DELETE_DONE)  {
                mListener.onDialogEvent(mDialog, msg.what);
                dismiss();
            }
        }
    };

    public TdialogDelete() {
        super();

        mDeleteThread = null;
    }

    public TdialogDelete(int id) {
        this(id, null);
    }

    public TdialogDelete(int id, String callingfragmentTag) {
        super(id, callingfragmentTag);

        mDeleteThread = null;
    }

    public void show(FragmentManager fm) {
        _show(fm, FRAGMENT_TAG);
    }

    public void setDeleteFileName(String[] filename) {
        getArguments().putStringArray(ARG_PATH, filename);
    }

    public String[] getDeleteFileName() {
        return getArguments().getStringArray(ARG_PATH);
    }

    public void cancel() {
        if(mDeleteThread != null) mDeleteThread.cancel();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateView(Context context, ViewGroup container, Bundle savedInstanceState) {
        float dip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getActivity().getResources().getDisplayMetrics());

        ProgressBar pb = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        pb.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        pb.setPadding(0, 0, 0, (int)(10*dip));
        pb.setIndeterminate(true);

        return pb;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mDeleteThread = new DeleteThread(activity, getArguments().getStringArray(ARG_PATH));
        if(mDeleteThread.size() <= 0) {
            dismiss();
        } else {
            mDeleteThread.start();
        }
    }

    @Override
    public void onDetach() {
        mDeleteThread.cancel();
        mDeleteThread = null;

        super.onDetach();
    }

    private class DeleteThread extends Thread {
        private Context context;

        private boolean running;
        private ArrayList<String> fileList;
        private AFileScanner mAFileScanner;
        private Object cancelSyncObject = new Object();

        DeleteThread(Context context, String[] deleteFile) {
            this.context = context;

            running = false;
            mAFileScanner = null;

            fileList = new ArrayList<String>();
            for(String filename : deleteFile) {
                if(new File(filename).exists()) {
                    fileList.add(filename);
                }
            }
        }

        public int size() {
            return fileList.size();
        }

        public void cancel() {
            if(running) {
                synchronized (cancelSyncObject) {
                    if(running) {
                        running = false;
                        try {
                            cancelSyncObject.wait(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        @Override
        public void run() {
            running = true;

            try {
                mAFileScanner = new AFileScanner(context);
                for(String filename : fileList) {
                    _delete(new File(filename));
                    mAFileScanner.deleteFile(filename);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(mAFileScanner != null) {
                    mAFileScanner.release();
                    mAFileScanner = null;
                }
            }

            synchronized (cancelSyncObject) {
                if(running)
                    mHandler.sendEmptyMessage(EVENT_DELETE_DONE);
                running = false;
                cancelSyncObject.notifyAll();
            }
        }

        private void _delete(File f) throws IOException {
            if(f.exists() && f.isDirectory()) {
                for(File dFile : f.listFiles()) {
                    _delete(dFile);
                }

                if(!f.delete()) {
                    throw new IOException("Failed delete folder : " + f.getPath());
                }

            } else if(f.exists() && f.isFile()) {
                if(f.delete()) {

                } else {
                    throw new IOException("Failed delete file : " + f.getPath());
                }
            }
            if(!running) return;
        }
    }
}
