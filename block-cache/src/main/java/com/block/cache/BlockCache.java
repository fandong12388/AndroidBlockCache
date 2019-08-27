package com.block.cache;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.block.cache.interceptor.IEncyptInterceptor;
import com.block.cache.interceptor.ISerializeInterceptor;
import com.block.cache.internal.LiteCache;
import com.block.cache.internal.task.CacheReadBooleanTask;
import com.block.cache.internal.task.CacheReadBufTask;
import com.block.cache.internal.task.CacheReadDoubleTask;
import com.block.cache.internal.task.CacheReadIntTask;
import com.block.cache.internal.task.CacheReadLongTask;
import com.block.cache.internal.task.CacheReadResultTask;
import com.block.cache.internal.task.CacheReadStringTask;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by fandong
 * created on: 2018/10/18
 * description:美团轻量级的缓存框架
 */
public class BlockCache {
    //是否是测试环境
    private static boolean sDebug = false;
    // 默认的扇区 
    private static String sDefaultBlock = "default";

    public static Initializer initializer(Context context) {
        return new Initializer(context);
    }


    //---------------------数据存储开始------------------------------------
    public static void putBytes(String block, String key, byte[] value) {
        LiteCache.getInstance().put(block, key, value);
    }

    public static void putInt(String block, String key, Integer value) {
        putString(block, key, String.valueOf(value));
    }

    public static void putLong(String block, String key, Long value) {
        putString(block, key, String.valueOf(value));
    }

    public static void putBool(String block, String key, Boolean value) {
        putString(block, key, String.valueOf(value));
    }

    public static void putDouble(String block, String key, Double value) {
        putString(block, key, String.valueOf(value));
    }

