package com.hyh.tools.download.utils;

import android.util.Log;

import java.util.Locale;

/**
 * @author Administrator
 * @description
 * @data 2018/2/27
 */

public class FD_LogUtil {

    private static final String TAG = "FileDownloader";

    private static String generateContent(StackTraceElement caller, String content) {
        String tag = "%s.%s(L:%d)";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(Locale.getDefault(), tag, callerClazzName, caller.getMethodName(), caller.getLineNumber());
        content = tag + " : " + content;
        return content;
    }


    private static StackTraceElement getCallerStackTraceElement() {
        return Thread.currentThread().getStackTrace()[4];
    }


    public static void d(String content) {
        StackTraceElement traceElement = getCallerStackTraceElement();
        content = generateContent(traceElement, content);
        Log.d(TAG, content);
    }

    public static void d(String content, Throwable th) {
        StackTraceElement traceElement = getCallerStackTraceElement();
        content = generateContent(traceElement, content);
        Log.d(TAG, content, th);
    }


    public static void w(String content) {
        StackTraceElement traceElement = getCallerStackTraceElement();
        content = generateContent(traceElement, content);
        Log.w(TAG, content);
    }


    public static void i(String content) {
        StackTraceElement traceElement = getCallerStackTraceElement();
        content = generateContent(traceElement, content);
        Log.i(TAG, content);
    }

    public static void e(String content) {
        StackTraceElement traceElement = getCallerStackTraceElement();
        content = generateContent(traceElement, content);
        Log.e(TAG, content);
    }

}
