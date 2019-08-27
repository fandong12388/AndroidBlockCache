package com.block.cache.internal.task;

import android.text.TextUtils;

import com.block.cache.internal.Cache;

import java.io.UnsupportedEncodingException;

/**
 * Created by fandong
 * created on: 2018/10/19
 * description:
 */
public class CacheReadBooleanTask extends CacheReadTask<Boolean> {
    private static final Boolean DEFAULT_VALUE = Boolean.FALSE;

    public CacheReadBooleanTask(String block, String key, Boolean defaultValue) {
        super(block, key, defaultValue);
    }

    @Override
    public void postExecute(Cache<Boolean> cache) {
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
            try {
                mListener.onFinish(Boolean.valueOf(value));
            } catch (Exception e) {
                e.printStackTrace();
                finishDefaultValue();
            }
        }
    }

    private void finishDefaultValue() {
        if (null == mListener) {
            return;
        }
        if (null != defaultValue) {
            mListener.onFinish(defaultValue);
        } else {
            mListener.onFinish(DEFAULT_VALUE);
        }
    }
}
