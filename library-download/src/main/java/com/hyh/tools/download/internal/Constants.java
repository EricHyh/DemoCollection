package com.hyh.tools.download.internal;

/**
 * Created by Eric_He on 2017/3/10.
 */

public class Constants {

    static final String COMMADN = "command";

    static final String REQUEST_INFO = "request_info";

    static final String MAX_SYNCHRONOUS_DOWNLOAD_NUM = "max_synchronous_download_num";

    static final int MEMORY_SIZE_ERROR = -1;

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

    }

    public static class ThirdLibraryClassName {
        public static final String GSON_CLASS_NAME = "com.google.gson.Gson";
        public static final String FASTJSON_CLASS_NAME = "com.alibaba.fastjson.JSON";
        public static final String OKHTTP_CLASS_NAME = "okhttp3.OkHttpClient";
    }
}
