package com.hyh.tools.download.net;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public interface HttpClient {

    HttpCall newCall(String tag, String url, long oldSize);

    HttpCall newCall(String tag, String url, long startPosition, long endPosition);

    HttpResponse getHttpResponse(String url) throws Exception;
}
