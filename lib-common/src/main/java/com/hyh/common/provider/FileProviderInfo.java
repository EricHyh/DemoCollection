package com.hyh.common.provider;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.hyh.common.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2019/6/17
 */

public class FileProviderInfo {

    public Context context;

    public String name;

    public String authority;

    public FileProviderInfo(Context context, String name, String authority) {
        this.context = context;
        this.name = name;
        this.authority = authority;
    }

    public List<FilePathEntry> rootPathList;

    public List<FilePathEntry> filesPathList;

    public List<FilePathEntry> cachePathList;

    public List<FilePathEntry> externalPathList;

    public List<FilePathEntry> externalFilesPathList;

    public List<FilePathEntry> externalCachePathList;

    public FilePathEntry selectSupportPath(File file) {
        if (file == null) return null;
        return selectSupportPath(file.getAbsolutePath());
    }

    public FilePathEntry selectSupportPath(Uri uri) {
        try {
            String pathName = uri.getPath().split("/")[1];
            List<FilePathEntry> allFilePathEntries = getAllFilePathEntries();
            for (FilePathEntry filePathEntry : allFilePathEntries) {
                if (filePathEntry.pathName.equals(pathName)) {
                    return filePathEntry;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<FilePathEntry> getAllFilePathEntries() {
        List<FilePathEntry> allPathList = new ArrayList<>();
        if (rootPathList != null) {
            allPathList.addAll(rootPathList);
        }
        if (filesPathList != null) {
            allPathList.addAll(filesPathList);
        }
        if (externalPathList != null) {
            allPathList.addAll(externalPathList);
        }
        if (externalFilesPathList != null) {
            allPathList.addAll(externalFilesPathList);
        }

        if (cachePathList != null) {
            allPathList.addAll(cachePathList);
        }

        if (externalCachePathList != null) {
            allPathList.addAll(externalCachePathList);
        }
        return allPathList;
    }

    public FilePathEntry selectSupportPath(String filePath) {
        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        FilePathEntry supportPath;
        if (filePath.startsWith(sdcardPath)) {
            supportPath = selectSupportPath(externalFilesPathList, filePath);
            if (supportPath == null) {
                supportPath = selectSupportPath(externalCachePathList, filePath);
            }
            if (supportPath == null) {
                supportPath = selectSupportPath(externalPathList, filePath);
            }
        } else {
            supportPath = selectSupportPath(filesPathList, filePath);
            if (supportPath == null) {
                supportPath = selectSupportPath(cachePathList, filePath);
            }
        }
        if (supportPath == null) {
            supportPath = selectSupportPath(rootPathList, filePath);
        }
        return supportPath;
    }

    public FilePathEntry getSuggestedPath() {
        if (FileUtil.externalMemoryAvailable()) {
            if (externalFilesPathList != null && !externalFilesPathList.isEmpty()) {
                return externalFilesPathList.get(0);
            }
            if (externalCachePathList != null && !externalCachePathList.isEmpty()) {
                return externalCachePathList.get(0);
            }
        }
        if (filesPathList != null && !filesPathList.isEmpty()) {
            return filesPathList.get(0);
        }
        if (cachePathList != null && !cachePathList.isEmpty()) {
            return cachePathList.get(0);
        }
        if (rootPathList != null && !rootPathList.isEmpty()) {
            return rootPathList.get(0);
        }
        return null;
    }

    private FilePathEntry selectSupportPath(List<FilePathEntry> pathList, String filePath) {
        if (pathList == null || pathList.isEmpty()) return null;
        for (FilePathEntry pathEntry : pathList) {
            String pathDir = pathEntry.getDirPath();
            if (filePath.startsWith(pathDir)) {
                return pathEntry;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "FileProviderInfo{" +
                "name='" + name + '\'' +
                ", authority='" + authority + '\'' +
                ", rootPathList=" + rootPathList +
                ", filesPathList=" + filesPathList +
                ", cachePathList=" + cachePathList +
                ", externalPathList=" + externalPathList +
                ", externalFilesPathList=" + externalFilesPathList +
                ", externalCachePathList=" + externalCachePathList +
                '}';
    }
}