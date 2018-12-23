package com.hyh.download.core;

import java.net.HttpURLConnection;

/**
 * Created by Eric_He on 2017/3/10.
 */

public class Constants {

    public static final String COMMADN = "command";

    public static final String REQUEST_INFO = "request_info";

    public static final int MEMORY_SIZE_ERROR = -1;

    public static class ResponseCode {

        /**
         * 200——>请求成功。
         */
        static final int OK = HttpURLConnection.HTTP_OK;
        /**
         * 206——>请求部分数据成功。
         */
        static final int PARTIAL_CONTENT = HttpURLConnection.HTTP_PARTIAL;

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
        static final int NOT_FOUND = HttpURLConnection.HTTP_NOT_FOUND;

        /**
         * 请求的范围无法满足
         */
        static final int RANGE_NOT_SATISFIABLE = 416;

    }

    static class EventId {

        static final int ADD_TASK = 0;

        static final int REMOVE_TASK = 1;

    }

    /**
     * @author Administrator
     * @description
     * @data 2018/12/12
     */
    public static class Preference {

        public static final String SHARE_NAME = "file_downloader_config";

        public static class Key {

            public static final String USER_AGENT = "user_agent";

            public static final String CACHE_USER_AGENT_TIME_MILLIS = "cache_user_agent_time_millis";

        }
    }
}
