package com.block.cache.internal.task;

import android.os.AsyncTask;

import com.block.cache.ICacheListener;
import com.block.cache.internal.Cache;
import com.block.cache.internal.CacheUtil;
import com.block.cache.internal.LiteCache;

/**
 * Created by fandong
 * created on: 2018/10/18
 * description:
 */
public abstract class CacheReadTask<T> extends AsyncTask<Void, Void, byte[]> {
    protected static final String UTF_8 = "UTF-8";
    protected ICacheListener<T> mListener;
    protected String block;
    protected String key;
    protected T defaultValue;

    public CacheReadTask() {
    }

    public CacheReadTask(String block, String key, T defaultValue) {
        this.block = block;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public void on(ICacheListener<T> listener) {
        this.mListener = listener;
        LiteCache.getInstance().get(this);
    }

    @Override
    protected byte[] doInBackground(Void... caches) {
        byte[] buffer = CacheUtil.cacheFromDisk(LiteCache.getInstance().getRootPath(), block, key);
        if (null != LiteCache.getInstance().getEncyptInterceptor()) {
            return LiteCache.getInstance().getEncyptInterceptor().decypt(buffer);
        }
        return buffer;
    }

    public String getBlock() {
        return block;
    }


    public String getKey() {
        return key;
    }


    @Override
    protected void onPostExecute(byte[] bytes) {
        postExecute(new Cache<T>(block, key, bytes, false));
    }

    public abstract void postExecute(Cache<T> cache);

}
