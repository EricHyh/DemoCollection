package com.hyh.tools.download.internal.paser;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Type;

/**
 * @author Administrator
 * @description
 * @data 2017/10/14
 */

public class FastJsonParser implements JsonParser {

    @Override
    public String toJson(Object object) {
        return JSON.toJSONString(object, true);
    }

    @Override
    public <T> T fromJson(String json, Class<T> classOfT) {
        return JSON.parseObject(json, classOfT);
    }

    @Override
    public <T> T fromJson(String json, Type typeOfT) {
        return JSON.parseObject(json, typeOfT);
    }
}
