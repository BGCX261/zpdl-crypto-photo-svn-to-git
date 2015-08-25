package com.zpdl.encryptionphoto.gridthumbnail;

import com.zpdl.encryptionphoto.cache.TnParam;
import com.zpdl.encryptionphoto.gridthumbnail.GridTnLoadThumbnail.GridTnLoadThumbnailListener;

import com.zpdl.encryptionphoto.R;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class GridTnAdapter extends CursorAdapter implements GridTnLoadThumbnailListener {
    private GridTnLoadThumbnail mGridTnLoadThumbnail;
    private int mTnSize;

    public GridTnAdapter(Context context, Cursor c) {
        super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);

        mGridTnLoadThumbnail = new GridTnLoadThumbnail(context.getContentResolver(), this);
        mTnSize = context.getResources().getDimensionPixelSize(R.dimen.gridtn_item_size);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.gridtn_item, null);
        view.setTag(new GridTnViewHolder(view));

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
        long dateModified = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
        int orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));

        GridTnViewHolder viewHolder = (GridTnViewHolder) view.getTag();
        viewHolder.setPosition(cursor.getPosition());
        viewHolder.setThumbnailId(id);
        viewHolder.setThumbnailOrientation(orientation);
        viewHolder.setThumbnailSize(mTnSize);
        viewHolder.setThumbnailDateModified(dateModified);

        viewHolder.getThumbnailView().setImageBitmap(null);

        mGridTnLoadThumbnail.loadThumbnail(viewHolder);
    }

    @Override
    public void onLoadThumbnail(TnParam p, GridTnViewHolder h) {
        if(!p.isError) {
            h.getThumbnailView().setRotation(p.orientation);
            h.getThumbnailView().setImageBitmap(p.thumbnail);
        }
    }
}
