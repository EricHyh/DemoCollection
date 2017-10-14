package com.hyh.tools.download.internal.paser;

import java.lang.reflect.Type;

/**
 * @author Administrator
 * @description
 * @data 2017/10/14
 */

public class EmptyJsonParser implements JsonParser {

    @Override
    public String toJson(Object object) {
        return null;
    }

    @Override
    public <T> T fromJson(String json, Class<T> classOfT) {
        return null;
    }

    @Override
    public <T> T fromJson(String json, Type typeOfT) {
        return null;
    }

}
