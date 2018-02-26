package com.hyh.tools.download.utils;

import android.os.Process;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by Eric_He on 2017/3/11.
 */
public class Utils {



    public static boolean isClassFound(String className) {
        Class<?> targetClass = null;
        try {
            targetClass = Class.forName(className);
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
                Log.d("FDL_HH", "rejectedExecution: ");
            }
        });
        executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        return executor;
    }
}
