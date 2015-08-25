package com.zpdl.encryptionphoto.griddecrypto;

import com.zpdl.encryptionphoto.R;
import android.view.View;
import android.widget.ImageView;

public class GridDecryptoViewHolder {
    private View base;
    private ImageView thumbnail;

    private int  position;

    private String encryptedPath;
    /**
     * Constructor & view related functions
     */
    public GridDecryptoViewHolder(View base) {
        this.base = base;
        this.thumbnail = null;

        this.position = 0;

        this.encryptedPath = null;
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

    public void setEncryptedPath(String path) {
        encryptedPath = path;
    }
    /**
     * Get functions
     */
    public int getPosition() {
        return position;
    }

    public String getEncryptedPath() {
        return encryptedPath;
    }
}