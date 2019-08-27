package com.block.cache.internal;

import android.text.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fandong
 * created on: 2018/10/18
 * description:
 */
public final class CacheUtil {
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private CacheUtil() {
    }

    public static byte[] serialize(Serializable serializable) {
        byte[] buffer = null;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(serializable);
            buffer = bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            CLog.error("CacheUtil", "数据序列化失败：" + e.getMessage());
        } finally {
            if (null != bos) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != oos) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return buffer;
    }

    public static Serializable deserialize(byte[] buffer) {
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        Object obj = null;
        try {
            bis = new ByteArrayInputStream(buffer);
            ois = new ObjectInputStream(bis);
            obj = ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != ois) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != bis) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return (Serializable) obj;
    }

    public static List<String> getKeys(String root, String block) {
        if (!root.endsWith(File.separator)) {
            root = root + File.separator;
        }
        File parent;
        if (TextUtils.isEmpty(block)) {
            parent = new File(root);
        } else {
            parent = new File(root + block);
        }
        if (!parent.exists() || !parent.isDirectory()) {
            return null;
        }
        File[] files = parent.listFiles();
        if (files == null || files.length <= 0) {
            return null;
        }
        List<String> list = new ArrayList<>();
        for (File file : files) {
            if (!file.isDirectory()) {
                list.add(file.getName());
            }
        }
        return list;
    }


    public static boolean cacheInDisk(String root, Cache cache) {
        if (!root.endsWith(File.separator)) {
            root = root + File.separator;
        }
        File parent;
        if (TextUtils.isEmpty(cache.getBlock())) {
            parent = new File(root);
        } else {
            parent = new File(root + cache.getBlock());
        }
        if (!parent.exists() || !parent.isDirectory()) {
            parent.mkdirs();
        }
        File target = new File(parent, cache.getKey());
        if (target.exists()) {
            target.delete();
        }
        FileOutputStream fos = null;
        try {
            if (null != cache.getValue()) {
                target.createNewFile();
                fos = new FileOutputStream(target);
                fos.write(cache.getValue());
                fos.flush();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(fos);
        }
        return false;
    }

    public static byte[] cacheFromDisk(String root, String block, String key) {
        if (!root.endsWith(File.separator)) {
            root = root + File.separator;
        }
        File parent = new File(root + block);
        if (!parent.exists() || !parent.isDirectory()) {
            return null;
        }
        File target = new File(parent, key);
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        try {
            fis = new FileInputStream(target);
            bos = new ByteArrayOutputStream();
            copy(fis, bos);
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(bos);
            close(fis);
        }
        return null;
    }


    public static void deleteCache(String root, Cache cache) {
        File parent;
        if (!TextUtils.isEmpty(cache.getBlock())) {
            if (!root.endsWith(File.separator)) {
                root = root + File.separator;
            }
            parent = new File(root + cache.getBlock());
            if (TextUtils.isEmpty(cache.getKey())) {
                deleteFile(parent);
                return;
            }
            File target = new File(parent, cache.getKey());
            deleteFile(target);
        } else {
            parent = new File(root);
            deleteFile(parent);
        }
    }

    private static boolean deleteFile(File deleteFile) {
        if (deleteFile != null) {
            if (!deleteFile.exists()) {
                return true;
            }
            if (deleteFile.isDirectory()) {
                // 处理目录
                File[] files = deleteFile.listFiles();
                //循环删除目录
                if (null != files) {
                    for (File file : files) {
                        deleteFile(file);
                    }
                }
                //删除目录自己
                return deleteFile.delete();
            } else {
                // 如果是文件，删除
                return deleteFile.delete();
            }
        }
        return false;
    }


    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException localIOException) {
                //
            }

        }
    }

    private static int copy(InputStream inputStream, OutputStream outputStream)
            throws IOException {
        long l = copyLarge(inputStream, outputStream);
        int i;
        if (l > 2147483647L) {
            i = -1;
        } else {
            i = (int) l;
        }
        return i;
    }

    private static long copyLarge(InputStream inputStream,
                                  OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long len = 0L;
        int i = 0;
        while ((i = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, i);
            len += i;
        }
        return len;
    }

}

