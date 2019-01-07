package com.hyh.filedownloader.sample.net;

import android.os.Build;
import android.text.TextUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2019/1/3
 */

class RawResponse {

    public static final String CONTENT_LENGTH = "Content-Length";

    public static final String CONTENT_TYPE = "Content-Type";

    private HttpURLConnection connection;

    private String url;

    private int code;

    private InputStream inputStream;

    RawResponse lastResponse;

    RawResponse(HttpURLConnection connection, String url, int code, InputStream inputStream) {
        this.connection = connection;
        this.url = url;
        this.code = code;
        this.inputStream = inputStream;
    }

    public String url() {
        return url;
    }

    public String method() {
        return connection.getRequestMethod();
    }

    public int code() {
        return code;
    }

    public String contentType() {
        return connection.getHeaderField(CONTENT_TYPE);
    }

    public long contentLength() {
        return getContentLength();
    }

    public Map<String, List<String>> headers() {
        return connection.getHeaderFields();
    }

    public String header(String name) {
        return connection.getHeaderField(name);
    }

    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }

    public InputStream inputStream() {
        return inputStream;
    }

    private long getContentLength() {
        long contentLength;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            contentLength = connection.getContentLengthLong();
        } else {
            String contentLengthStr = connection.getHeaderField(CONTENT_LENGTH);
            if (!TextUtils.isEmpty(contentLengthStr) && TextUtils.isDigitsOnly(contentLengthStr)) {
                contentLength = Long.parseLong(contentLengthStr);
            } else {
                contentLength = connection.getContentLength();
            }
        }
        return contentLength;
    }
}
