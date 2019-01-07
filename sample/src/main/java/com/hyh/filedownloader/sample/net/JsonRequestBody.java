package com.hyh.filedownloader.sample.net;

/**
 * @author Administrator
 * @description
 * @data 2019/1/3
 */

public class JsonRequestBody extends StringRequestBody {

    public JsonRequestBody(String content) {
        super("application/json;charset=UTF-8", content);
    }
}
