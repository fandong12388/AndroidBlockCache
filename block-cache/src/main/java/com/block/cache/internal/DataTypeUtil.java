package com.block.cache.internal;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;

/**
 * Created by fandong
 * created on: 2019/3/1
 * description: 数据转换工具类
 */
public final class DataTypeUtil {

    private DataTypeUtil() {

    }

    public static byte[] bitmap2Bytes(Bitmap bmp) {
        if (bmp == null) {
            return null;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Drawable bytes2Drawable(byte[] buffer) {
        if (null == buffer || buffer.length == 0) {
            return null;
        }
        return bitmapToDrawable(bytes2Bitmap(buffer));

    }

    public static Bitmap bytes2Bitmap(byte[] buffer) {
        if (null == buffer || buffer.length == 0) {
            return null;
        }
        return BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
    }

    private static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }


    private static Drawable bitmapToDrawable(Bitmap bitmap) {
        return new BitmapDrawable(bitmap);
    }

    public static byte[] drawable2Bytes(Drawable drawable) {
        if (null == drawable) {
            return null;
        }
        return bitmap2Bytes(drawable2Bitmap(drawable));
    }

}
