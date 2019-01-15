package com.hyh.download.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.hyh.download.DownloadInfo;
import com.hyh.download.State;
import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.net.HttpResponse;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Administrator
 * @description
 * @data 2018/12/12
 */

public class DownloadFileHelper {

    private static final int MEMORY_SIZE_ERROR = -1;

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
            return MEMORY_SIZE_ERROR;
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


    public static boolean isFileExists(String filePath) {
        return !TextUtils.isEmpty(filePath) && new File(filePath).exists();
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


    public static void ensureParentCreated(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        File parentFile = new File(filePath).getParentFile();
        if (parentFile != null) {
            ensureCreated(parentFile);
        }
    }


    public static long getFileLength(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return 0;
        }
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return file.length();
        }
        return 0;
    }

    public static String getTaskFilePath(TaskInfo taskInfo) {
        String fileDir = taskInfo.getFileDir();
        String fileName = taskInfo.getFileName();
        if (TextUtils.isEmpty(fileDir) || TextUtils.isEmpty(fileName)) {
            return null;
        }
        return fileDir + File.separator + fileName;
    }

    public static String getTaskFilePath(DownloadInfo downloadInfo) {
        String fileDir = downloadInfo.getFileDir();
        String fileName = downloadInfo.getFileName();
        if (TextUtils.isEmpty(fileDir) || TextUtils.isEmpty(fileName)) {
            return null;
        }
        return fileDir + File.separator + fileName;
    }

    public static void deleteDownloadFile(TaskInfo taskInfo) {
        taskInfo.setCurrentSize(0);
        taskInfo.setTotalSize(0);
        taskInfo.setContentMD5(null);
        taskInfo.setContentType(null);
        taskInfo.setETag(null);
        taskInfo.setLastModified(null);
        taskInfo.setCurrentStatus(State.NONE);

        String filePath = getTaskFilePath(taskInfo);

        if (!TextUtils.isEmpty(filePath)) {
            deleteFile(filePath);
            deleteFile(getTempFilePath(filePath));
        }
    }

    public static boolean deleteFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            L.d("deleteFile filePath is null");
            return false;
        }
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
        L.d("deleteFile file is null");
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

    public static String getTempFilePath(String filePath) {
        return filePath + "-rangeSize.temp";
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

    public static void fixTaskFilePath(HttpResponse response, TaskInfo taskInfo) {
        fixTaskFilePath(response.url(),
                response.header(NetworkHelper.CONTENT_DISPOSITION),
                response.header(NetworkHelper.CONTENT_TYPE),
                taskInfo);
    }

    private static void fixTaskFilePath(String url, String contentDisposition, String contentType, TaskInfo taskInfo) {
        String fileDir = taskInfo.getFileDir();
        String fileName = taskInfo.getFileName();
        if (TextUtils.isEmpty(fileName)) {
            fileName = URLUtil.guessFileName(url, contentDisposition, contentType);
            if (TextUtils.isEmpty(fileName)) {
                fileName = string2MD5(taskInfo.getResKey());
            }
            ensureCreated(fileDir);
            fileName = fixFileExists(fileDir, fileName);
            taskInfo.setFileName(fileName);
        } else {
            ensureCreated(fileDir);
        }
    }


    public static String fixFileExists(String fileDir, String fileName) {
        if (TextUtils.isEmpty(fileDir) || TextUtils.isEmpty(fileName)) {
            return fileName;
        }
        File file = new File(fileDir, fileName);
        if (!file.exists()) {
            return fileName;
        }
        file = fixFileExists(file);
        return file.getName();
    }

    private static String fixFileExists(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return filePath;
        }
        file = fixFileExists(file);
        return file.getAbsolutePath();
    }

    private static File fixFileExists(File file) {
        String fileName = file.getName();
        int index = fileName.lastIndexOf(".");
        String toPrefix;
        String toSuffix;
        if (index < 0) {
            toPrefix = fileName;
            toSuffix = "";
        } else {
            toPrefix = fileName.substring(0, index);
            toSuffix = fileName.substring(index, fileName.length());
        }
        File directory = file.getParentFile();
        ensureCreated(directory);
        BigInteger fileIndex = new BigInteger("0");
        BigInteger addOne = new BigInteger("1");
        File newFile;
        do {
            fileIndex = fileIndex.add(addOne);
            newFile = new File(directory, toPrefix + '(' + fileIndex + ')' + toSuffix);
        } while (newFile.exists());
        return newFile;
    }
}
