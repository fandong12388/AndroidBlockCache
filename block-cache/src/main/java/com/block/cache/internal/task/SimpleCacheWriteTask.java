package com.block.cache.internal.task;

import android.os.AsyncTask;

import com.block.cache.BlockCache;
import com.block.cache.internal.CLog;
import com.block.cache.internal.Cache;
import com.block.cache.internal.CacheUtil;
import com.block.cache.internal.LiteCache;

/**
 * Created by fandong
 * created on: 2018/10/18
 * description:
 */
public class SimpleCacheWriteTask extends AsyncTask<Cache, Void, Integer> {
    private static final int IGNORE = 0;
    private static final int FINISH = 1;
    private static final int ERROR = 2;

    private Cache mCache;

    @Override
    protected Integer doInBackground(Cache... caches) {
        mCache = caches[0];
        printDoingLog(mCache);
        if (LiteCache.getInstance().checkCaching(mCache)) {
            return IGNORE;
        }
        //执行保存
        if (mCache.isSave()) {
            //首先应该对数据进行预处理
            handleCache(mCache);
            //然后保存数据
            if (!CacheUtil.cacheInDisk(LiteCache.getInstance().getRootPath(), mCache)) {
                printDoingLogError(mCache);
                return ERROR;
            }
        } else {
            CacheUtil.deleteCache(LiteCache.getInstance().getRootPath(), mCache);
        }
        printDoingLogFinish(mCache);
        return FINISH;
    }

    private void handleCache(Cache cache) {
        //如果是简单类型，就直接保存就行了，如果是复杂类型就需要首先要序列化一波
        if (null != cache.getObjValue() && cache.getObjValue().getClass() != String.class
                && null != LiteCache.getInstance().getSerializeInterceptor()) {
            byte[] buffer = LiteCache.getInstance().getSerializeInterceptor().serialize(cache.getObjValue());
            cache.setValue(buffer);
        }
        if (null != LiteCache.getInstance().getEncyptInterceptor() && null != cache.getValue()) {
            byte[] buffer = LiteCache.getInstance().getEncyptInterceptor().encypt(cache.getValue());
            cache.setValue(buffer);
        }
    }

    @Override
    protected void onPostExecute(Integer value) {
        if (FINISH == value) {
            //删除当前的对象
            LiteCache.getInstance().removeAndStartInternal(mCache);
            return;
        }
        if (ERROR == value || IGNORE == value) {
            mCache.compareAndSet(true, false);
        }
    }


    private void printDoingLog(Cache cache) {
        if (BlockCache.isDebug()) {
            CLog.debug("BlockCache", "【block:" + cache.getBlock() + "_._key:" + cache.getKey()
                    + "正在执行" + (cache.isSave() ? "存储" : "删除") + "操作】");
        }
    }

    private void printDoingLogError(Cache cache) {
        if (BlockCache.isDebug()) {
            CLog.debug("BlockCache", "【block:" + cache.getBlock() + "_._key:" + cache.getKey() + "执行存储操作失败了！】");
        }
    }

    private void printDoingLogFinish(Cache cache) {
        if (BlockCache.isDebug()) {
            CLog.debug("BlockCache", "【block:" + cache.getBlock() + "_._key:" + cache.getKey() + "执行" + (cache.isSave() ? "存储" : "删除") + "操作成功！】");
        }
    }

}
