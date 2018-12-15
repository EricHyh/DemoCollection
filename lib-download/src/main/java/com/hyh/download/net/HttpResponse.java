package com.hyh.download.net;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Administrator
 * @description
 * @data 2017/5/17
 */

public interface HttpResponse extends Closeable {

    int code();

    InputStream inputStream();

    String header(String name);

    long contentLength();

    void close() throws IOException;

}
