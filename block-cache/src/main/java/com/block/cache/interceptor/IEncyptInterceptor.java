package com.block.cache.interceptor;

/**
 * Created by fandong
 * created on: 2018/12/28
 * description:
 */
public interface IEncyptInterceptor{

    byte[] encypt(byte[] buffer);

    byte[] decypt(byte[] buffer);
}
