package com.eric.hyh.tools.download.api;

import java.io.IOException;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public interface HttpClient {

    HttpCall newCall(String tag, String url, long oldSize);

    HttpCall newCall(String tag, String url, long startPosition, long endPosition);

    long getContentLength(String url) throws IOException;
}
