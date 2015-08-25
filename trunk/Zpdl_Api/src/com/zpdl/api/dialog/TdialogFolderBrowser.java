package com.zpdl.api.dialog;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.zpdl.api.R;
import com.zpdl.api.file.AFileScanner;
import com.zpdl.api.file.AFileUtil;

public class TdialogFolderBrowser extends Tdialog implements ContextDialogListener, AdialogListener {
    private static final String ARG_FRAGMENT_TAG    = "TdialogFolderBrowser";

    private static final String ARG_PATH            = "TdialogFolderBrowser_path";
    private static final String ARG_PARAM           = "TdialogFolderBrowser_param";

    private static final int CONTEXTMENU_ID_RENAME  = 0x0001;
    private static final int CONTEXTMENU_ID_DELETE  = 0x0002;

    private static final int DIALOG_ID_NEW_FOLDER   = 0x1001;
    private static final int DIALOG_ID_RENAME       = 0x1002;
    private static final int DIALOG_ID_RENAME_TASK  = 0x1003;
    private static final int DIALOG_ID_DELETE       = 0x1004;

    private TdialogFolderListView mTdialogFolderListView;
    private TextView              mFolderNameView;
    private String mPath;

    public TdialogFolderBrowser() {
        super();

        mPath = null;
        mTdialogFolderListView = null;
    }

    public TdialogFolderBrowser(int id) {
        this(id, null);
    }

    public TdialogFolderBrowser(int id, String callingfragmentTag) {
        super(id, callingfragmentTag);

        mPath = null;
        mTdialogFolderListView = null;
    }

    public void show(FragmentManager fm) {
        _show(fm, ARG_FRAGMENT_TAG);
    }

    public void setPath(String path) {
        if(path == null) {
            mPath = AFileUtil.getInstance().getSdCard();
        } else {
            mPath = path;
        }

        if(mTdialogFolderListView != null) {
            mTdialogFolderListView.init(mPath);
        }
    }

    public void setParam(String[] param) {
        getArguments().putStringArray(ARG_PARAM, param);
    }

    public String getPath() {
        return mTdialogFolderListView == null ? mPath : mTdialogFolderListView.getPath();
    }

