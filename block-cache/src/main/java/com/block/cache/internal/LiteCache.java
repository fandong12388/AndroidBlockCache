package com.block.cache.internal;


import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.block.cache.BlockCache;
import com.block.cache.interceptor.IEncyptInterceptor;
import com.block.cache.interceptor.ISerializeInterceptor;
import com.block.cache.internal.task.CacheReadTask;
import com.block.cache.internal.task.SimpleCacheWriteTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by fandong
 * created on: 2018/10/18
 * description:真正执行缓存操作的类
 */
public class LiteCache {
    private static final int MAX_TASK_COUNT = 5;
    private static final int MIN_TASK_COUNT = 2;
    private final List<Cache> cacheList;
    private String rootPath;

    private static LiteCache sLiteCache;
    private Executor mExecutor;

    private IEncyptInterceptor mEncyptInterceptor;
    private ISerializeInterceptor mSerializeInterceptor;

    private static InternalHandler sHandler;

    private static Handler getMainHandler() {
        synchronized (LiteCache.class) {
            if (sHandler == null) {
                sHandler = new InternalHandler(Looper.getMainLooper());
            }
            return sHandler;
        }
    }

    public synchronized static LiteCache getInstance() {
        if (null == sLiteCache) {
            sLiteCache = new LiteCache();
        }
        return sLiteCache;
    }

    private LiteCache() {
        this.cacheList = new ArrayList<>();
    }

    public <T> void put(String block, String key, T t) {
        prePut(block, key);
        Cache cache = new Cache<T>(block, key, t, true);
        cacheList.add(cache);
        printPutLog(cache);
        startCacheTasks();
    }

    public void put(String block, String key, byte[] value) {
        prePut(block, key);
        Cache cache = new Cache(block, key, value, true);
        cacheList.add(cache);
        printPutLog(cache);
        startCacheTasks();
    }

    /**
     * 将当前的删除任务与队列当中的任务作对比，从后往前遍历
     * 1、删除的是 block
     * 1.1 、将block相关的任务全部删除
     * 2、删除的是 block+key
     * 2.1 、如果前面有block+key的任务，删除
     *
     * @param block 扇区名称
     * @param key   关键字
     */
    private void preRemove(String block, String key) {
        if (null == cacheList || cacheList.size() <= 0) {
            return;
        }
        List<Cache> cloneList = new ArrayList<Cache>(cacheList);
        for (Cache cache : cloneList) {
            if (cache != null) {
                //1、是清空，那么需要将所有的未开始的任务删除
                if (TextUtils.isEmpty(block) && TextUtils.isEmpty(key)) {
                    if (!cache.get()) {
                        printPutRemoveLog(cache);
                        cacheList.remove(cache);
                    }
                    continue;
                }
                //2.删除block
                if (TextUtils.isEmpty(key)) {
                    if (TextUtils.equals(block, cache.getBlock())) {
                        if (!cache.get()) {
                            printPutRemoveLog(cache);
                            cacheList.remove(cache);
                        }
                    }
                    continue;
                }
                //3.删除block+key
                if (TextUtils.equals(block, cache.getBlock()) && TextUtils.equals(key, cache.getKey())) {
                    if (!cache.get()) {
                        printPutRemoveLog(cache);
                        cacheList.remove(cache);
                    }
                }
            }
        }
    }


    /**
     * 将当前的删存任务与队列当中的任务作对比
     * 1、当两者block和key相同时继续下面的比较，否则返回
     * 2、如果已经存在一个的操作，那么无论这个操作是否开始，都应该将这个操作移除队列
     *
     * @param block 扇区名称
     * @param key   关键字
     */
    private void prePut(String block, String key) {
        if (null == cacheList || cacheList.size() <= 0) {
            return;
        }
        //任务是添加到列表的尾部的，所以遍历应该是从前往后遍历
        List<Cache> cloneList = new ArrayList<Cache>(cacheList);
        for (Cache cache : cloneList) {
            if (cache != null) {
                if (TextUtils.equals(block, cache.getBlock()) && TextUtils.equals(key, cache.getKey())) {
                    if (!cache.get()) {
                        printPutRemoveLog(cache);
                        cacheList.remove(cache);
                    }
                }
            }
        }
    }

