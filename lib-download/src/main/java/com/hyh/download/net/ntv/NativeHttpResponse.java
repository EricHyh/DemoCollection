package com.hyh.download.net.ntv;


import android.os.Build;
import android.text.TextUtils;

import com.hyh.download.net.HttpResponse;
import com.hyh.download.utils.NetworkHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * @author Administrator
 * @description
 * @data 2017/10/14
 */

public class NativeHttpResponse implements HttpResponse {

    private HttpURLConnection connection;

    private InputStream inputStream;

    NativeHttpResponse(HttpURLConnection connection) {
        this.connection = connection;
    }

    @Override
    public int code() {
        try {
            return connection.getResponseCode();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public String getUrl() {
        return connection.getURL().toString();
    }

    @Override
    public InputStream inputStream() throws IOException {
        if (inputStream != null) {
            return inputStream;
        }
        inputStream = connection.getInputStream();
        return inputStream;
    }

    @Override
    public String header(String name) {
        return connection.getHeaderField(name);
    }

    @Override
    public long contentLength() {
        return getContentLength();
    }

    @Override
    public void close() throws IOException {
        if (inputStream == null) {
            inputStream = connection.getInputStream();
        }
        if (inputStream != null) {
            inputStream.close();
        }
    }

    private long getContentLength() {
        long contentLength;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            contentLength = connection.getContentLengthLong();
        } else {
            String contentLengthStr = connection.getHeaderField(NetworkHelper.CONTENT_LENGTH);
            if (!TextUtils.isEmpty(contentLengthStr) && TextUtils.isDigitsOnly(contentLengthStr)) {
                contentLength = Long.parseLong(contentLengthStr);
            } else {
                contentLength = connection.getContentLength();
            }
        }
        if (contentLength <= 0) {
            String contentRange = connection.getHeaderField(NetworkHelper.CONTENT_RANGE);
            contentLength = NetworkHelper.parseContentLengthFromContentRange(contentRange);
        }
        return contentLength;
    }
}
