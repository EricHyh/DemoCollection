package com.hyh.filedownloader.sample.utils;

import android.util.Log;

import java.util.Locale;

/**
 * @author Administrator
 * @description
 * @data 2018/3/16
 */

public class Print {

    private static final String TAG = "SSP_AD";

    private static String generateContent(StackTraceElement caller, String content) {
        String tag = "[ (%s:%d)#%s ]";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        if (callerClazzName.contains("$")) {
            String[] split = callerClazzName.split("\\$");
            callerClazzName = split[0];
        }
        tag = String.format(Locale.getDefault(), tag, callerClazzName + ".java", caller.getLineNumber(), caller.getMethodName());
        content = tag + ":" + content;
        return content;
    }

    private static StackTraceElement getCallerStackTraceElement() {
        return Thread.currentThread().getStackTrace()[5];
    }

    public static void v(String content) {
        StackTraceElement traceElement = getCallerStackTraceElement();
        content = generateContent(traceElement, content);
        Log.v(TAG, content);
    }

    public static void d(String content) {
        d(content, null);
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

    public static void w(String content, Throwable th) {
        StackTraceElement traceElement = getCallerStackTraceElement();
        content = generateContent(traceElement, content);
        Log.w(TAG, content, th);
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

    public static void e(String content, Throwable throwable) {
        StackTraceElement traceElement = getCallerStackTraceElement();
        content = generateContent(traceElement, content);
        Log.e(TAG, content, throwable);
    }
}