    public static void putString(String block, String key, String value) {
        try {
            if (TextUtils.isEmpty(value)) {
                return;
            }
            putBytes(block, key, value.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static <T> void put(String block, String key, T t) {
        LiteCache.getInstance().put(block, key, t);
    }


    //---------------------数据存储结束------------------------------------
    //---------------------数据同步读取开始---------------------------------
    public static byte[] getBytes(String block, String key) {
        return LiteCache.getInstance().get(block, key);
    }

    public static boolean getBool(String block, String key) {
        return getBool(block, key, Boolean.FALSE);
    }

    public static boolean getBool(String block, String key, Boolean defaultValue) {
        String value = getString(block, key);
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Boolean.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public static int getInt(String block, String key) {
        return getInt(block, key, 0);
    }

    public static int getInt(String block, String key, Integer defaultValue) {
        String value = getString(block, key);
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public static long getLong(String block, String key) {
        return getLong(block, key, 0L);
    }

    public static long getLong(String block, String key, Long defaultValue) {
        String value = getString(block, key);
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Long.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }


    public static double getDouble(String block, String key) {
        return getDouble(block, key, 0.);
    }

    public static double getDouble(String block, String key, Double defaultValue) {
        String value = getString(block, key);
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Double.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }


    public static String getString(String block, String key) {
        try {
            byte[] buffer = getBytes(block, key);
            if (null != buffer) {
                return new String(buffer, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T get(String block, String key, Class<T> tClass) {
        return LiteCache.getInstance().get(block, key, tClass);
    }

    //---------------------数据同步读取结束---------------------------------
    //---------------------数据异步读取开始---------------------------------

    public static CacheReadBufTask loadBytes(String block, String key) {
        return new CacheReadBufTask(block, key);
    }

    public static <T> CacheReadResultTask<T> load(String block, String key, Class<T> clazz) {
        return new CacheReadResultTask<T>(clazz, block, key);
    }

    public static CacheReadBooleanTask loadBool(String block, String key) {
        return new CacheReadBooleanTask(block, key, Boolean.FALSE);
    }

    public static CacheReadBooleanTask loadBool(String block, String key, Boolean defaultValue) {
        return new CacheReadBooleanTask(block, key, defaultValue);
    }

    public static CacheReadIntTask loadInt(String block, String key) {
        return new CacheReadIntTask(block, key, 0);
    }

    public static CacheReadIntTask loadInt(String block, String key, Integer defaultValue) {
        return new CacheReadIntTask(block, key, defaultValue);
    }

    public static CacheReadDoubleTask loadDouble(String block, String key) {
        return new CacheReadDoubleTask(block, key, 0.0);
    }

    public static CacheReadDoubleTask loadDouble(String block, String key, Double defaultValue) {
        return new CacheReadDoubleTask(block, key, defaultValue);
    }

    public static CacheReadLongTask loadLong(String block, String key) {
        return new CacheReadLongTask(block, key, 0L);
    }

    public static CacheReadLongTask loadLong(String block, String key, Long defaultValue) {
        return new CacheReadLongTask(block, key, defaultValue);
    }

    public static CacheReadStringTask loadString(String block, String key) {
        return new CacheReadStringTask(block, key, null);
    }

    public static CacheReadStringTask loadString(String block, String key, String defaultValue) {
        return new CacheReadStringTask(block, key, defaultValue);
    }

    //---------------------数据异步读取结束---------------------------------

    //---------------------数据存储开始------------------------------------
    public static void putBytes(String key, byte[] value) {
        LiteCache.getInstance().put(sDefaultBlock, key, value);
    }

    public static void putInt(String key, Integer value) {
        putString(sDefaultBlock, key, String.valueOf(value));
    }

    public static void putLong(String key, Long value) {
        putString(sDefaultBlock, key, String.valueOf(value));
    }

    public static void putBool(String key, Boolean value) {
        putString(sDefaultBlock, key, String.valueOf(value));
    }

    public static void putDouble(String key, Double value) {
        putString(sDefaultBlock, key, String.valueOf(value));
    }

    public static void putString(String key, String value) {
        try {
            if (TextUtils.isEmpty(value)) {
                return;
            }
            putBytes(sDefaultBlock, key, value.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static <T> void put(String key, T t) {
        LiteCache.getInstance().put(sDefaultBlock, key, t);
    }


    //---------------------数据存储结束------------------------------------
    //---------------------数据同步读取开始---------------------------------
    public static byte[] getBytes(String key) {
        return LiteCache.getInstance().get(sDefaultBlock, key);
    }

    public static boolean getBool(String key) {
        return getBool(sDefaultBlock, key, Boolean.FALSE);
    }

    public static boolean getBool(String key, Boolean defaultValue) {
        String value = getString(sDefaultBlock, key);
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Boolean.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public static int getInt(String key) {
        return getInt(sDefaultBlock, key, 0);
    }

    public static int getInt(String key, Integer defaultValue) {
        String value = getString(sDefaultBlock, key);
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public static long getLong(String key) {
        return getLong(sDefaultBlock, key, 0L);
    }

    public static long getLong(String key, Long defaultValue) {
        String value = getString(sDefaultBlock, key);
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Long.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }


    public static double getDouble(String key) {
        return getDouble(sDefaultBlock, key, 0.);
    }

    public static double getDouble(String key, Double defaultValue) {
        String value = getString(sDefaultBlock, key);
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Double.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }


    public static String getString(String key) {
        try {
            byte[] buffer = getBytes(sDefaultBlock, key);
            if (null != buffer) {
                return new String(buffer, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T get(String key, Class<T> tClass) {
        return LiteCache.getInstance().get(sDefaultBlock, key, tClass);
    }

    //---------------------数据同步读取结束---------------------------------
    //---------------------数据异步读取开始---------------------------------

    public static CacheReadBufTask loadBytes(String key) {
        return new CacheReadBufTask(sDefaultBlock, key);
    }

    public static <T> CacheReadResultTask<T> load(String key, Class<T> clazz) {
        return new CacheReadResultTask<T>(clazz, sDefaultBlock, key);
    }

    public static CacheReadBooleanTask loadBool(String key) {
        return new CacheReadBooleanTask(sDefaultBlock, key, Boolean.FALSE);
    }

    public static CacheReadBooleanTask loadBool(String key, Boolean defaultValue) {
        return new CacheReadBooleanTask(sDefaultBlock, key, defaultValue);
    }

    public static CacheReadIntTask loadInt(String key) {
        return new CacheReadIntTask(sDefaultBlock, key, 0);
    }

    public static CacheReadIntTask loadInt(String key, Integer defaultValue) {
        return new CacheReadIntTask(sDefaultBlock, key, defaultValue);
    }

    public static CacheReadDoubleTask loadDouble(String key) {
        return new CacheReadDoubleTask(sDefaultBlock, key, 0.0);
    }

    public static CacheReadDoubleTask loadDouble(String key, Double defaultValue) {
        return new CacheReadDoubleTask(sDefaultBlock, key, defaultValue);
    }

    public static CacheReadLongTask loadLong(String key) {
        return new CacheReadLongTask(sDefaultBlock, key, 0L);
    }

    public static CacheReadLongTask loadLong(String key, Long defaultValue) {
        return new CacheReadLongTask(sDefaultBlock, key, defaultValue);
    }

    public static CacheReadStringTask loadString(String key) {
        return new CacheReadStringTask(sDefaultBlock, key, null);
    }

    //---------------------数据删除----------------------------------------
    public static void remove(String block, String key) {
        LiteCache.getInstance().remove(block, key);
    }

    public static List<String> keys(String block) {
        return LiteCache.getInstance().keys(block);
    }

    public static void remove(String block) {
        LiteCache.getInstance().remove(block);
    }


    public static void clear() {
        LiteCache.getInstance().clear();
    }

    public static boolean isDebug() {
        return sDebug;
    }

    //---------------------初始化----------------------------------------
    public static class Initializer {
        private boolean debug;
        private Context context;
        private IEncyptInterceptor encyptInterceptor;
        private ISerializeInterceptor serializeInterceptor;
        private String rootBlock;
        private String defaultBlock = "default";
        private Executor executor;
        private static final String DEFAULT_SDCARD = "/sdcard";
        private static final String DEFAULT_BLOCK = "block_lite_cache";

        private Initializer(Context context) {
            this.context = context;
        }

        public Initializer debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public Initializer encyptInterceptor(IEncyptInterceptor encyptInterceptor) {
            this.encyptInterceptor = encyptInterceptor;
            return this;
        }

        public Initializer serializeInterceptor(ISerializeInterceptor serializeInterceptor) {
            this.serializeInterceptor = serializeInterceptor;
            return this;
        }

        public Initializer rootBlock(String rootBlock) {
            this.rootBlock = rootBlock;
            return this;
        }

        public Initializer defaultBlock(String defaultBlock) {
            this.defaultBlock = defaultBlock;
            return this;
        }

        public Initializer executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public void initialize() {
            if (null == serializeInterceptor) {
                throw new RuntimeException("SerializeInterceptor must not be null!");
            }
            //设置环境
            BlockCache.sDebug = debug;
            BlockCache.sDefaultBlock = defaultBlock;
            //设置根目录
            File cacheFile = context.getCacheDir();
            String contextCachePath = null;
            String packageName = context.getPackageName();
            if (null != cacheFile) {
                contextCachePath = context.getCacheDir().getAbsolutePath() + File.separator;
            } else {
                contextCachePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                if (TextUtils.isEmpty(contextCachePath)) {
                    contextCachePath = DEFAULT_SDCARD;
                }
                if (!contextCachePath.endsWith(File.separator)) {
                    contextCachePath = contextCachePath + File.separator;
                }
                contextCachePath = contextCachePath + packageName + File.separator;
            }
            String sRootPath = contextCachePath + DEFAULT_BLOCK;
            if (!TextUtils.isEmpty(rootBlock)) {
                sRootPath = sRootPath + File.separator + rootBlock;
            }
            LiteCache.getInstance().setEncyptInterceptor(encyptInterceptor);
            LiteCache.getInstance().setSerializeInterceptor(serializeInterceptor);
            LiteCache.getInstance().setRootPath(sRootPath);
            LiteCache.getInstance().setExecutor(executor);
        }
    }
}
