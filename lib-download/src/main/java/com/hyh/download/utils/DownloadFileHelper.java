package com.hyh.download.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import com.hyh.download.bean.TaskInfo;
import com.hyh.download.core.Constants;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Administrator
 * @description
 * @data 2018/12/12
 */

public class DownloadFileHelper {

    /**
     * SDCARD是否存在
     */
    private static boolean externalMemoryAvailable() {
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


    public static File ensureCreated(File fileDir) {
        if (!fileDir.exists() && !fileDir.mkdirs()) {
            L.w("Unable to create the directory:" + fileDir.getPath());
        }
        return fileDir;
    }

    public static File ensureCreated(String fileDirPath) {
        return ensureCreated(new File(fileDirPath));
    }


    public static long getFileLength(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return file.length();
        }
        return 0;
    }


    public static boolean deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                boolean delete = file.delete();
                L.d("deleteFile " + filePath + " is delete:" + delete);
                return delete;
            } else {
                return true;
            }
        } catch (Exception e) {
            L.d("deleteFile failed", e);
        }
        return false;
    }

    public static boolean deleteFile(File file) {
        if (file == null) {
            return false;
        }
        try {
            if (file.exists()) {
                boolean delete = file.delete();
                L.d("deleteFile " + file.getAbsolutePath() + " is delete:" + delete);
                return delete;
            }
        } catch (Exception e) {
            L.d("deleteFile failed, filePath:" + file.getAbsolutePath());
        }
        return false;
    }

    public static void deleteDownloadFile(TaskInfo taskInfo) {
        String filePath = taskInfo.getFilePath();
        deleteFile(filePath);
        int rangeNum = taskInfo.getRangeNum();
        if (rangeNum > 1) {
            for (int index = 0; index < rangeNum; index++) {
                File tempFile = new File(filePath + "-" + index);
                deleteFile(tempFile);
            }
        }
    }

    public static String getDefaultFileDir(Context context) {
        File dir;
        //has sdcard 优先存于sd卡中 如果没有就存于内部内存中
        if (externalMemoryAvailable()) {
            File filesDir = context.getExternalFilesDir(null);
            dir = new File(filesDir, "download");
        } else {
            File filesDir = context.getFilesDir();
            dir = new File(filesDir, "download");
        }
        ensureCreated(dir);
        return dir.getAbsolutePath();
    }

    public static String string2MD5(String content) {
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

    public static File getParenFile(String filePath) {
        File file = new File(filePath);
        File parentFile = file.getParentFile();
        return ensureCreated(parentFile);
    }

    public static String getParenFilePath(String filePath) {
        File file = new File(filePath);
        File parentFile = file.getParentFile();
        ensureCreated(parentFile);
        return parentFile.getAbsolutePath();
    }
}
