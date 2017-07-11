package com.eric.hyh.tools.download.internal;

/**
 * Created by Eric_He on 2017/3/10.
 */

public class Constans {

    public static final String COMMADN = "command";

    public static final String REQUEST_INFO = "request_info";

    public static final String MAX_SYNCHRONOUS_DOWNLOAD_NUM = "max_synchronous_download_num";

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

}
