package com.hyh.filedownloader.sample.net;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2019/1/3
 */

public class Response {

    private RawResponse rawResponse;

    private String url;

    private ResponseBody responseBody;

    Response(RawResponse rawResponse, String url) {
        this.rawResponse = rawResponse;
        this.url = url;
        this.responseBody = new ResponseBody(rawResponse.contentType(), rawResponse.contentLength(), rawResponse.inputStream());
    }

    public String url() {
        return url;
    }

    public int code() {
        return rawResponse.code();
    }

    public Map<String, List<String>> headers() {
        return rawResponse.headers();
    }

    public String header(String name) {
        return rawResponse.header(name);
    }

    public boolean isSuccessful() {
        return rawResponse.isSuccessful();
    }

    public ResponseBody body() {
        return responseBody;
    }

    public Response cacheResponse() {
        return null;
    }
}
