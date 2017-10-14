package com.hyh.tools.download.internal.paser;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * @author Administrator
 * @description
 * @data 2017/10/14
 */

public class GsonPaser implements JsonParser {

    private Gson gson = new Gson();

    @Override
    public String toJson(Object object) {
        return gson.toJson(object);
    }

    @Override
    public <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    @Override
    public <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }
}
