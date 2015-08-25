package com.zpdl.encryptionphoto.gridthumbnail;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.os.Handler;
import android.provider.MediaStore.Images;

import com.zpdl.api.drawable.AbitmapFactory;
import com.zpdl.api.drawable.AbitmapFactory.ScaleType;
import com.zpdl.api.util.Alog;
import com.zpdl.encryptionphoto.cache.TnCache;
import com.zpdl.encryptionphoto.cache.TnParam;

public class GridTnLoadThumbnail {
    private static final int        TN_KIND = Images.Thumbnails.MINI_KIND;
    private static final ScaleType  SCALE = AbitmapFactory.ScaleType.CENTER_CROP;

    private TnCache mThumbnailCache;

    private LinkedList<GridTnViewHolder> mQueue;
    private ExecutorService mExecutor;

    private ContentResolver mContentResolver;
    private GridTnLoadThumbnailListener mListener;
    private Handler mHandler = new Handler();

    public interface GridTnLoadThumbnailListener {
        void onLoadThumbnail(TnParam p, GridTnViewHolder h);
    }
	
    public GridTnLoadThumbnail(ContentResolver cr, GridTnLoadThumbnailListener l) {
        mThumbnailCache = TnCache.getInstance();

        mQueue = new LinkedList<GridTnViewHolder>();
        mExecutor = Executors.newSingleThreadExecutor();

        mContentResolver = cr;
        mListener = l;
    }

    public void loadThumbnail(final GridTnViewHolder holder) {
        TnParam tnParam = mThumbnailCache.get(_generateKey(holder.getThumbnailId(), holder.getThumbnailDateModified()));

        if(tnParam != null) {
            if(mListener != null) {
                mListener.onLoadThumbnail(tnParam, holder);
            }
            return;
        }

        synchronized (mQueue) {
            boolean skip = false;

            for(GridTnViewHolder h : mQueue) {
                if(h.equals(holder)) {
                    skip = true;
                    Alog.i("GridTnLoadTn : loadTn : skip");
                    break;
                }
            }

            if(!skip) {
                mQueue.add(holder);
                if(mQueue.size() == 1) {
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            loadThumbnail();
                        }
                    });
                }
            }
        }
    }

    private void loadThumbnail() {
        while(true) {
            GridTnViewHolder holder;
            long id = 0;
            int size = 0;
            int orientation = 0;
            long dateModified = 0;

            synchronized (mQueue) {
                if(mQueue.size() > 0) {
                    int index = 0;
                    for(int i = 0, position = Integer.MAX_VALUE; i < mQueue.size(); i++) {
                        GridTnViewHolder h = mQueue.get(i);
                        if(h.getPosition() < position) {
                            position = h.getPosition();
                            index = i;
                        }
                    }
                    holder = mQueue.remove(index);
                    id = holder.getThumbnailId();
                    size = holder.getThumbnailSize();
                    orientation = holder.getThumbnailOrientation();
                    dateModified = holder.getThumbnailDateModified();
                } else {
                    break;
                }
            }

            Bitmap bm = AbitmapFactory.ScaledBitmap(Images.Thumbnails.getThumbnail(mContentResolver,
                                                                                   id,
                                                                                   TN_KIND,
                                                                                   null),
                                                    SCALE,
                                                    size,
                                                    size);
            TnParam tnParam = new TnParam();
            if(bm == null) {
                tnParam.isError = true;
            } else {
                tnParam.thumbnail = bm;
                tnParam.orientation = orientation;
            }
            mThumbnailCache.put(_generateKey(id, dateModified), tnParam);

            if(mListener != null) {
                mHandler.post(new PostRunnable(id, tnParam, holder));
            }
        }
    }

    private String _generateKey(long id, long dateModified) {
        return String.format("t_%d%d", dateModified, id);
    }

    private class PostRunnable implements Runnable {
        long mId;
        TnParam tnParam;
        GridTnViewHolder holder;

        public PostRunnable(long id, TnParam p, GridTnViewHolder h) {
            mId = id;
            tnParam = p;
            holder = h;
        }

        @Override
        public void run() {
            if(mId == holder.getThumbnailId()) {
                mListener.onLoadThumbnail(tnParam, holder);
            }
        }

    }
}
