package com.hyh.download.net;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public interface HttpClient {

    HttpCall newCall(String tag, String url, long startPosition);

    HttpCall newCall(String tag, String url, long startPosition, long endPosition);

    HttpResponse execute(String url) throws Exception;
}
