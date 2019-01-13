package com.hyh.download.net;

import java.net.HttpURLConnection;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public interface HttpClient {

    HttpCall newCall(String tag, String url, long startPosition);

    HttpCall newCall(String tag, String url, long startPosition, long endPosition);

    HttpResponse execute(String url) throws Exception;

    class ResponseCode {

        /**
         * 200——>请求成功。
         */
        public static final int OK = HttpURLConnection.HTTP_OK;
        /**
         * 206——>请求部分数据成功。
         */
        public static final int PARTIAL_CONTENT = HttpURLConnection.HTTP_PARTIAL;

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
         * 206——>请求部分数据成功。
         */
        public static final int NOT_FOUND = HttpURLConnection.HTTP_NOT_FOUND;

        /**
         * 请求的范围无法满足
         */
        public static final int RANGE_NOT_SATISFIABLE = 416;

    }
}
