package com.block.cache.interceptor;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.block.cache.internal.DataTypeUtil;

/**
 * Created by fandong
 * created on: 2018/12/28
 * description:
 */
public abstract class ISerializeInterceptor {

    public byte[] serialize(Object object) {
        if (object instanceof Bitmap) {
            return DataTypeUtil.bitmap2Bytes((Bitmap) object);
        } else if (object instanceof Drawable) {
            return DataTypeUtil.drawable2Bytes((Drawable) object);
        }
        return doSerialize(object);
    }

    @SuppressWarnings("unchecked")
    public <T> T deSerialize(byte[] buffer, Class<T> clazz) {
        if (clazz == Bitmap.class) {
            return (T) DataTypeUtil.bytes2Bitmap(buffer);
        } else if (clazz == Drawable.class) {
            return (T) DataTypeUtil.bytes2Drawable(buffer);
        }
        return doDeserialize(buffer, clazz);
    }

    protected abstract byte[] doSerialize(Object object);


    protected abstract <T> T doDeserialize(byte[] buffer, Class<T> clazz);
}