    /**
     * 获取队列里面缓存的数据
     * 1、如果队列里面缓存了一个保存block和key的任务，应该立即返回缓存的Cache数据
     * 2、如果队列里面缓存了一个删除block和key的任务，则应该返回null
     * 3、如果队列里面缓存了一个删除block的任务，则应该返回null
     * 4、如果队列里面缓存了一个clear的任务，则应该返回null
     *
     * @param block 扇区
     * @param key   关键字
     * @return 缓存数据
     */
    private Cache getMemoryCache(String block, String key) {
        if (null == cacheList || cacheList.size() <= 0) {
            return null;
        }
        //这里需要从后往前遍历
        List<Cache> cloneList = new ArrayList<Cache>(cacheList);
        if (cloneList.size() <= 0) {
            return null;
        }
        for (Cache cache : cloneList) {
            if (cache != null) {
                //1.如果是删除
                if (!cache.isSave()) {
                    //1.删除一个block,key
                    if ((!TextUtils.isEmpty(cache.getBlock()) && !TextUtils.isEmpty(cache.getKey()) && TextUtils.equals(block, cache.getBlock()) && TextUtils.equals(key, cache.getKey()))
                            //2.删除一个block
                            || (!TextUtils.isEmpty(cache.getBlock()) && TextUtils.isEmpty(cache.getKey()) && TextUtils.equals(block, cache.getBlock()))
                            //3.clear
                            || (TextUtils.isEmpty(cache.getBlock()) && TextUtils.isEmpty(cache.getKey()))) {
                        return null;
                    }
                }
                if (TextUtils.equals(block, cache.getBlock()) && TextUtils.equals(key, cache.getKey())) {
                    return cache;
                }
            }
        }
        return null;
    }

    /**
     * 检查当前队列当中是否已经有一个正在执行的相同操作（为了防止并发的写同一个缓存文件)
     *
     * @param idleCache 需要进行操作的Cache
     * @return true 表示存在相同的操作
     */
    public boolean checkCaching(Cache idleCache) {
        if (null == cacheList || cacheList.size() <= 0) {
            return false;
        }
        //这里需要从后往前遍历
        List<Cache> cloneList = new ArrayList<Cache>(cacheList);
        if (cloneList.size() <= 0) {
            return false;
        }
        for (Cache cache : cloneList) {
            if (cache != null) {
                if (TextUtils.equals(idleCache.getBlock(), cache.getBlock())
                        && TextUtils.equals(idleCache.getKey(), cache.getKey())
                        && idleCache != cache
                        && cache.get()) {
                    printDoingLog(cache);
                    return true;
                }
            }
        }
        return false;
    }

    public void remove(String block, String key) {
        preRemove(block, key);
        cacheList.add(new Cache(block, key, false));
        startCacheTasks();
    }

    public void remove(String block) {
        remove(block, null);
    }

    public void clear() {
        remove(null, null);
    }

    public void removeAndStartInternal(Cache cache) {
        boolean result = cacheList.remove(cache);
        if (!result) {
            printRemoveErrorLog(cache);
            //如果没有删掉，要改变cache的状态
            cache.compareAndSet(true, false);
        } else {
            printRemoveSuccessLog(cache);
        }
        startCacheTaskInternal();
    }

