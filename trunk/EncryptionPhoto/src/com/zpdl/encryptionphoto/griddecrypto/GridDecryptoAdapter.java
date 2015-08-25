package com.zpdl.encryptionphoto.griddecrypto;

import java.util.ArrayList;

import com.zpdl.encryptionphoto.cache.TnParam;
import com.zpdl.encryptionphoto.griddecrypto.GridDecryptoThumbnail.GridDecryptoThumbnailListener;

import com.zpdl.encryptionphoto.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class GridDecryptoAdapter extends ArrayAdapter<String> implements GridDecryptoThumbnailListener {
    private GridDecryptoThumbnail mGridDecryptoThumbnail;

    public GridDecryptoAdapter(Context context, ArrayList<String> objects) {
        super(context, 0, objects);

        mGridDecryptoThumbnail = new GridDecryptoThumbnail(this);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if(view == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            view = inflater.inflate(R.layout.gridtn_item, null);
            view.setTag(new GridDecryptoViewHolder(view));
        }

        final String encryptedPath = this.getItem(position);
        if(encryptedPath != null) {
            GridDecryptoViewHolder viewHolder = (GridDecryptoViewHolder) view.getTag();
            viewHolder.setPosition(position);
            viewHolder.setEncryptedPath(encryptedPath);

            viewHolder.getThumbnailView().setImageBitmap(null);

            mGridDecryptoThumbnail.decryptoThumbnail(viewHolder);
        }

        return view;
    }

    @Override
    public void onDecryptoThumbnail(TnParam p, GridDecryptoViewHolder h) {
        if(!p.isError) {
            h.getThumbnailView().setRotation(p.orientation);
            h.getThumbnailView().setImageBitmap(p.thumbnail);
        }
    }

}
