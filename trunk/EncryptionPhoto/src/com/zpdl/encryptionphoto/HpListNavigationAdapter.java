package com.zpdl.encryptionphoto;

import java.io.File;
import java.util.ArrayList;

import com.zpdl.encryptionphoto.R;
import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class HpListNavigationAdapter extends ArrayAdapter<String> {

    public static final String HIDDEN_PHOTO = "Hidden Photo";

    private static final String INTERNAL_SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();

    private LayoutInflater mInflater;
    private Resources mResources;

    public static ArrayList<String> createArrayList(Context c) {
        ArrayList<String> al = new ArrayList<String>();
        al.add(c.getResources().getString(R.string.title_Encrypted_Photo));

        return al;
    }

    public HpListNavigationAdapter(Context context, ArrayList<String> objects) {
        super(context, 0, objects);

        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResources = context.getResources();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv;

        if (convertView == null) {
            tv = (TextView) mInflater.inflate(R.layout.hp_list_navigation_drawer_row, parent, false);
        } else {
            tv = (TextView) convertView;
        }

        String item = getItem(position);

        if(position == 0) {
            tv.setText(item);
            tv.setCompoundDrawablesRelativeWithIntrinsicBounds(mResources.getDrawable(R.drawable.ic_navigation_secure), null, null, null);
        } else {
            File dirFile = new File(item);

            tv.setText(dirFile.getName());
            if(dirFile.getAbsoluteFile().getAbsolutePath().startsWith(INTERNAL_SDCARD)) {
                tv.setCompoundDrawablesRelativeWithIntrinsicBounds(mResources.getDrawable(R.drawable.ic_navigation_phone), null, null, null);
            } else {
                tv.setCompoundDrawablesRelativeWithIntrinsicBounds(mResources.getDrawable(R.drawable.ic_navigation_sdcard), null, null, null);
            }
        }

        return tv;
    }


}
