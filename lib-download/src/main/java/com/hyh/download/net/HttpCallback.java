package com.hyh.download.net;

import java.io.IOException;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public interface HttpCallback {

    void onResponse(HttpCall httpCall, HttpResponse httpResponse) throws IOException;

    void onFailure(HttpCall httpCall, Exception e);

}
