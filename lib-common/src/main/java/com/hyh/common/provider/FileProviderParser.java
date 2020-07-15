package com.hyh.common.provider;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.text.TextUtils;

import com.hyh.common.log.Logger;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2019/6/17
 */

public class FileProviderParser {

    public static FileProviderInfo selectFileProviderByAuthority(Context context, String authority) {
        if (TextUtils.isEmpty(authority)) {
            return null;
        }
        FileProviderInfo fileProviderInfo = null;
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PROVIDERS | PackageManager.GET_META_DATA);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (packageInfo != null) {
            ProviderInfo[] providers = packageInfo.providers;
            if (providers != null && providers.length > 0) {
                for (ProviderInfo provider : providers) {
                    if (!authority.equals(provider.authority))
                        continue;
                    String name = provider.name;
                    Bundle metaData = provider.metaData;
                    if (metaData == null) {
                        break;
                    }
                    Object file_paths_id_obj = metaData.get("android.support.FILE_PROVIDER_PATHS");
                    if (file_paths_id_obj instanceof Integer) {
                        int file_paths_id = (int) file_paths_id_obj;
                        try {
                            XmlResourceParser xml = context.getResources().getXml(file_paths_id);
                            fileProviderInfo = new FileProviderInfo(context, name, authority);
                            parseFileProviderXml(xml, fileProviderInfo);
                            Logger.d("FileProviderParser fileProviderInfo = " + fileProviderInfo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return fileProviderInfo;
    }

    public static List<FileProviderInfo> parseAllFileProvider(Context context) {
        List<FileProviderInfo> fileProviderInfoList = new ArrayList<>();
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PROVIDERS | PackageManager.GET_META_DATA);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (packageInfo != null) {
            ProviderInfo[] providers = packageInfo.providers;
            if (providers != null && providers.length > 0) {
                for (ProviderInfo provider : providers) {
                    String name = provider.name;
                    String authority = provider.authority;
                    Bundle metaData = provider.metaData;
                    if (metaData == null) {
                        continue;
                    }
                    Object file_paths_id_obj = metaData.get("android.support.FILE_PROVIDER_PATHS");
                    if (file_paths_id_obj instanceof Integer) {
                        int file_paths_id = (int) file_paths_id_obj;
                        try {
                            XmlResourceParser xml = context.getResources().getXml(file_paths_id);
                            FileProviderInfo fileProviderInfo = new FileProviderInfo(context, name, authority);
                            parseFileProviderXml(xml, fileProviderInfo);
                            fileProviderInfoList.add(fileProviderInfo);
                            Logger.d("FileProviderParser fileProviderInfo = " + fileProviderInfo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return fileProviderInfoList;
    }


    private static void parseFileProviderXml(XmlResourceParser xml, FileProviderInfo fileProviderInfo) {
        int event;
        try {
            event = xml.getEventType();
        } catch (Exception e) {
            return;
        }
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                String name = xml.getName();
                switch (name) {
                    case "root-path": {
                        String pathName = xml.getAttributeValue(null, "name");
                        String path = xml.getAttributeValue(null, "path");
                        if (checkPath(pathName, path)) {
                            List<FilePathEntry> rootPathList = fileProviderInfo.rootPathList;
                            if (rootPathList == null) {
                                fileProviderInfo.rootPathList = rootPathList = new ArrayList<>();
                            }
                            rootPathList.add(new FilePathEntry(fileProviderInfo.context, fileProviderInfo.authority, name, pathName, path));
                        }
                        break;
                    }
                    case "files-path": {
                        String pathName = xml.getAttributeValue(null, "name");
                        String path = xml.getAttributeValue(null, "path");
                        if (checkPath(pathName, path)) {
                            List<FilePathEntry> filesPathList = fileProviderInfo.filesPathList;
                            if (filesPathList == null) {
                                fileProviderInfo.filesPathList = filesPathList = new ArrayList<>();
                            }
                            filesPathList.add(new FilePathEntry(fileProviderInfo.context, fileProviderInfo.authority, name, pathName, path));
                        }
                        break;
                    }
                    case "cache-path": {
                        String pathName = xml.getAttributeValue(null, "name");
                        String path = xml.getAttributeValue(null, "path");
                        if (checkPath(pathName, path)) {
                            List<FilePathEntry> cachePathList = fileProviderInfo.cachePathList;
                            if (cachePathList == null) {
                                fileProviderInfo.cachePathList = cachePathList = new ArrayList<>();
                            }
                            cachePathList.add(new FilePathEntry(fileProviderInfo.context, fileProviderInfo.authority, name, pathName, path));
                        }
                        break;
                    }
                    case "external-path": {
                        String pathName = xml.getAttributeValue(null, "name");
                        String path = xml.getAttributeValue(null, "path");
                        if (checkPath(pathName, path)) {
                            List<FilePathEntry> externalPathList = fileProviderInfo.externalPathList;
                            if (externalPathList == null) {
                                fileProviderInfo.externalPathList = externalPathList = new ArrayList<>();
                            }
                            externalPathList.add(new FilePathEntry(fileProviderInfo.context, fileProviderInfo.authority, name, pathName, path));
                        }
                        break;
                    }
                    case "external-files-path": {
                        String pathName = xml.getAttributeValue(null, "name");
                        String path = xml.getAttributeValue(null, "path");
                        if (checkPath(pathName, path)) {
                            List<FilePathEntry> externalFilesPathList = fileProviderInfo.externalFilesPathList;
                            if (externalFilesPathList == null) {
                                fileProviderInfo.externalFilesPathList = externalFilesPathList = new ArrayList<>();
                            }
                            externalFilesPathList.add(new FilePathEntry(fileProviderInfo.context, fileProviderInfo.authority, name, pathName, path));
                        }
                        break;
                    }
                    case "external-cache-path": {
                        String pathName = xml.getAttributeValue(null, "name");
                        String path = xml.getAttributeValue(null, "path");
                        if (checkPath(pathName, path)) {
                            List<FilePathEntry> externalCachePathList = fileProviderInfo.externalCachePathList;
                            if (externalCachePathList == null) {
                                fileProviderInfo.externalCachePathList = externalCachePathList = new ArrayList<>();
                            }
                            externalCachePathList.add(new FilePathEntry(fileProviderInfo.context, fileProviderInfo.authority, name, pathName, path));
                        }
                        break;
                    }
                }
            }
            try {
                event = xml.next();   //将当前解析器光标往下一步移
            } catch (Exception e) {
                break;
            }
        }
    }

    private static boolean checkPath(String pathName, String path) {
        return pathName != null && path != null;
    }
}