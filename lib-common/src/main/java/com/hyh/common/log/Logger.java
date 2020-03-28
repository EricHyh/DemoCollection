package com.hyh.common.log;


import android.content.Context;
import android.util.Log;

import com.hyh.common.log.core.SyncSaveLogCore;


/**
 * @author Administrator
 * @description
 * @data 2018/5/30
 */

public class Logger {

    private static String sCommonTag = "CommonLib";

    private static LogConfig sLogConfig = LogConfig.getDefaultLogConfig();

    private static LogCore sLogCore;

    static {
        sLogCore = new SyncSaveLogCore(sCommonTag);
    }

    public static void setCommonTag(String commonTag) {
        sCommonTag = commonTag;
        sLogCore.setTag(commonTag);
    }

    public static void init(Context context) {
        sLogCore.init(context);
    }

    //v start
    public static void v(String content) {
        if (sLogConfig.isEnabled(Log.VERBOSE)) {
            sLogCore.v(null, content, null);
        }
    }

    public static void v(String content, Throwable th) {
        if (sLogConfig.isEnabled(Log.VERBOSE)) {
            sLogCore.v(null, content, th);
        }
    }

    public static void v(String tag, String content) {
        if (sLogConfig.isEnabled(Log.VERBOSE)) {
            sLogCore.v(tag, content, null);
        }
    }

    public static void v(String tag, String content, Throwable th) {
        if (sLogConfig.isEnabled(Log.VERBOSE)) {
            sLogCore.v(tag, content, th);
        }
    }
    //v end


    //d start
    public static void d(String content) {
        if (sLogConfig.isEnabled(Log.DEBUG)) {
            sLogCore.d(null, content, null);
        }
    }

    public static void d(String content, Throwable th) {
        if (sLogConfig.isEnabled(Log.DEBUG)) {
            sLogCore.d(null, content, th);
        }
    }

    public static void d(String tag, String content) {
        if (sLogConfig.isEnabled(Log.DEBUG)) {
            sLogCore.d(tag, content, null);
        }
    }

    public static void d(String tag, String content, Throwable th) {
        if (sLogConfig.isEnabled(Log.DEBUG)) {
            sLogCore.d(tag, content, th);
        }
    }
    //d end

    //i start
    public static void i(String content) {
        if (sLogConfig.isEnabled(Log.INFO)) {
            sLogCore.i(null, content, null);
        }
    }

    public static void i(String content, Throwable th) {
        if (sLogConfig.isEnabled(Log.INFO)) {
            sLogCore.i(null, content, th);
        }
    }

    public static void i(String tag, String content) {
        if (sLogConfig.isEnabled(Log.INFO)) {
            sLogCore.i(tag, content, null);
        }
    }

    public static void i(String tag, String content, Throwable th) {
        if (sLogConfig.isEnabled(Log.INFO)) {
            sLogCore.i(tag, content, th);
        }
    }
    //i end

    //w start
    public static void w(String content) {
        if (sLogConfig.isEnabled(Log.INFO)) {
            sLogCore.w(null, content, null);
        }
    }

    public static void w(String content, Throwable th) {
        if (sLogConfig.isEnabled(Log.INFO)) {
            sLogCore.w(null, content, th);
        }
    }

    public static void w(String tag, String content) {
        if (sLogConfig.isEnabled(Log.INFO)) {
            sLogCore.w(tag, content, null);
        }
    }

    public static void w(String tag, String content, Throwable th) {
        if (sLogConfig.isEnabled(Log.INFO)) {
            sLogCore.w(tag, content, th);
        }
    }
    //w end


    //e start
    public static void e(String content) {
        if (sLogConfig.isEnabled(Log.ERROR)) {
            sLogCore.e(null, content, null);
        }
    }

    public static void e(String content, Throwable th) {
        if (sLogConfig.isEnabled(Log.ERROR)) {
            sLogCore.e(null, content, th);
        }
    }

    public static void e(String tag, String content) {
        if (sLogConfig.isEnabled(Log.ERROR)) {
            sLogCore.e(tag, content, null);
        }
    }

    public static void e(String tag, String content, Throwable th) {
        if (sLogConfig.isEnabled(Log.ERROR)) {
            sLogCore.e(tag, content, th);
        }
    }
    //e end


    //wtf start
    public static void wtf(String content) {
        if (sLogConfig.isEnabled(Log.ASSERT)) {
            sLogCore.wtf(null, content, null);
        }
    }

    public static void wtf(String content, Throwable th) {
        if (sLogConfig.isEnabled(Log.ASSERT)) {
            sLogCore.wtf(null, content, th);
        }
    }

    public static void wtf(String tag, String content) {
        if (sLogConfig.isEnabled(Log.ASSERT)) {
            sLogCore.wtf(tag, content, null);
        }
    }

    public static void wtf(String tag, String content, Throwable th) {
        if (sLogConfig.isEnabled(Log.ASSERT)) {
            sLogCore.wtf(tag, content, th);
        }
    }
    //wtf end

    public static void setWriteToFileEnabled(boolean enabled) {
        sLogCore.setWriteToFileEnabled(enabled);
    }

    public static void setVerboseEnabled(boolean enabled) {
        sLogConfig.verboseEnabled = enabled;
    }

    public static void setDebugEnabled(boolean enabled) {
        sLogConfig.verboseEnabled = enabled;
    }

    public static void setInfoEnabled(boolean enabled) {
        sLogConfig.verboseEnabled = enabled;
    }

    public static void setWarnEnabled(boolean enabled) {
        sLogConfig.verboseEnabled = enabled;
    }

    public static void setErrorEnabled(boolean enabled) {
        sLogConfig.verboseEnabled = enabled;
    }

    private static class LogConfig {

        static LogConfig getDefaultLogConfig() {
            LogConfig logConfig = new LogConfig();
            logConfig.verboseEnabled = true;
            logConfig.debugEnabled = true;
            logConfig.infoEnabled = true;
            logConfig.warnEnabled = true;
            logConfig.errorEnabled = true;
            logConfig.wtfEnabled = true;
            return logConfig;
        }

        private boolean verboseEnabled;
        private boolean debugEnabled;
        private boolean infoEnabled;
        private boolean warnEnabled;
        private boolean errorEnabled;
        private boolean wtfEnabled;

        boolean isEnabled(int logLevel) {
            boolean isEnabled = false;
            switch (logLevel) {
                case Log.VERBOSE: {
                    isEnabled = verboseEnabled;
                    break;
                }
                case Log.DEBUG: {
                    isEnabled = debugEnabled;
                    break;
                }
                case Log.INFO: {
                    isEnabled = infoEnabled;
                    break;
                }
                case Log.WARN: {
                    isEnabled = warnEnabled;
                    break;
                }
                case Log.ERROR: {
                    isEnabled = errorEnabled;
                    break;
                }
                case Log.ASSERT: {
                    isEnabled = wtfEnabled;
                    break;
                }
            }
            return isEnabled;
        }
    }
}
