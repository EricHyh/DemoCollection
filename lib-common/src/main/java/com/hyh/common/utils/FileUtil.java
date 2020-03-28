package com.hyh.common.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * @author Administrator
 * @description
 * @data 2019/6/17
 */

public class FileUtil {

    private static final String TAG = "FileUtil";

    public static boolean externalMemoryAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && !Environment.isExternalStorageRemovable();
    }

    public static File ensureCreated(File fileDir) {
        if (!fileDir.exists() && !fileDir.mkdirs()) {
            Log.w(TAG, "Unable to create the directory:" + fileDir.getPath());
        }
        return fileDir;
    }

    public static boolean isChildFile(File parent, File child) {
        if (parent == null || child == null) return false;
        return child.getAbsolutePath().startsWith(parent.getAbsolutePath());
    }

    public static File getChildFile(File file1, File file2) {
        if (isChildFile(file1, file2)) {
            return file2;
        }
        if (isChildFile(file2, file1)) {
            return file1;
        }
        return null;
    }

    public static String copyFileToTargetDir(String filePath, File dir) {
        FileUtil.ensureCreated(dir);
        if (filePath.startsWith(dir.getAbsolutePath())) {
            return filePath;
        }
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                String targetFilePath = new File(dir, file.getName()).getAbsolutePath();
                bis = new BufferedInputStream(new FileInputStream(file));
                bos = new BufferedOutputStream(new FileOutputStream(targetFilePath, false));
                int len;
                byte[] buffer = new byte[1024 * 1024];
                while ((len = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);
                }
                bos.flush();
                return targetFilePath;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return filePath;
    }

    public static String getExternalFilesDir(Context context) {
        File externalFilesDir = context.getExternalFilesDir(null);
        if (externalFilesDir == null) {
            externalFilesDir = new File(Environment.getExternalStorageDirectory() + File.separator
                    + "Android" + File.separator
                    + "data" + File.separator
                    + context.getPackageName() + File.separator
                    + "files");
        }
        return externalFilesDir.getAbsolutePath();
    }

    public static String getExternalFilesDir(Context context, String type) {
        if (TextUtils.isEmpty(type)) return getExternalFilesDir(context);
        File externalFilesDir = context.getExternalFilesDir(type);
        if (externalFilesDir == null) {
            externalFilesDir = new File(Environment.getExternalStorageDirectory() + File.separator
                    + "Android" + File.separator
                    + "data" + File.separator
                    + context.getPackageName() + File.separator
                    + "files" + File.separator
                    + type);
        }
        return externalFilesDir.getAbsolutePath();
    }

    public static String getExternalRootFilesDir(String type) {
        if (TextUtils.isEmpty(type)) return Environment.getExternalStorageDirectory().getAbsolutePath();
        File   externalFilesDir = new File(Environment.getExternalStorageDirectory() + File.separator + type);
        return externalFilesDir.getAbsolutePath();
    }
}