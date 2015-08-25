package com.zpdl.encryptionphoto.cache;

import android.util.LruCache;


public class TnCache {
    private static final int MAX = 32;

    private volatile static TnCache uniqueInstance;
    private LruCache<String, TnParam> lruCache;

    public static TnCache getInstance() {
        if(uniqueInstance == null) {
            synchronized (TnCache.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new TnCache();
                }
            }
        }

        return uniqueInstance;
    }

    private TnCache() {
        lruCache = new LruCache<String, TnParam>(MAX)
//                {
//            @Override
//            protected void entryRemoved(boolean evicted, String key, TnParam oldValue, TnParam newValue) {
//                oldValue.getThumbnail().recycle();
//            };
//        }
                ;
    }

    public TnParam get(String key) {
        return lruCache.get(key);
    }

    public void put(String key, TnParam bm) {
        lruCache.put(key, bm);
    }
}
