package com.hyh.common.log.core;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

/**
 * Class: LogUtils
 * 功能描述：日志工具
 *
 * @author [zhengge]
 * @version 1.0, 2018.05.04
 * @see [相关的类或者方法]
 */
public class LogUtils {

    public static final boolean DEBUG = true;
    public static final String TAG = null;


    private static int STACK_TRACE_INDEX = 6;
    private static final String SUFFIX = ".java";
    private static Context appContext;
    public static String SDK_DEFAULT_TAG = "YSDKLOG";


    public static void init(Context context) {
        appContext = context;
        removeInvalidLog();
    }


    public static boolean allowD = false;

    public static boolean allowE = false;

    public static boolean allowI = false;

    public static boolean allowV = false;

    public static boolean allowW = false;

    public static boolean allowWtf = false;

    private static boolean showCallerInfo = true;

    private static boolean writeToSdcard = false;


    /**
     * 修改默认的 TAG
     *
     * @param TAG
     */
    public static void setSdkTag(String TAG) {
        SDK_DEFAULT_TAG = TAG;
    }

    public static void setStackTraceIndex(int stackTraceIndex) {
        STACK_TRACE_INDEX = stackTraceIndex;
    }

    private static String wrapperContent(int stackTraceIndex, String TAG) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        StackTraceElement targetElement = stackTrace[stackTraceIndex];
        String className = targetElement.getClassName();
        String[] classNameInfo = className.split("\\.");
        if (classNameInfo.length > 0) {
            className = classNameInfo[classNameInfo.length - 1] + SUFFIX;
        }

        if (className.contains("$")) {
            className = className.split("\\$")[0] + SUFFIX;
        }
        String tag = (TextUtils.isEmpty(TAG) ? className : TAG);

        if (!showCallerInfo) {
            return tag;
        }


        String methodName = targetElement.getMethodName();
        int lineNumber = targetElement.getLineNumber();

        if (lineNumber < 0) {
            lineNumber = 0;
        }

