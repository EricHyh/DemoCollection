package com.hyh.common.log.core;

import android.content.Context;

import com.hyh.common.log.LogCore;


/**
 * @author Administrator
 * @description
 * @data 2018/6/15
 */

public class LogCoreImpl implements LogCore {


    private static final String SUFFIX = ".java";

    private int mStackTraceIndex;

    @Override
    public void init(Context context) {

    }

    @Override
    public void setTag(String tag) {

    }

    @Override
    public void setStackTraceIndex(int index) {
        mStackTraceIndex = index;
    }

    private String wrapperContent(String content) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement targetElement = stackTrace[mStackTraceIndex];

        String className = targetElement.getClassName();
        String[] classNameInfo = className.split("\\.");
        if (classNameInfo.length > 0) {
            className = classNameInfo[classNameInfo.length - 1] + SUFFIX;
        }

        if (className.contains("$")) {
            className = className.split("\\$")[0] + SUFFIX;
        }

        String methodName = targetElement.getMethodName();
        int lineNumber = targetElement.getLineNumber();

        if (lineNumber < 0) {
            lineNumber = 0;
        }

        String headString = "[ (" + className + ":" + lineNumber + ")#" + methodName + " ] ";
        return headString + content;
    }


    @Override
    public void setWriteToFileEnabled(boolean enabled) {

    }

    @Override
    public void setLogFileDirPath(String dirPath) {

    }

    @Override
    public void v(String tag, String content, Throwable th) {

    }

    @Override
    public void d(String tag, String content, Throwable th) {

    }

    @Override
    public void i(String tag, String content, Throwable th) {

    }

    @Override
    public void w(String tag, String content, Throwable th) {

    }

    @Override
    public void e(String tag, String content, Throwable th) {

    }

    @Override
    public void wtf(String tag, String content, Throwable th) {

    }
}
