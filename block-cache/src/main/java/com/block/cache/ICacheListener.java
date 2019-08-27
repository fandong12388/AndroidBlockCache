package com.block.cache;

/**
 * Created by fandong
 * created on: 2018/10/19
 * description:
 */
public interface ICacheListener<T> {

    void onFinish(T t);
}
