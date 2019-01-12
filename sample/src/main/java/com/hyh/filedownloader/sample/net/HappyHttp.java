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

    public static class Net {

        public static final int REQUEST_TIME_OUT = 10 * 1000;

        public static final int READ_TIME_OUT = 10 * 1000;

        public static final int MAX_REDIRECT_TIMES = 10;

        public static class ResponseCode {

            /**
             * 200——>请求成功。
             */
            public static final int OK = 200;

            /**
             * 206——>请求部分数据成功。
             */
            public static final int PARTIAL_CONTENT = 206;

            /**
             * The target resource resides temporarily under a different URI and the user agent MUST NOT
             * change the request method if it performs an automatic redirection to that URI.
             */
            public static final int HTTP_TEMPORARY_REDIRECT = 307;

            /**
             * The target resource has been assigned a new permanent URI and any future references to this
             * resource ought to use one of the enclosed URIs.
             */
            public static final int HTTP_PERMANENT_REDIRECT = 308;

            /**
             * 未找到资源
             */
            public static final int RESOUCE_NOT_FOUND = 404;

            /**
             * 408——>网络请求超时
             */
            public static final int REQUEST_TIMEOUT = 408;


            /**
             * 请求的范围无法满足
             */
            public static final int RANGE_NOT_SATISFIABLE = 416;

            /**
             * 500——>服务器遇到了意料不到的情况，不能完成客户的请求
             */
            public static final int SERVER_ERRO = 500;

            /**
             * 503——>服务器由于维护或者负载过重未能应答
             */
            public static final int SERVICE_UNAVAILABLE = 503;

        }
    }
}
