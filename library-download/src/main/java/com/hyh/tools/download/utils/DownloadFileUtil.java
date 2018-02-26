package com.hyh.tools.download.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import com.hyh.tools.download.bean.Constants;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Administrator
 * @description
 * @data 2018/2/7
 */

public class DownloadFileUtil {


    /**
     * SDCARD是否存在
     */
    public static boolean externalMemoryAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


    /**
     * 获取SDCARD剩余存储空间
     *
     * @return
     */
    public static long getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize;
            long availableBlocks;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.getBlockSizeLong();
                availableBlocks = stat.getAvailableBlocksLong();
            } else {
                blockSize = stat.getBlockSize();
                availableBlocks = stat.getAvailableBlocks();
            }
            return availableBlocks * blockSize;
        } else {
            return Constants.MEMORY_SIZE_ERROR;
        }
    }


    /**
     * 获取手机内部剩余存储空间
     *
     * @return
     */
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize;
        long availableBlocks;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            availableBlocks = stat.getAvailableBlocksLong();
        } else {
            blockSize = stat.getBlockSize();
            availableBlocks = stat.getAvailableBlocks();
        }
        return availableBlocks * blockSize;
    }


    public static File getDownLoadFile(Context context, String resKey) {
        File dir;
        //has sdcard 优先存于sd卡中 如果没有就存于内部内存中
        if (externalMemoryAvailable()) {
            File filesDir = context.getExternalFilesDir(null);
            if (filesDir == null) {
                filesDir = new File(Environment.getExternalStorageDirectory() + File.separator
                        + "Android" + File.separator
                        + "data" + File.separator
                        + context.getPackageName() + File.separator
                        + "files");
            }
            dir = new File(filesDir, "download");
        } else {
            File filesDir = context.getFilesDir();
            if (filesDir == null) {
                filesDir = new File(context.getCacheDir().getParentFile(), "files");
            }
            dir = new File(filesDir, "download");
        }
        if (!dir.exists()) {
            //如果文件夹不存在 创建文件夹
            dir.mkdirs();
        }
        return new File(dir, string2MD5(resKey));
    }


    public static boolean deleteDownloadFile(Context context, String resKey, int rangeNum) {
        boolean delete = false;
        File file = getDownLoadFile(context, resKey);
        if (file != null && file.exists()) {
            final File to = new File(file, String.valueOf(System.currentTimeMillis()));
            if (file.renameTo(to)) {
                delete = to.delete();
            } else {
                delete = file.delete();
            }
        }
        if (rangeNum > 1) {
            for (int rangeId = 0; rangeId < rangeNum; rangeId++) {
                File tempFile = getTempFile(context, resKey, rangeId);
                if (tempFile != null && tempFile.exists()) {
                    final File to = new File(tempFile, String.valueOf(System.currentTimeMillis()));
                    if (tempFile.renameTo(to)) {
                        to.delete();
                    } else {
                        tempFile.delete();
                    }
                }
            }
        }
        return delete;
    }

    static File getTempFile(Context context, String resKey, int rangeId) {
        File downLoadFile = getDownLoadFile(context, resKey);
        return new File(downLoadFile.getParent(), downLoadFile.getName().concat("_").concat
                (String.valueOf(rangeId)));
    }

    private static String string2MD5(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(content.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                String s = Integer.toHexString(0xff & bytes[i]);
                if (s.length() == 1) {
                    sb.append("0").append(s);
                } else {
                    sb.append(s);
                }
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return content;
    }
}
