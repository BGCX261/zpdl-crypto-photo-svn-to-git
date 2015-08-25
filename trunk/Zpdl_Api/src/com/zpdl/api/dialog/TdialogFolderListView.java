package com.zpdl.api.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ListView;

import com.zpdl.api.R;
import com.zpdl.api.file.AFileUtil;

public class TdialogFolderListView extends ListView {
    public static final String UP = "..";

    private ArrayList<String>           mList;
    private TdialogFolderListAdapter    mAdapter;

    private String                      mPath;

    public TdialogFolderListView(Context context) {
        this(context, null);
    }

    public TdialogFolderListView(Context context, AttributeSet attrs) {
        this(context, attrs , android.R.attr.absListViewStyle);
    }

    public TdialogFolderListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mList       = null;
        mAdapter    = null;

        mPath       = null;

        mList = new ArrayList<String>();
        mAdapter = new TdialogFolderListAdapter(getContext(), mList);

        this.setAdapter(mAdapter);
        this.setSelector(R.drawable.tdialog_btn_selector);
    }

    public String getPath() {
        return mPath;
    }

    public String init(String path) {
        mPath = path;

        mList.clear();

        if(mPath == null || !AFileUtil.getInstance().isSdCard(path)) {
            mPath = AFileUtil.getInstance().getSdCard();
        }
//        if(!mPath.equals(AFileUtil.getInstance().getSdCard())) {
//            mList.add(UP);
//        }

        ArrayList<String> folderList = new ArrayList<String>();
        ArrayList<String> fileList = new ArrayList<String>();
//        Alog.d("TdialogFolderListView mPath = %s" , mPath);
        File[] files = new File(mPath).listFiles();

        if(files != null) {
            for(File file: files) {
                if(file.isHidden()) {
                    continue;
                } else if(file.isDirectory()) {
                    folderList.add(file.getPath());
                } else if(file.isFile()) {
                    fileList.add(file.getPath());
                }
            }
        }

        if(folderList.size() > 0) {
            Collections.sort(folderList, String.CASE_INSENSITIVE_ORDER);
            for(String folder : folderList) {
                mList.add(folder);
            }
        }

        mAdapter.notifyDataSetChanged();
        return mPath;
    }

    public void newfolder(String path) {
        mList.add(path);
        mAdapter.notifyDataSetChanged();
    }

    public boolean delete(String path) {
        boolean result = mList.remove(path);
        mAdapter.notifyDataSetChanged();
        return result;
    }

    public boolean rename(String in, String out) {
        for(int i = 0; i < mList.size(); i++) {
            if(mList.get(i).equals(in)) {
                mList.set(i, out);
                mAdapter.notifyDataSetChanged();
                return true;
            }
        }
        return false;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int cWidthMeasureSpec = widthMeasureSpec;
        int cHeightMeasureSpec = heightMeasureSpec;

        ViewGroup.LayoutParams p = this.getLayoutParams();

        if(p.width == LayoutParams.MATCH_PARENT) {
            cWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                    MeasureSpec.getSize(widthMeasureSpec),
                    MeasureSpec.EXACTLY);
        }
        if(p.height == LayoutParams.MATCH_PARENT) {
            cHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    MeasureSpec.getSize(heightMeasureSpec),
                    MeasureSpec.EXACTLY);
        }

        super.onMeasure(cWidthMeasureSpec, cHeightMeasureSpec);
    }
}
