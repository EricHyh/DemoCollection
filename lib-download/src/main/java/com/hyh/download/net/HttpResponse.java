package com.hyh.download.net;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2017/5/17
 */

public interface HttpResponse extends Closeable {

    int code();

    boolean isSuccessful();

    String url();

    InputStream inputStream() throws IOException;

    Map<String, List<String>> headers();

    String header(String name);

    long contentLength();

    void close() throws IOException;


}