    public List<String> keys(String block) {
        List<Cache> list = new ArrayList<>(cacheList);
        List<String> result = CacheUtil.getKeys(rootPath, block);
        if (list.size() <= 0) {
            return result;
        }
        //从后面往前面遍历
        List<String> addList = new ArrayList<>();
        List<String> rmList = new ArrayList<>();
        int i = list.size() - 1;
        for (; i >= 0; i--) {
            Cache cache = list.get(i);
            if (null == cache) {
                continue;
            }
            //1.如果是clear
            if (TextUtils.isEmpty(cache.getBlock()) && TextUtils.isEmpty(cache.getKey())) {
                if (null != result && result.size() > 0) {
                    result.clear();
                }
                break;
            }
            //2.如果是同一个block
            if (TextUtils.equals(block, cache.getBlock())) {
                //如果包含了删除
                if (!cache.isSave()) {
                    //删除整个block
                    if (TextUtils.isEmpty(cache.getKey())) {
                        if (null != result && result.size() > 0) {
                            result.clear();
                        }
                        break;
                    } else {
                        if (!addList.contains(cache.getKey()) && !rmList.contains(cache.getKey())) {
                            rmList.add(cache.getKey());
                        }
                    }
                } else {
                    //如果包含了添加key
                    if (!TextUtils.isEmpty(cache.getKey())) {
                        if (!rmList.contains(cache.getKey()) && !addList.contains(cache.getKey())) {
                            addList.add(cache.getKey());
                        }
                    }
                }
            }
        }
        if (addList.size() > 0 || rmList.size() > 0) {
            if (null == result || result.size() <= 0) {
                result = new ArrayList<>(addList);
                return result;
            }
            for (String add : addList) {
                if (!result.contains(add)) {
                    result.add(add);
                }
            }
            for (String rm : rmList) {
                if (result.contains(rm)) {
                    result.remove(rm);
                }
            }
        }
        return result;
    }

