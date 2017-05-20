package com.eric.hyh.tools.download.api;

import java.io.IOException;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public interface HttpCallback {

    void onFailure(HttpCall httpCall, IOException e);

    void onResponse(HttpCall httpCall, HttpResponse httpResponse) throws IOException;

}
