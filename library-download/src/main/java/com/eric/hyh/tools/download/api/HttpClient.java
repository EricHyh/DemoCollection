package com.eric.hyh.tools.download.api;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public interface HttpClient {

    HttpCall newCall(String resKey, String url, long oldSize);

}
