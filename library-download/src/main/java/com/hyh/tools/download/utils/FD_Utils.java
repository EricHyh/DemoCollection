package com.hyh.tools.download.utils;

import android.os.Process;
import android.text.TextUtils;

import com.hyh.tools.download.bean.State;
import com.hyh.tools.download.bean.TagInfo;
import com.hyh.tools.download.bean.TaskInfo;
import com.hyh.tools.download.db.bean.TaskDBInfo;
import com.hyh.tools.download.paser.TagParser;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by Eric_He on 2017/3/11.
 */
public class FD_Utils {


    public static boolean isClassFound(String className) {
        Class<?> targetClass = null;
        try {
            targetClass = FD_Utils.class.getClassLoader().loadClass(className);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return targetClass != null;
    }

    public static ThreadPoolExecutor buildExecutor(int corePoolSize,
                                                   int maximumPoolSize,
                                                   long keepAliveTime,
                                                   final String threadName,
                                                   boolean allowCoreThreadTimeOut) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize
                , maximumPoolSize
                , keepAliveTime
                , TimeUnit.SECONDS
                , new LinkedBlockingQueue<Runnable>()
                , new ThreadFactory() {

            private AtomicInteger mInteger = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, threadName.concat(" - ")
                        .concat(String.valueOf(Process.myPid()))
                        .concat(" : ")
                        .concat(String.valueOf(mInteger.incrementAndGet())));
                thread.setDaemon(true);
                return thread;
            }
        }, new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                FD_LogUtil.d("rejectedExecution");
            }
        });
        executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        return executor;
    }

    public static TaskInfo taskDBInfo2TaskInfo(TaskDBInfo taskDBInfo, TagParser tagParser) {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setResKey(taskDBInfo.getResKey());
        taskInfo.setUrl(taskDBInfo.getUrl());
        taskInfo.setPackageName(taskDBInfo.getPackageName());
        taskInfo.setFilePath(taskDBInfo.getFilePath());
        taskInfo.setVersionCode(taskDBInfo.getVersionCode() == null ? -1 : taskDBInfo.getVersionCode());
        taskInfo.setProgress(taskDBInfo.getProgress() == null ? 0 : taskDBInfo.getProgress());
        taskInfo.setRangeNum(taskDBInfo.getRangeNum() == null ? 0 : taskDBInfo.getRangeNum());
        taskInfo.setTotalSize(taskDBInfo.getTotalSize() == null ? 0 : taskDBInfo.getTotalSize());
        taskInfo.setCurrentSize(taskDBInfo.getCurrentSize() == null ? 0 : taskDBInfo.getCurrentSize());
        taskInfo.setCurrentStatus(taskDBInfo.getCurrentStatus() == null ? State.NONE : taskDBInfo.getCurrentStatus());
        taskInfo.setWifiAutoRetry(taskDBInfo.getWifiAutoRetry() == null ? true : taskDBInfo.getWifiAutoRetry());
        taskInfo.setResponseCode(taskDBInfo.getResponseCode() == null ? 0 : taskDBInfo.getResponseCode());

        String tagStr = taskDBInfo.getTagStr();
        String tagClassName = taskDBInfo.getTagClassName();
        tagClassName = tagParser.onTagClassNameChanged(tagClassName);
        Object tag = null;
        if (!TextUtils.isEmpty(tagStr) && !TextUtils.isEmpty(tagClassName)) {
            try {
                Class<?> clazz = Class.forName(tagClassName);
                tag = tagParser.fromString(tagStr, clazz);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        taskInfo.setTagInfo(new TagInfo(tagStr, tagClassName, tag));
        return taskInfo;
    }


    public static TaskDBInfo taskInfo2TaskDBInfo(TaskInfo taskInfo) {
        TaskDBInfo taskDBInfo = new TaskDBInfo();
        taskDBInfo.setResKey(taskInfo.getResKey());
        taskDBInfo.setUrl(taskInfo.getUrl());
        taskDBInfo.setFilePath(taskInfo.getFilePath());
        taskDBInfo.setCurrentSize(taskInfo.getCurrentSize());
        taskDBInfo.setCurrentStatus(taskInfo.getCurrentStatus());
        taskDBInfo.setTotalSize(taskInfo.getTotalSize());
        taskDBInfo.setPackageName(taskInfo.getPackageName());
        taskDBInfo.setVersionCode(taskInfo.getVersionCode());
        taskDBInfo.setTimeMillis(System.currentTimeMillis());
        taskDBInfo.setProgress(taskInfo.getProgress());
        taskDBInfo.setRangeNum(taskInfo.getRangeNum());
        taskDBInfo.setWifiAutoRetry(taskInfo.isWifiAutoRetry());
        taskDBInfo.setTagClassName(taskInfo.getTagInfo().getTagClassName());
        taskDBInfo.setTagStr(taskInfo.getTagInfo().getTagStr());
        return taskDBInfo;
    }
}
