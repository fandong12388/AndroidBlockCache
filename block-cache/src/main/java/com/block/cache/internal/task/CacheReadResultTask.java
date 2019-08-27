package com.block.cache.internal.task;

import com.block.cache.internal.Cache;
import com.block.cache.internal.LiteCache;

/**
 * Created by fandong
 * created on: 2018/10/19
 * description:
 */
public class CacheReadResultTask<Result> extends CacheReadTask<Result> {

    private Class<Result> resultClass;

    public CacheReadResultTask(Class<Result> resultClass, String block, String key) {
        super(block, key, null);
        this.resultClass = resultClass;
    }

    @Override
    public void postExecute(Cache<Result> cache) {
        if (null == mListener) {
            return;
        }
        if (null != cache.getObjValue()) {
            mListener.onFinish(cache.getObjValue());
            return;
        }
        Result value = LiteCache.getInstance().getSerializeInterceptor().deSerialize(cache.getValue(), resultClass);
        if (null == value) {
            mListener.onFinish(null);
        } else {
            mListener.onFinish(value);
        }
    }
}
