package com.hyh.filedownloader.sample.net;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @description
 * @data 2019/1/3
 */

public class HappyHttp {

    private static ThreadPoolExecutor executor;

    static {
        executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "HappyHttp");
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    public static HttpCallBuilder from(String url) {
        return new HttpCallBuilder(executor, url);
    }
}
