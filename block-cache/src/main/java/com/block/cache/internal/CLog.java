package com.block.cache.internal;

import android.util.Log;

import com.block.cache.BlockCache;


/**
 * Created by fandong
 * created on: 2018/10/18
 * description:
 */
public final class CLog {
    private CLog() {
    }

    public static void error(String tag, String log) {
        if (BlockCache.isDebug()) {
            Log.e(tag, log);
        }
    }

    public static void debug(String tag, String log) {
        if (BlockCache.isDebug()) {
            Log.d(tag, log);
        }
    }

    public static void info(String tag, String log) {
        if (BlockCache.isDebug()) {
            Log.i(tag, log);
        }
    }
}