    public byte[] get(String block, String key) {
        Cache cache = getMemoryCache(block, key);
        if (null != cache) {
            if (!cache.isSave()) {
                return null;
            }
            return cache.getValue();
        }
        byte[] buffer = CacheUtil.cacheFromDisk(getRootPath(), block, key);
        if (null != getEncyptInterceptor()) {
            return getEncyptInterceptor().decypt(buffer);
        }
        return buffer;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String block, String key, Class<T> tClass) {
        Cache cache = getMemoryCache(block, key);
        if (null != cache) {
            if (!cache.isSave()) {
                return null;
            }
            return (T) cache.getObjValue();
        }
        byte[] buffer = CacheUtil.cacheFromDisk(getRootPath(), block, key);
        if (null != getEncyptInterceptor()) {
            buffer = getEncyptInterceptor().decypt(buffer);
        }
        if (null != getSerializeInterceptor() && null != buffer) {
            return getSerializeInterceptor().deSerialize(buffer, tClass);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> void get(final CacheReadTask<T> task) {
        if (null == task) {
            return;
        }
        String block = task.getBlock();
        String key = task.getKey();
        final Cache cache = getMemoryCache(block, key);
        if (null != cache) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                if (!cache.isSave()) {
                    task.postExecute(null);
                    return;
                }
                task.postExecute(cache);
            } else {
                getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (!cache.isSave()) {
                            task.postExecute(null);
                            return;
                        }
                        task.postExecute(cache);
                    }
                });

            }
            return;
        }
        if (null == mExecutor) {
            mExecutor = getDefaultExecutor();
        }
        task.executeOnExecutor(mExecutor);
    }

    private void startCacheTasks() {
        //首先应该判断队列当中是否已经有最小数量的执行线程了
        int doingCnt = 0;
        int todoCnt = 0;
        //这里只需要一个模糊的数据即可，不必太精确
        List<Cache> cloneList = new ArrayList<Cache>(cacheList);
        for (Cache cache : cloneList) {
            if (cache == null) {
                continue;
            }
            if (cache.get()) {
                doingCnt++;
            } else {
                todoCnt++;
            }
            if (doingCnt > MAX_TASK_COUNT * 2) {
                CLog.debug("BlockCache", "当前执行的操作太多，已返回，当前正在执行的操作数有" + doingCnt + "个");
                return;
            }
        }
        if (todoCnt > 0) {
            CLog.debug("BlockCache", "开启线程池，开始执行");
            //从头开始遍历5个不为default的任务
            for (int i = 0; i < MAX_TASK_COUNT; i++) {
                startCacheTaskInternal();
            }
        }
    }

    /**
     * 从任务缓存列表里面查询第一个尚未开始的任务，并开始执行
     */
    private void startCacheTaskInternal() {
        for (int i = 0; i < cacheList.size(); i++) {
            Cache cache = null;
            try {
                //防止IndexOutOfBoundsException
                cache = cacheList.get(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (null == cache) {
                return;
            }
            if (cache.compareAndSet(false, true)) {
                SimpleCacheWriteTask task = new SimpleCacheWriteTask();
                if (null == mExecutor) {
                    mExecutor = getDefaultExecutor();
                }
                task.executeOnExecutor(mExecutor, cache);
                CLog.debug("BlockCache", "doingCnt开始：key:" + cache.getKey());
                return;
            }
        }
    }

    private Executor getDefaultExecutor() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                8, 22, 120, TimeUnit.SECONDS
                , new LinkedBlockingQueue<Runnable>(128)
                , new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(Runnable task) {
                return new Thread(task, "LiteCache #" + mCount.getAndIncrement());
            }
        });
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        return threadPoolExecutor;
    }

    public void setExecutor(Executor executor) {
        this.mExecutor = executor;
    }

    private static class InternalHandler extends Handler {
        private InternalHandler(Looper looper) {
            super(looper);
        }
    }

    public IEncyptInterceptor getEncyptInterceptor() {
        return mEncyptInterceptor;
    }

    public void setEncyptInterceptor(IEncyptInterceptor encyptInterceptor) {
        this.mEncyptInterceptor = encyptInterceptor;
    }

    public ISerializeInterceptor getSerializeInterceptor() {
        return mSerializeInterceptor;
    }

    public void setSerializeInterceptor(ISerializeInterceptor serializeInterceptor) {
        this.mSerializeInterceptor = serializeInterceptor;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
        if (BlockCache.isDebug()) {
            CLog.debug("BlockCache", "rootPath:" + rootPath);
        }
    }

    private void printPutRemoveLog(Cache cache) {
        if (BlockCache.isDebug()) {
            CLog.debug("BlockCache", "【block:" + cache.getBlock() + "_._key:" + cache.getKey()
                    + "的任务写入操作被取消，并加入新的写入操作，当前队列长度：" + cacheList.size() + "】");
        }
    }

    private void printPutLog(Cache cache) {
        if (BlockCache.isDebug()) {
            CLog.debug("BlockCache", "【block:" + cache.getBlock() + "_._key:" + cache.getKey()
                    + "加入新的写入操作，当前队列长度：" + cacheList.size() + "】");
        }
    }

    private void printDoingLog(Cache cache) {
        if (BlockCache.isDebug()) {
            CLog.debug("BlockCache", "【block:" + cache.getBlock() + "_._key:" + cache.getKey()
                    + "正在执行相同写入操作，取消当前待执行任务，当前队列长度：" + cacheList.size() + "】");
        }
    }

    private void printRemoveErrorLog(Cache cache) {
        if (BlockCache.isDebug()) {
            CLog.debug("BlockCache", "【block:" + cache.getBlock() + "_._key:" + cache.getKey()
                    + "任务完成之后，从队列里面删除失败了，当前队列长度：" + cacheList.size() + "】");
        }
    }

    private void printRemoveSuccessLog(Cache cache) {
        if (BlockCache.isDebug()) {
            CLog.debug("BlockCache", "【block:" + cache.getBlock() + "_._key:" + cache.getKey()
                    + "任务完成之后，从队列里面删除成功，当前队列长度：" + cacheList.size() + "】");
        }
    }
}
