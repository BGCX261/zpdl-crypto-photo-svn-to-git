package com.zpdl.encryptionphoto.gridthumbnail;

import com.zpdl.encryptionphoto.R;
import android.view.View;
import android.widget.ImageView;

public class GridTnViewHolder {
    private View base;
    private ImageView thumbnail;

    private int  position;

    private long thumbnailId;
    private int  thumbnailSize;
    private int  thumbnailOrientation;
    private long thumbnailDateModified;
    /**
     * Constructor & view related functions
     */
    public GridTnViewHolder(View base) {
        this.base = base;
        this.thumbnail = null;

        this.position = 0;

        this.thumbnailId = 0;
        this.thumbnailSize = 0;
        this.thumbnailOrientation = 0;
        this.thumbnailDateModified = 0;
    }

    public ImageView getThumbnailView() {
        if(thumbnail == null) {
            thumbnail = (ImageView) base.findViewById(R.id.gridtn_item_tn);
        }
        return thumbnail;
    }
    /**
     * Set functions
     */
    public void setPosition(int position) {
        this.position = position;
    }

    public void setThumbnailId(long id) {
        thumbnailId = id;
    }

    public void setThumbnailOrientation(int orientation) {
        thumbnailOrientation = orientation;
    }

    public void setThumbnailSize(int size) {
        thumbnailSize = size;
    }

    public void setThumbnailDateModified(long dateModified) {
        thumbnailDateModified = dateModified;
    }
    /**
     * Get functions
     */
    public int getPosition() {
        return position;
    }

    public long getThumbnailId() {
        return thumbnailId;
    }

    public int getThumbnailOrientation() {
        return thumbnailOrientation;
    }

    public int getThumbnailSize() {
        return thumbnailSize;
    }

    public long getThumbnailDateModified() {
        return thumbnailDateModified;
    }
}