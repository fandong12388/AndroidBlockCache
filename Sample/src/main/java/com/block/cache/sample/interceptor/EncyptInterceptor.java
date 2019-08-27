package com.block.cache.sample.interceptor;

import android.util.Base64;

import com.block.cache.interceptor.IEncyptInterceptor;


/**
 * Created by fandong
 * created on: 2018/12/28
 * description:
 */
public class EncyptInterceptor implements IEncyptInterceptor {
    @Override
    public byte[] encypt(byte[] buffer) {
        if (null == buffer || buffer.length == 0) {
            return buffer;
        }
        return Base64.encode(buffer, 0, buffer.length, Base64.NO_WRAP);
    }

    @Override
    public byte[] decypt(byte[] buffer) {
        if (null == buffer || buffer.length == 0) {
            return buffer;
        }
        return Base64.decode(buffer, 0, buffer.length, Base64.NO_WRAP);
    }
}
