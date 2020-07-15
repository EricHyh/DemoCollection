package com.hyh.common.http;


import com.hyh.common.utils.StreamUtil;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Administrator
 * @description
 * @data 2019/1/3
 */

public class ResponseBody implements Closeable {

    private String contentType;

    private long contentLength;

    private InputStream inputStream;

    ResponseBody(String contentType, long contentLength, InputStream inputStream) {
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.inputStream = inputStream;
    }

    public final String contentType() {
        return contentType;
    }

    public final long contentLength() {
        return contentLength;
    }

    public final InputStream byteStream() {
        return inputStream;
    }

    public final String string() throws IOException {
        return StreamUtil.stream2String(inputStream);
    }

    public final byte[] bytes() throws IOException {
        return StreamUtil.stream2Bytes(inputStream);
    }

    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
