package com.hyh.tools.download.paser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Administrator
 * @description
 * @data 2017/10/14
 */

public class GsonPaser implements TagParser {

    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    @Override
    public String toString(Object object) {
        return gson.toJson(object);
    }

    @Override
    public <T> T fromString(String str, Class<T> classOfT) {
        return gson.fromJson(str, classOfT);
    }

    @Override
    public String onTagClassNameChanged(String oldClassName) {
        return oldClassName;
    }

}
