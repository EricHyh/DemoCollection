package com.hyh.common.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @description
 * @data 2018/3/20
 */

public class ThreadUtil {

    private static volatile Handler sUiHandler = new Handler(Looper.getMainLooper());

    private static volatile Handler sBackHandler;

    private static volatile ThreadPoolExecutor sExecutorService;

    public static void runOnUiThread(Runnable runnable) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            postUiThread(runnable);
        }
    }

    private static Handler getUiHandler() {
        return sUiHandler;
    }

    private static Handler getBackHandler() {
        if (sBackHandler != null) {
            return sBackHandler;
        }
        synchronized (ThreadUtil.class) {
            if (sBackHandler == null) {
                HandlerThread handlerThread = new HandlerThread("trifles_thread");
                handlerThread.setDaemon(true);
                handlerThread.start();
                sBackHandler = new Handler(handlerThread.getLooper());
            }
            return sBackHandler;
        }
    }

    public static Looper getBackThreadLooper() {
        Handler backHandler = getBackHandler();
        return backHandler.getLooper();
    }

    public static void runOnBackThread(Runnable runnable) {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            runnable.run();
        } else {
            execute(runnable);
        }
    }

    public static void postUiThread(Runnable runnable) {
        getUiHandler().post(runnable);
    }

    public static void postUiThreadDelayed(Runnable runnable, long delayMillis) {
        getUiHandler().postDelayed(runnable, delayMillis);
    }

    public static void removeUiThreadRunnable(Runnable runnable) {
        getUiHandler().removeCallbacks(runnable);
    }

    public static void postBackThread(final Runnable runnable) {
        getBackHandler().post(runnable);
    }

    public static void postBackThreadDelayed(Runnable runnable, long delayMillis) {
        getBackHandler().postDelayed(runnable, delayMillis);
    }

    public static void removeBackThreadRunnable(Runnable runnable) {
        getBackHandler().removeCallbacks(runnable);
    }

    private static ExecutorService getExecutorService() {
        if (sExecutorService != null) {
            return sExecutorService;
        }
        synchronized (ThreadUtil.class) {
            if (sExecutorService == null) {
                sExecutorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                        new SynchronousQueue<>(), createThreadFactory());
            }
            return sExecutorService;
        }
    }

    public static void execute(Runnable command) {
        getExecutorService().execute(command);
    }


    private static ThreadFactory createThreadFactory() {
        return runnable -> {
            Thread result = new Thread(runnable, "ThreadUtil");
            result.setDaemon(true);
            return result;
        };
    }

    public static boolean isMain() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
}
