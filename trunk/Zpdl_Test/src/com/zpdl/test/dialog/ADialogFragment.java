package com.zpdl.test.dialog;

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
import android.widget.Toast;

import com.zpdl.api.dialog.Adialog;
import com.zpdl.api.dialog.AdialogListener;
import com.zpdl.api.dialog.DdialogAlert;
import com.zpdl.api.dialog.DdialogFileEdit;
import com.zpdl.api.dialog.DdialogProgress;
import com.zpdl.api.dialog.TdialogAlert;
import com.zpdl.api.dialog.TdialogFolderBrowser;
import com.zpdl.api.dialog.TdialogProgress;
import com.zpdl.api.file.AFileUtil;
import com.zpdl.api.util.Alog;

public class ADialogFragment extends Fragment implements AdialogListener {
    /**
     * Returns a new instance of this fragment for the given section number.
     */
    private static int fragmentCnt = 0;

    public static ADialogFragment newInstance() {
        Alog.e("ADialogFragment newInstance = %d", fragmentCnt);
        ADialogFragment fragment = new ADialogFragment();
        return fragment;
    }

    public ADialogFragment() {
        fragmentCnt++;
        Alog.e("ADialogFragment fragmentCnt = %d", fragmentCnt);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_adialog, container, false);

        Button btn = (Button) rootView.findViewById(R.id.adialog_alert);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Alog.i("ADialogFragment Tag = %s", getTag());

                DdialogAlert dialog = new DdialogAlert(0, getTag());
                dialog.setTitle("Title");
                dialog.setMessage("Test \nSee the UI");
                dialog.setPositiveButton("Positive");
                dialog.setNegativeButton("Negative");
                dialog.setNeutralButton("Neutral");
                dialog.show(getFragmentManager());
            }
        });

        btn = (Button) rootView.findViewById(R.id.adialog_progress);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DdialogProgress dialog = new DdialogProgress(1, getTag());
                dialog.setMessage("Test \nSee the UI");
                dialog.setMaxCount(20);
                dialog.setPositiveButton("Positive");
                dialog.setNegativeButton("Negative");
                dialog.setNeutralButton("Neutral");
                dialog.show(getFragmentManager());

                mProgress = 0;
                mCount = 0;
                mHandler.sendEmptyMessageDelayed(0, 50);
            }
        });

        btn = (Button) rootView.findViewById(R.id.adialog_edit_filename);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DdialogFileEdit dialog = new DdialogFileEdit(2, getTag());
                dialog.setTitle("File Edit");
                dialog.setHint("hint");
                dialog.setEditText("Edit Text.jar");
                dialog.setPositiveButton("Positive");
                dialog.setNegativeButton("Negative");
                dialog.setNeutralButton("Neutral");
                dialog.show(getFragmentManager());
            }
        });

        btn = (Button) rootView.findViewById(R.id.tdialog_alert);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TdialogAlert dialog = new TdialogAlert(0, getTag());
                dialog.setTitle("Title");
                dialog.setMessage("Test \nSee the UI");
                dialog.setPositiveButton("Positive");
                dialog.setNegativeButton("Negative");
                dialog.setNeutralButton("Neutral");
                dialog.show(getFragmentManager());
            }
        });

        btn = (Button) rootView.findViewById(R.id.tdialog_progress);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TdialogProgress dialog = new TdialogProgress(1, getTag());
                dialog.setTitle("Title");
                dialog.setMessage("Test \nSee the UI");
                dialog.setMaxCount(20);
                dialog.setPositiveButton("Positive");
                dialog.setNegativeButton("Negative");
                dialog.setNeutralButton("Neutral");
                dialog.show(getFragmentManager());

                mProgress = 0;
                mCount = 0;
                mHandler.sendEmptyMessageDelayed(0, 50);
            }
        });

        btn = (Button) rootView.findViewById(R.id.tdialog_folder_browser);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TdialogFolderBrowser dialog = new TdialogFolderBrowser(3, getTag());
                dialog.setTitle("Title");
                dialog.setPath(AFileUtil.getInstance().getSdCard());
                dialog.setPositiveButton("Positive");
                dialog.setNegativeButton("Negative");
                dialog.setNeutralButton("Neutral");
                dialog.show(getFragmentManager());
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached("ADialogTestFragment");
    }

    @Override
    public void onDialogAttach(Adialog dialog) {
        mADialog = dialog;
        Alog.d("onDialogAttach getActivity() = "+getActivity());
        Alog.d("onDialogAttach dialog = "+dialog);
        Alog.d("onDialogAttach fragmentCnt = %d", fragmentCnt);
//        Toast.makeText(getActivity(), "onDialogLoaded "+dialog.getTag(), Toast.LENGTH_SHORT);
        if(mADialog.getDialogId() == 1) {
            Alog.d("onDialogAttach Progress dialog");
        }
    }

    @Override
    public void onDialogDetach(Adialog dialog) {
        Alog.i("onDialogDetach getActivity() = "+getActivity());
        Alog.i("onDialogDetach dialog = "+dialog);
        Alog.i("onDialogDetach fragmentCnt = %d", fragmentCnt);
//        Toast.makeText(getActivity(), "onDialogUnLoaded "+dialog.getTag(), Toast.LENGTH_SHORT);
        if(mADialog.getDialogId() == 1) {
            Alog.i("onDialogDetach Progress dialog");
        }
        mADialog = null;
    }

    @Override
    public void onDialogEvent(Adialog dialog, int which) {
        Alog.w("onDialogButtonClick getActivity() = "+getActivity());
        Alog.w("onDialogButtonClick dialog = "+dialog);
        Alog.w("onDialogButtonClick fragmentCnt = ", fragmentCnt);
        if(getActivity() != null)
            Toast.makeText(getActivity(), "onDialogButtonClick which = "+which+" "+dialog.getTag(), Toast.LENGTH_SHORT).show();

        if(mADialog.getDialogId() == 1) {
            if(which == Adialog.EVENT_NEGATIVE || which == Adialog.EVENT_CANCEL) {
                mHandler.removeMessages(0);
            }
        }
    }

    private static Adialog mADialog;
    private static int mProgress = 0;
    private static int mCount = 0;

    private static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Alog.d("handleMessage = "+msg);
            if(mADialog != null) {
                if(mADialog.getDialogId() == 1) {
                    if(mADialog instanceof DdialogProgress) {
                        DdialogProgress progressDialog = (DdialogProgress) mADialog;
                        progressDialog.setProgress(++mProgress);
                        if(mProgress % 5 == 0) {
                            progressDialog.setCount(++mCount);
                        }
                    } else if(mADialog instanceof TdialogProgress) {
                        TdialogProgress progressDialog = (TdialogProgress) mADialog;
                        progressDialog.setProgress(++mProgress);
                        if(mProgress % 5 == 0) {
                            progressDialog.setCount(++mCount);
                        }
                    }
                }
                if(mProgress < 120) {
                    mHandler.sendEmptyMessageDelayed(0, 50);
                } else {
                    mADialog.dismiss();
                }
            } else {
                ++mProgress;
                if(mProgress % 5 == 0) {
                    ++mCount;
                }
                mHandler.sendEmptyMessageDelayed(0, 50);
            }
        }
    };
}
