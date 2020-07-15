package com.hyh.common.provider;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.hyh.common.utils.FileUtil;

import java.io.File;

/**
 * @author Administrator
 * @description
 * @data 2019/6/17
 */

public class FilePathEntry {

    public Context context;

    public String authority;

    public String pathType;

    public String pathName;

    public String path;

    FilePathEntry(Context context, String authority, String pathType, String pathName, String path) {
        this.context = context;
        this.authority = authority;
        this.pathType = pathType;
        this.pathName = pathName;
        this.path = path;
    }

    File getParentDir() {
        File dir = null;
        switch (pathType) {
            case "root-path": {
                dir = new File(File.separator);
                break;
            }
            case "files-path": {
                dir = context.getFilesDir();
                break;
            }
            case "cache-path": {
                dir = context.getCacheDir();
                break;
            }
            case "external-path": {
                dir = Environment.getExternalStorageDirectory();
                break;
            }
            case "external-files-path": {
                dir = context.getExternalFilesDir(null);
                break;
            }
            case "external-cache-path": {
                dir = context.getExternalCacheDir();
                break;
            }
        }
        return dir;
    }

    public String getParentDirPath() {
        File parentDir = getParentDir();
        return parentDir == null ? null : parentDir.getAbsolutePath();
    }

    public File getDir() {
        File parentDir = getParentDir();
        if (parentDir == null) return null;
        return path.equals("") || path.equals(".") ? parentDir : new File(parentDir, path);
    }

    public String getDirPath() {
        File dir = getDir();
        if (dir == null) return null;
        return dir.getAbsolutePath();
    }

    public File getSuggestedDir() {
        File dir = getDir();
        if (dir == null) {
            return null;
        }
        return getSuggestedChildDir(dir);
    }

    public String getSuggestedDirPath() {
        File suggestedDir = getSuggestedDir();
        if (suggestedDir == null) return null;
        return suggestedDir.getAbsolutePath();
    }

    private File getSuggestedChildDir(File parentDir) {
        if (FileUtil.externalMemoryAvailable()) {
            File externalFilesDir = context.getExternalFilesDir(null);
            File child = FileUtil.getChildFile(parentDir, externalFilesDir);
            if (child != null) {
                return child;
            }

            File externalCacheDir = context.getExternalCacheDir();
            child = FileUtil.getChildFile(parentDir, externalCacheDir);
            if (child != null) {
                return child;
            }

            File externalStorageDirectory = Environment.getExternalStorageDirectory();
            child = FileUtil.getChildFile(parentDir, externalStorageDirectory);
            if (child != null) {
                return child;
            }
        } else {
            File filesDir = context.getFilesDir();
            File child = FileUtil.getChildFile(parentDir, filesDir);
            if (child != null) {
                return child;
            }

            File cacheDir = context.getCacheDir();
            child = FileUtil.getChildFile(parentDir, cacheDir);
            if (child != null) {
                return child;
            }
        }
        return parentDir;
    }

    public Uri getContentUriForFile(File file) {
        return getContentUriForFile(file.getAbsolutePath());
    }

    public Uri getContentUriForFile(String filePath) {
        String dirPath = getDirPath();
        String path;
        if (TextUtils.equals(new File(File.separator).getAbsolutePath(), dirPath)) {//根目录需要特殊处理
            path = pathName + filePath;
        } else {
            path = filePath.replaceFirst(dirPath, pathName);
        }
        return new Uri.Builder()
                .scheme("content")
                .authority(authority)
                .encodedPath(path)
                .build();
    }

    public String getFilePathFromContentUri(Uri uri) {
        String dirPath = getDirPath();
        String uriStr = uri.toString();
        if (TextUtils.equals(new File(File.separator).getAbsolutePath(), dirPath)) {//根目录需要特殊处理
            return uriStr.replace("content://" + authority + File.separator + pathName, "");
        } else {
            return uriStr.replace("content://" + authority + File.separator + pathName, dirPath);
        }
    }

    @Override
    public String toString() {
        return "FilePathEntry{" +
                "authority='" + authority + '\'' +
                ", pathType='" + pathType + '\'' +
                ", pathName='" + pathName + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}