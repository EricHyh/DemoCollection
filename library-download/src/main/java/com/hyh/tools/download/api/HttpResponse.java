package com.hyh.tools.download.api;

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

    long contentLength();

    void close() throws IOException;

}
