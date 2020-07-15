package com.hyh.common.http;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * @author Administrator
 * @description
 * @data 2019/1/3
 */

public class StringRequestBody extends RequestBody {

    private String contentType;

    private String content;

    public StringRequestBody(String contentType, String content) {
        this.contentType = contentType;
        this.content = content;
    }

    @Override
    public String contentType() {
        return contentType;
    }

    @Override
    public void writeTo(OutputStream outputStream) {
        PrintWriter printWriter = new PrintWriter(outputStream);
        printWriter.write(content);
        printWriter.flush();
    }
}