    public String[] getParam() {
        return getArguments().getStringArray(ARG_PARAM);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mPath  = savedInstanceState.getString(ARG_PATH, AFileUtil.getInstance().getSdCard());
        }
    }

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateView(Context context, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(context).inflate(R.layout.tdialog_folderbrowser, null);

        mTdialogFolderListView = (TdialogFolderListView) view.findViewById(R.id.tdialog_folderbrowser_folderbrowser);
        mTdialogFolderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPath = (String) parent.getItemAtPosition(position);
                _requestFolderChange();
            }
        });
        mTdialogFolderListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ContextDialog dialog = new ContextDialog((String) parent.getItemAtPosition(position), getTag());
                dialog.show(getFragmentManager());
                return true;
            }
        });
        mFolderNameView = (TextView) view.findViewById(R.id.tdialog_folderbrowser_foldername);
        mFolderNameView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        mFolderNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getPath().equals(AFileUtil.getInstance().getSdCard())) {
                    mDialog.getDialog().cancel();
                } else {
                    mPath = new File(getPath()).getParent();
                    _requestFolderChange();
                }
            }
        });
        ImageButton ib = (ImageButton) view.findViewById(R.id.tdialog_folderbrowser_newfolder);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _newfolderCreateEditDialog(getActivity().getFragmentManager(), mPath, getTag());
            }
        });

        _requestFolderChange();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(ARG_PATH, getPath());
    }

    private void _requestFolderChange() {
        mPath = mTdialogFolderListView.init(mPath);
        if(mPath.equals(AFileUtil.getInstance().getSdCard())) {
            mFolderNameView.setText(R.string.Cancel);
        } else {
            mFolderNameView.setText(AFileUtil.getInstance().getSeparateFileString(mPath));
        }
    }

    @Override
    public void onCreateContextDialog(ArrayList<ContextDialogMenu> menu, String selectedItem) {
        if(AFileUtil.getInstance().isSdCard(selectedItem)) {
            menu.add(new ContextDialogMenu(CONTEXTMENU_ID_RENAME, CONTEXTMENU_ID_RENAME, getString(R.string.Rename)));
            menu.add(new ContextDialogMenu(CONTEXTMENU_ID_DELETE, CONTEXTMENU_ID_DELETE, getString(R.string.Delete)));
        }
    }

    @Override
    public void onContextDialogItemSelected(int id, String selectedItem) {
        if(id == CONTEXTMENU_ID_RENAME) {
            _renameCreateEditDialog(getFragmentManager(), selectedItem, getTag());
        } else if(id == CONTEXTMENU_ID_DELETE) {
            _deleteCreateDeleteDialog(getFragmentManager(), new String[] { selectedItem }, getTag());
        }
    }

    @Override
    public void onDialogAttach(Adialog dialog) {

    }

    @Override
    public void onDialogDetach(Adialog dialog) {

    }

    @Override
    public void onDialogEvent(Adialog dialog, int which) {
        if(dialog.getDialogId() == DIALOG_ID_NEW_FOLDER) {
            if(which == Adialog.EVENT_POSITIVE) {
                _newfolderPositiveDialogButton((TdialogFileEdit) dialog);
            }
            dialog.dismiss();
        } else if(dialog.getDialogId() == DIALOG_ID_RENAME) {
            if(which == Adialog.EVENT_POSITIVE) {
                _renamePositiveDialogButton((TdialogFileEdit) dialog);
            }
            dialog.dismiss();
        } else if(dialog.getDialogId() == DIALOG_ID_DELETE) {
            if(which == TdialogDelete.EVENT_DELETE_DONE) {
                _deleteDone((TdialogDelete) dialog);
            } else {
                dialog.dismiss();
            }
        }
    }

    private void _newfolderCreateEditDialog(FragmentManager fm, String parent, String tag) {
        TdialogFileEdit dialog = new TdialogFileEdit(DIALOG_ID_NEW_FOLDER, getTag());
        dialog.setTitle(getString(R.string.New_folder));
        dialog.setHint(getString(R.string.Input_new_folder_name));
        dialog.setParentPath(parent);
        dialog.setPositiveButton(getString(R.string.Ok));
        dialog.setNegativeButton(getString(R.string.Cancel));
        dialog.show(fm);
    }

    private void _newfolderPositiveDialogButton(TdialogFileEdit dialog) {
        final String newfolder = dialog.getPath();
        int result = AFileUtil.getInstance().createDirectory(newfolder);
        if(result == 0) {
            Toast.makeText(getActivity(), R.string.Exist_folder , Toast.LENGTH_SHORT).show();
        } else if(result == 1) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(newfolder)));
            getActivity().sendBroadcast(intent);

            mTdialogFolderListView.newfolder(newfolder);
            mTdialogFolderListView.smoothScrollToPosition(mTdialogFolderListView.getCount());
        } else {
            Toast.makeText(getActivity(), R.string.Success , Toast.LENGTH_SHORT).show();
        }
    }

    private void _renameCreateEditDialog(FragmentManager fm, String path, String tag) {
        TdialogFileEdit dialog = new TdialogFileEdit(DIALOG_ID_RENAME, getTag());
        dialog.setTitle(getString(R.string.Rename));
        dialog.setHint(getString(R.string.Input_new_name));
        dialog.setParentPath(new File(path).getParent());
        dialog.setFileName(new File(path).getName());
        dialog.setPositiveButton(getString(R.string.Ok));
        dialog.setNegativeButton(getString(R.string.Cancel));
        dialog.show(fm);
    }

    private Handler mRenameHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0x1101)  {
                Toast.makeText(getActivity(), R.string.Success , Toast.LENGTH_SHORT).show();
                String[] path = (String[]) msg.obj;
                mTdialogFolderListView.rename(path[0], path[1]);
            } else if(msg.what == 0x1102) {
                Toast.makeText(getActivity(), R.string.Fail , Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void _renamePositiveDialogButton(TdialogFileEdit dialog) {
        final String source = dialog.getOriginalPath();
        final String target = dialog.getPath();
        TdialogTask dialogTask = new TdialogTask(DIALOG_ID_RENAME_TASK, getTag());
        dialogTask.setRunnable(new TdialogRunnable() {
            @Override
            public void runnable() {
                File sourcefile = new File(source);
                File targetfile = new File(target);

                if(sourcefile.renameTo(targetfile)) {
                    AFileScanner.renameFile(getActivity(), source, target);
                    Message msg = mRenameHandler.obtainMessage();
                    msg.what = 0x1101;
                    msg.obj = new String[] {source, target};
                    mRenameHandler.sendMessage(msg);
                } else {
                    mRenameHandler.sendEmptyMessage(0x1102);
                }
            }
        });
        dialogTask.show(getFragmentManager());
    }

    private void _deleteCreateDeleteDialog(FragmentManager fm, String[] deleteList, String tag) {
        TdialogDelete dialog = new TdialogDelete(DIALOG_ID_DELETE, getTag());
        dialog.setDeleteFileName(deleteList);
        dialog.setNegativeButton(getString(R.string.Cancel));
        dialog.show(fm);
    }

    private void _deleteDone(TdialogDelete dialog) {
        Toast.makeText(getActivity(), R.string.Success , Toast.LENGTH_SHORT).show();

        String[] deleted = dialog.getDeleteFileName();
        mTdialogFolderListView.delete(deleted[0]);
    }
}
