package com.block.cache.internal;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fandong
 * created on: 2018/10/18
 * description:
 */
public class Cache<T> extends AtomicBoolean {
    private String block;
    private String key;
    private byte[] value;
    //用于读取的时候
    private T defaultValue;
    private T objValue;
    //是否是保存(否则为删除)
    private boolean isSave;

    public Cache() {
    }


    public Cache(String block, String key, boolean isSave) {
        this.block = block;
        this.isSave = isSave;
        this.key = key;
    }

    public Cache(String block, String key, byte[] value, boolean isSave) {
        this.block = block;
        this.key = key;
        this.value = value;
        this.isSave = isSave;
    }

    public Cache(String block, String key, T value, boolean isSave) {
        this.block = block;
        this.key = key;
        this.objValue = value;
        this.isSave = isSave;
    }


    public Cache(String block, String key, T defaultValue) {
        this.block = block;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public T getObjValue() {
        return objValue;
    }

    public void setObjValue(T objValue) {
        this.objValue = objValue;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public boolean isSave() {
        return isSave;
    }

    public void setSave(boolean save) {
        isSave = save;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }
}
