package com.zpdl.encryptionphoto.griddecrypto;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;

import com.zpdl.api.file.AFileUtil;
import com.zpdl.api.util.Alog;
import com.zpdl.encryptionphoto.cache.TnCache;
import com.zpdl.encryptionphoto.cache.TnParam;
import com.zpdl.encryptionphoto.crypto.CryptoExceptionCertification;
import com.zpdl.encryptionphoto.crypto.CryptoExceptionError;
import com.zpdl.encryptionphoto.crypto.DecryptoARunnable;

public class GridDecryptoThumbnail {
    private TnCache                             mThumbnailCache;

    private LinkedList<GridDecryptoViewHolder>  mQueue;
    private ExecutorService                     mExecutor;

    private GridDecryptoThumbnailListener       mListener;

    private Handler mHandler = new Handler();

    public interface GridDecryptoThumbnailListener {
        void onDecryptoThumbnail(TnParam p, GridDecryptoViewHolder h);
    }

    public GridDecryptoThumbnail(GridDecryptoThumbnailListener l) {
        mThumbnailCache = TnCache.getInstance();

        mQueue = new LinkedList<GridDecryptoViewHolder>();
        mExecutor = Executors.newSingleThreadExecutor();

        mListener = l;
    }

    public void decryptoThumbnail(final GridDecryptoViewHolder holder) {
        TnParam tnParam = mThumbnailCache.get(_generateKey(holder.getEncryptedPath()));

        if(tnParam != null) {
            if(mListener != null) {
                mListener.onDecryptoThumbnail(tnParam, holder);
            }
            return;
        }

        synchronized (mQueue) {
            boolean skip = false;

            for(GridDecryptoViewHolder h : mQueue) {
                if(h.equals(holder)) {
                    skip = true;
                    Alog.d("GridDecryptoThumbnail : loadTn : skip");
                    break;
                }
            }

            if(!skip) {
                mQueue.add(holder);
                if(mQueue.size() == 1) {
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            decryptoThumbnail();
                        }
                    });
                }
            }
        }
    }

    private void decryptoThumbnail() {
        while(true) {
            GridDecryptoViewHolder holder;
            String encryptedPath = null;

            synchronized (mQueue) {
                if(mQueue.size() > 0) {
                    int index = 0;
                    for(int i = 0, position = Integer.MAX_VALUE; i < mQueue.size(); i++) {
                        GridDecryptoViewHolder h = mQueue.get(i);
                        if(h.getPosition() < position) {
                            position = h.getPosition();
                            index = i;
                        }
                    }
                    holder = mQueue.remove(index);
                    encryptedPath = holder.getEncryptedPath();
                } else {
                    break;
                }
            }

            TnParam tnParam = null;
            try {
                tnParam = DecryptoARunnable.decryptoThumbnail(encryptedPath);
            } catch (CryptoExceptionCertification e) {
                e.printStackTrace();
                tnParam = new TnParam();
                tnParam.isError = true;
            } catch (CryptoExceptionError e) {
                e.printStackTrace();
                tnParam = new TnParam();
                tnParam.isError = true;
            }

            mThumbnailCache.put(_generateKey(encryptedPath), tnParam);

            if(mListener != null) {
                mHandler.post(new PostRunnable(encryptedPath, tnParam, holder));
            }
        }
    }

    private String _generateKey(String encryptedPath) {
        return "d_"+AFileUtil.getFileName(encryptedPath);
    }

    private class PostRunnable implements Runnable {
        String encryptedPath;
        TnParam tnParam;
        GridDecryptoViewHolder holder;

        public PostRunnable(String path, TnParam p, GridDecryptoViewHolder h) {
            encryptedPath = path;
            tnParam = p;
            holder = h;
        }

        @Override
        public void run() {
            if(encryptedPath.equals(holder.getEncryptedPath())) {
                mListener.onDecryptoThumbnail(tnParam, holder);
            }
        }

    }
}
