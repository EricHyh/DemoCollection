package com.hyh.common.provider;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.hyh.common.log.Logger;
import com.hyh.common.utils.FileUtil;

import java.io.File;
import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2019/7/23
 */

public class FileProviderManager {

    private static final String DEFAULT_FILE_PROVIDER_PATH = "com.yly.mob.DlxProvider";

    private static FileProviderManager sInstance = new FileProviderManager();

    public static FileProviderManager getInstance() {
        return sInstance;
    }

    private List<FileProviderInfo> mFileProviderInfos;

    private FileProviderInfo mSuggestedFileProvider;

    private Context mContext;

    private FileProviderManager() {
    }

    public void init(Context context, String outerFileProviderClassPath) {
        this.mContext = context.getApplicationContext();
        Logger.d("FileProviderManager outerFileProviderClassPath = " + outerFileProviderClassPath);
        if (mFileProviderInfos != null && !mFileProviderInfos.isEmpty()) return;
        mFileProviderInfos = FileProviderParser.parseAllFileProvider(mContext);
        if (mFileProviderInfos == null || mFileProviderInfos.isEmpty()) {
            Logger.d("FileProviderManager init mFileProviderInfos is empty");
            return;
        }

        FileProviderInfo defaultFileProviderInfo = null;
        FileProviderInfo outerFileProviderInfo = null;

        for (FileProviderInfo fileProviderInfo : mFileProviderInfos) {
            if (TextUtils.equals(fileProviderInfo.name, outerFileProviderClassPath)) {
                outerFileProviderInfo = fileProviderInfo;
            } else if (TextUtils.equals(fileProviderInfo.name, DEFAULT_FILE_PROVIDER_PATH)) {
                defaultFileProviderInfo = fileProviderInfo;
                break;
            }
        }
        if (defaultFileProviderInfo != null) {
            mSuggestedFileProvider = defaultFileProviderInfo;
        } else if (outerFileProviderInfo != null) {
            mSuggestedFileProvider = outerFileProviderInfo;
        } else {
            mSuggestedFileProvider = mFileProviderInfos.get(0);
        }
    }

    public String getSuggestedDirPath() {
        String suggestedDirPath = null;
        if (mSuggestedFileProvider != null) {
            FilePathEntry suggestedPath = mSuggestedFileProvider.getSuggestedPath();
            if (suggestedPath != null) {
                suggestedDirPath = suggestedPath.getSuggestedDirPath();
            }
        }
        if (TextUtils.isEmpty(suggestedDirPath)) {
            suggestedDirPath = getDefaultFileDir(mContext);
            Logger.d("FileProviderManager getSuggestedDirPath getDefaultFileDir = " + suggestedDirPath);
        }
        return suggestedDirPath;
    }

    public File getSuggestedDir() {
        return new File(getSuggestedDirPath());
    }

    public Uri getContentUriForFile(File file) {
        Uri uri = null;
        if (mSuggestedFileProvider != null) {
            FilePathEntry filePathEntry = mSuggestedFileProvider.selectSupportPath(file);
            Logger.d("FileProviderManager getContentUriForFile suggested filePathEntry = " + filePathEntry);
            if (filePathEntry != null) {
                uri = filePathEntry.getContentUriForFile(file);
            }
        }
        if (uri == null && mFileProviderInfos != null && !mFileProviderInfos.isEmpty()) {
            for (FileProviderInfo fileProviderInfo : mFileProviderInfos) {
                if (fileProviderInfo == mSuggestedFileProvider) continue;
                FilePathEntry filePathEntry = fileProviderInfo.selectSupportPath(file);
                Logger.d("FileProviderManager getContentUriForFile filePathEntry = " + filePathEntry);
                if (filePathEntry != null) {
                    uri = filePathEntry.getContentUriForFile(file);
                    break;
                }
            }
        }
        return uri;
    }

    private String getDefaultFileDir(Context context) {
        File dir;
        //has sdcard 优先存于sd卡中 如果没有就存于内部内存中
        if (FileUtil.externalMemoryAvailable()) {
            File filesDir = context.getExternalFilesDir(null);
            dir = new File(filesDir, "download");
        } else {
            File filesDir = context.getFilesDir();
            dir = new File(filesDir, "download");
        }
        FileUtil.ensureCreated(dir);
        return dir.getAbsolutePath();
    }
}