package com.hyh.common.log;

import android.content.Context;

/**
 * @author Administrator
 * @description
 * @data 2018/5/30
 */

public interface LogCore {

    void init(Context context);

    void setTag(String tag);

    void setStackTraceIndex(int index);

    void setWriteToFileEnabled(boolean enabled);

    void setLogFileDirPath(String dirPath);

    void v(String tag, String content, Throwable th);

    void d(String tag, String content, Throwable th);

    void i(String tag, String content, Throwable th);

    void w(String tag, String content, Throwable th);

    void e(String tag, String content, Throwable th);

    void wtf(String tag, String content, Throwable th);

}