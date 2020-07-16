package com.hyh.common.json;

import com.hyh.common.json.exception.JsonSyntaxException;

import java.lang.reflect.Type;

/**
 * @author Administrator
 * @description
 * @data 2020/7/15
 */
public class AJson {

    public AJson() {
    }

    public <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {

        /*Field field = classOfT.getDeclaredField();
        field.*/


        return null;
    }

    public <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException {

        return null;
    }

    public String toJson(Object src) {
        return null;
    }
}