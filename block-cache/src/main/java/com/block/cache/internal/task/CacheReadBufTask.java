package com.block.cache.internal.task;

import com.block.cache.internal.Cache;

/**
 * Created by fandong
 * created on: 2018/10/19
 * description:
 */
public class CacheReadBufTask extends CacheReadTask<byte[]> {
    public CacheReadBufTask(String block, String key) {
        this.block = block;
        this.key = key;
    }

    @Override
    public void postExecute(Cache<byte[]> cache) {
        if (null == mListener) {
            return;
        }
        if (null != cache && null != cache.getValue() && cache.getValue().length > 0) {
            mListener.onFinish(cache.getValue());
            return;
        }
        mListener.onFinish(null);
    }

}
