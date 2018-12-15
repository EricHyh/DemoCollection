package com.hyh.download.core;

/**
 * Created by Eric_He on 2017/3/10.
 */

public class Constants {

    public static final String COMMADN = "command";

    public static final String REQUEST_INFO = "request_info";

    public static final String MAX_SYNCHRONOUS_DOWNLOAD_NUM = "max_synchronous_download_num";

    public static final int MEMORY_SIZE_ERROR = -1;

    static class ResponseCode {

        /**
         * 200——>请求成功。
         */
        static final int OK = 200;
        /**
         * 206——>请求部分数据成功。
         */
        static final int PARTIAL_CONTENT = 206;
        /**
         * 206——>请求部分数据成功。
         */
        static final int NOT_FOUND = 404;

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
