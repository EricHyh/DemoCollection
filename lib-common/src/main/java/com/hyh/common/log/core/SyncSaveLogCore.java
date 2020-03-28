package com.hyh.common.log.core;

import android.content.Context;

import com.hyh.common.log.LogCore;


/**
 * @author Administrator
 * @description
 * @data 2018/5/30
 */

public class SyncSaveLogCore implements LogCore {

    public SyncSaveLogCore(String commonTag) {
        LogUtils.SDK_DEFAULT_TAG = commonTag;
        LogUtils.openLog();
        LogUtils.setStackTraceIndex(8);
    }

    @Override
    public void init(Context context) {
        LogUtils.init(context);
    }

    @Override
    public void setTag(String tag) {
        LogUtils.SDK_DEFAULT_TAG = tag;
    }

    @Override
    public void setStackTraceIndex(int index) {
        LogUtils.setStackTraceIndex(index);
    }

    @Override
    public void setWriteToFileEnabled(boolean enabled) {
        if (enabled) {
            LogUtils.enableWriteToSdcard();
        } else {
            LogUtils.disableWriteToSdcard();
        }
    }

    @Override
    public void setLogFileDirPath(String dirPath) {
    }

    @Override
    public void v(String tag, String content, Throwable th) {
        if (th == null) {
            LogUtils.v(tag, content);
        } else {
            LogUtils.v(tag, content, th);
        }
    }

    @Override
    public void d(String tag, String content, Throwable th) {
        if (th == null) {
            LogUtils.d(tag, content);
        } else {
            LogUtils.d(tag, content, th);
        }
    }

    @Override
    public void i(String tag, String content, Throwable th) {
        if (th == null) {
            LogUtils.i(tag, content);
        } else {
            LogUtils.i(tag, content, th);
        }
    }

    @Override
    public void w(String tag, String content, Throwable th) {
        if (th == null) {
            LogUtils.w(tag, content);
        } else {
            LogUtils.w(tag, content, th);
        }
    }

    @Override
    public void e(String tag, String content, Throwable th) {
        if (th == null) {
            LogUtils.e(tag, content);
        } else {
            LogUtils.e(tag, content, th);
        }
    }

    @Override
    public void wtf(String tag, String content, Throwable th) {
        if (th == null) {
            LogUtils.wtf(tag, content);
        } else {
            LogUtils.wtf(tag, content, th);
        }
    }
}