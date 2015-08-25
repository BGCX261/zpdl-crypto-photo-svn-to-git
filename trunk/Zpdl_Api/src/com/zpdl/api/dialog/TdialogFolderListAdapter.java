package com.zpdl.api.dialog;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zpdl.api.R;
import com.zpdl.api.util.Alog;

public class TdialogFolderListAdapter extends ArrayAdapter<String> {

    public TdialogFolderListAdapter(Context context, ArrayList<String> objects) {
        super(context, 0, objects);
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if(view == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            view = inflater.inflate(R.layout.tdialog_folderlist_row, null);
            view.setTag(new ViewHolder(view));
        }

        final String folderPath = this.getItem(position);
        Alog.i("TdialogFolderList folderPath = %s", folderPath);
        if(folderPath != null) {
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            viewHolder.getIconView().setImageResource(R.drawable.ic_tdialog_folerlist_foler);
            viewHolder.getFolderNameView().setText(new File(folderPath).getName());
        }
        return view;
    }

    private class ViewHolder {
        private View base;
        private ImageView ic;
        private TextView tv;

        public ViewHolder(View base) {
            this.base = base;
            this.ic = null;
            this.tv = null;
        }

        public ImageView getIconView() {
            if(ic == null) {
                ic = (ImageView) base.findViewById(R.id.tdialog_folderlist_row_iv);
            }
            return ic;
        }

        public TextView getFolderNameView() {
            if(tv == null) {
                tv = (TextView) base.findViewById(R.id.tdialog_folderlist_row_tv);
            }
            return tv;
        }
    }
}
