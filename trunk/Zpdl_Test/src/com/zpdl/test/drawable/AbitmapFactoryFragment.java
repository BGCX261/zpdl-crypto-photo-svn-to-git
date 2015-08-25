package com.zpdl.test.drawable;

import java.io.IOException;
import java.io.InputStream;

import com.zpdl.test.MainActivity;
import com.zpdl.test.R;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zpdl.api.drawable.AbitmapFactory;

public class AbitmapFactoryFragment extends Fragment {
    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static AbitmapFactoryFragment newInstance() {
        AbitmapFactoryFragment fragment = new AbitmapFactoryFragment();
        return fragment;
    }

    public AbitmapFactoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_abitmapfactory, container, false);

        ImageView iv_h = (ImageView)rootView.findViewById(R.id.abitmapfactory_source_horizontal);

        InputStream is_h = getActivity().getResources().openRawResource(R.raw.abitmapfactory_horizontal);
        Bitmap bm_h = BitmapFactory.decodeStream(is_h);
        try {
            is_h.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        iv_h.setImageBitmap(bm_h);

        ImageView iv_v = (ImageView)rootView.findViewById(R.id.abitmapfactory_source_vertical);

        InputStream is_v = getActivity().getResources().openRawResource(R.raw.abitmapfactory_vertical);
        Bitmap bm_v = BitmapFactory.decodeStream(is_v);
        try {
            is_v.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        iv_v.setImageBitmap(bm_v);

        createCenterCrop(rootView, bm_h, bm_v);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached("AbitmapFactory");
    }

    private void createCenterCrop(View view, Bitmap hbm, Bitmap vBm) {
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getActivity().getResources().getDisplayMetrics());

        ImageView iv_h = (ImageView)view.findViewById(R.id.abitmapfactory_centercrop_horizontal);
        Bitmap bm_h = AbitmapFactory.ScaledBitmap(hbm, AbitmapFactory.ScaleType.CENTER_CROP, size, size);
        iv_h.setImageBitmap(bm_h);
//        iv_h.setImageResource(R.drawable.gridth_item_background);

        ImageView iv_v = (ImageView)view.findViewById(R.id.abitmapfactory_centercrop_vertical);
        Bitmap bm_v = AbitmapFactory.ScaledBitmap(vBm, AbitmapFactory.ScaleType.CENTER_CROP, size, size);
        iv_v.setImageBitmap(bm_v);
    }
}
