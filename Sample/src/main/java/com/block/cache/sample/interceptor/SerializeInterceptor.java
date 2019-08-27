package com.block.cache.sample.interceptor;


import android.text.TextUtils;

import com.block.cache.interceptor.ISerializeInterceptor;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;

/**
 * Created by fandong
 * created on: 2018/12/28
 * description:
 */
public class SerializeInterceptor extends ISerializeInterceptor {
    private static final String DEFAULT_CHARSET = "UTF-8";
    private Gson gson;

    public SerializeInterceptor() {
        this.gson = new Gson();
    }

    @Override
    protected byte[] doSerialize(Object object) {
        try {
            if (null == object) {
                return null;
            }
            String content;
            if (TextUtils.isEmpty(content = gson.toJson(object))) {
                return null;
            }
            return content.getBytes(DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException ignore) {
        }
        return null;
    }

    @Override
    protected <T> T doDeserialize(byte[] buffer, Class<T> clazz) {
        if (null != buffer && null != clazz) {
            try {
                return gson.fromJson(new String(buffer, DEFAULT_CHARSET), clazz);
            } catch (UnsupportedEncodingException ignore) {
            }
        }
        return null;
    }
}
