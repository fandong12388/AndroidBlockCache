package com.block.cache.internal.task;

import android.text.TextUtils;

import com.block.cache.internal.Cache;

import java.io.UnsupportedEncodingException;

/**
 * Created by fandong
 * created on: 2018/10/19
 * description:
 */
public class CacheReadStringTask extends CacheReadTask<String> {

    public CacheReadStringTask(String block, String key, String defaultValue) {
        super(block, key, defaultValue);
    }

    @Override
    public void postExecute(Cache<String> cache) {
        if (null == mListener) {
            return;
        }
        String value = null;
        try {
            if (null != cache && null != cache.getValue() && cache.getValue().length > 0) {
                value = new String(cache.getValue(), UTF_8);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(value)) {
            finishDefaultValue();
        } else {
            mListener.onFinish(value);
        }
    }

    private void finishDefaultValue() {
        if (null == mListener) {
            return;
        }
        if (null != defaultValue) {
            mListener.onFinish(defaultValue);
        } else {
            mListener.onFinish(null);
        }
    }
}
