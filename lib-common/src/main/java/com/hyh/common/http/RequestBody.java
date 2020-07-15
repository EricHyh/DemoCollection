package com.hyh.common.http;

import java.io.OutputStream;

/**
 * @author Administrator
 * @description
 * @data 2019/1/3
 */

public abstract class RequestBody {

    public static RequestBody create(String contentType, String content) {
        return new StringRequestBody(contentType, content);
    }

    public abstract String contentType();

    public abstract void writeTo(OutputStream outputStream);
}