        String headString = "[ (" + className + ":" + lineNumber + ")#" + methodName + " ] ";
        return tag + ":" + headString;
    }

    public static void d(String content) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowD)
            return;
        printD(TAG, content);
    }


    public static void d(String TAG, String content) {
        if (!allowD)
            return;
        printD(TAG, content);
    }

    public static void d(String content, Throwable tr) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowD)
            return;
        printD(TAG, content + tr);
    }

    public static void d(String TAG, String content, Throwable tr) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowD)
            return;
        printD(TAG, content + tr);
    }

    public static void e(String content) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowE)
            return;

        printE(TAG, content);
    }

    public static void e(String TAG, String content) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowE)
            return;
        printE(TAG, content);
    }

    public static void e(String content, Throwable tr) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowE)
            return;
        printE(TAG, content + tr);
    }

    public static void e(String TAG, String content, Throwable tr) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowE)
            return;
        printE(TAG, content + tr);
    }

    public static void i(String content) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowI)
            return;
        printI(TAG, content);
    }

    public static void i(String TAG, String content) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowI)
            return;
        printI(TAG, content);
    }

    public static void i(String content, Throwable tr) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowI)
            return;
        printI(TAG, content + tr);
    }

    public static void i(String TAG, String content, Throwable tr) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowI)
            return;
        printI(TAG, content + tr);
    }

    public static void v(String content) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowV)
            return;
        printV(TAG, content);
    }

    private static void saveLog(String msg) {
        if (writeToSdcard && appContext != null) {
            FileLog.printFile(appContext, SDK_DEFAULT_TAG, msg);
        }
    }

    public static void v(String TAG, String content) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowV)
            return;
        printV(TAG, content);
    }

    private static String getMsg(String TAG, String content) {
        String tag = wrapperContent(STACK_TRACE_INDEX, TAG);
        return tag + "---->" + content;
    }

    public static void v(String content, Throwable tr) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowV)
            return;
        printV(TAG, content + tr);
    }

    public static void v(String TAG, String content, Throwable tr) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowV)
            return;
        printV(TAG, content + tr);
    }

    public static void w(String content) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowW)
            return;
        printW(TAG, content);
    }

    public static void w(String TAG, String content) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowW)
            return;
        printW(TAG, content);
    }

    public static void w(String content, Throwable tr) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowW)
            return;
        printW(TAG, content + tr);
    }

    public static void w(String TAG, String content, Throwable tr) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowW)
            return;
        printW(TAG, content + tr);
    }

    public static void w(Throwable tr) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowW)
            return;
        printW(TAG, "" + tr);
    }

    public static void wtf(String content) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowWtf)
            return;
        printWtf(TAG, content);
    }

    public static void wtf(String TAG, String content) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowWtf)
            return;
        printWtf(TAG, content);
    }

    public static void wtf(String content, Throwable tr) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowWtf)
            return;
        printWtf(TAG, content + tr);
    }

    public static void wtf(String TAG, String content, Throwable tr) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowWtf)
            return;
        printWtf(TAG, content + tr);
    }

    public static void wtf(Throwable tr) {
        if (!DEBUG)
            return;
        else
            openLog();
        if (!allowWtf)
            return;
        printWtf(TAG, tr + "");
    }

    public static void openLog() {
        allowD = true;
        allowE = true;
        allowI = true;
        allowV = true;
        allowW = true;
        allowWtf = true;
    }

    public static void closeLog() {
        allowD = false;
        allowE = false;
        allowI = false;
        allowV = false;
        allowW = false;
        allowWtf = false;
    }

    public static void openReleaseLog() {
        allowD = false;
        allowI = false;
        allowV = false;
        allowE = true;
        allowW = true;
        allowWtf = true;
    }


    public static void removeInvalidLog() {
        FileLog.deleteInvalidFile(appContext);
    }


    public static void enableShowCallerInfo() {
        showCallerInfo = true;
    }


    public static void disableShowCallerInfo() {
        showCallerInfo = false;
    }

    public static void enableWriteToSdcard() {
        writeToSdcard = true;
    }

    public static void disableWriteToSdcard() {
        writeToSdcard = false;
    }

    public static void openLogD() {
        allowD = true;
    }

    public static void closeLogD() {
        allowD = false;
    }

    public static void openLogE() {
        allowE = true;
    }

    public static void closeLogE() {
        allowE = false;
    }

    public static void openLogI() {
        allowI = true;
    }

    public static void closeLogI() {
        allowI = false;
    }

    public void openLogV() {
        allowV = true;
    }

    public static void closeLogV() {
        allowV = false;
    }

    public static void openLogW() {
        allowW = true;
    }

    public static void closeLogW() {
        allowW = false;
    }

    public static void openLogWtf() {
        allowWtf = true;
    }

    public static void closeLogWtf() {
        allowWtf = false;
    }

    private static void printD(String TAG, String content) {
        String msg = getMsg(TAG, content);
        Log.d(SDK_DEFAULT_TAG, msg);
        saveLog(msg);
    }

    private static void printE(String TAG, String content) {
        String msg = getMsg(TAG, content);
        Log.e(SDK_DEFAULT_TAG, msg);
        saveLog(msg);
    }

    private static void printI(String TAG, String content) {
        String msg = getMsg(TAG, content);
        Log.i(SDK_DEFAULT_TAG, msg);
        saveLog(msg);
    }

    private static void printV(String TAG, String content) {
        String msg = getMsg(TAG, content);
        Log.v(SDK_DEFAULT_TAG, msg);
        saveLog(msg);
    }

    private static void printW(String TAG, String content) {
        String msg = getMsg(TAG, content);
        Log.w(SDK_DEFAULT_TAG, msg);
        saveLog(msg);
    }

    private static void printWtf(String TAG, String content) {
        String msg = getMsg(TAG, content);
        Log.wtf(SDK_DEFAULT_TAG, msg);
        saveLog(msg);
    }

}
