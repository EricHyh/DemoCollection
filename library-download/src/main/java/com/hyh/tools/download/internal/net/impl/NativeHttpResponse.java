package com.hyh.tools.download.internal.net.impl;

import com.hyh.tools.download.api.HttpResponse;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Administrator
 * @description
 * @data 2017/10/14
 */

public class NativeHttpResponse implements HttpResponse {


    private InputStream inputStream;
    private long contentLength;
    private int responseCode;

    public NativeHttpResponse(InputStream inputStream, long contentLength, int responseCode) {
        this.inputStream = inputStream;
        this.contentLength = contentLength;
        this.responseCode = responseCode;
    }

    @Override
    public int code() {
        return responseCode;
    }

    @Override
    public InputStream inputStream() {
        return inputStream;
    }

    @Override
    public long contentLength() {
        return contentLength;
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
