package com.hyh.common.log.core;

import android.content.Context;

import com.hyh.common.utils.PermissionUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Class: FileLog
 * 功能描述：日志文件工具
 *
 * @author [zhengge]
 * @version 1.0, 2018.05.04
 * @see [相关的类或者方法]
 */
class FileLog {

    private static final String FILE_PREFIX = "sdkLog_";
    private static final String FILE_FORMAT = ".log";
    private static final String LOG_DIR = "log";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    // 05-02 14:59:38.078
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("MM-dd HH:mm:ss");

    public static String getFileName() {
        String format = dateFormat.format(new Date());
        return FILE_PREFIX + format + FILE_FORMAT;
    }

    private static String getCurrentTime() {
        return timeFormat.format(new Date());
    }

    private static File openFile(Context context) {
        String path = context.getExternalCacheDir() + File.separator + LOG_DIR;
        File file = new File(path);
        if (!file.exists()) file.mkdirs();
        return new File(file, getFileName());
    }

    /**
     * @param commonTag
     */
    public static synchronized void printFile(Context context, String commonTag, String msg) {
        if (context == null) return;

        File file = openFile(context);
        if (file != null) {
            save(file, commonTag, msg);
        }
    }

    private static void save(File file, String commonTag, String msg) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write("\n");
            StringBuffer stringBuffer = new StringBuffer();
            // 需要写入时间
            stringBuffer.append(getCurrentTime()).append("  ");
            stringBuffer.append(commonTag).append(":").append(msg);
            outputStreamWriter.write(stringBuffer.toString());
            outputStreamWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 每次启动的时候 删除10天前的日志
     */
    public static void deleteInvalidFile(Context context) {
        if (context == null || !PermissionUtil.hasWriteSdCardPermission(context)) return;

        String path = context.getExternalCacheDir() + File.separator + LOG_DIR;
        File logDirectory = new File(path);
        if (logDirectory != null && logDirectory.exists()) {
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (getFileDate(pathname).compareTo(getLatestTenDay()) < 0) return true;
                    else return false;
                }
            };
            File[] files = logDirectory.listFiles(filter);
            for (File file : files) {
                file.delete();
            }
        }


    }

    /**
     * 获取10天前的日期
     */
    private static String getLatestTenDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String str = sdf.format(new Date());
        Date date = sdf.parse(str, new ParsePosition(0));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // add方法中的第二个参数n中，正数表示该日期后n天，负数表示该日期的前n天
        calendar.add(Calendar.DATE, -10);
        Date date1 = calendar.getTime();
        String out = sdf.format(date1);
        return out;
    }

    private static String getFileDate(File logFile) {
        return logFile.getName().substring(7, 15);
    }

}
