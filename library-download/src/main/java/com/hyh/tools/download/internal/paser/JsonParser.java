package com.hyh.tools.download.internal.paser;

import java.lang.reflect.Type;

/**
 * @author Administrator
 * @description
 * @data 2017/10/14
 */

public interface JsonParser {


    String toJson(Object object);

    <T> T fromJson(String json, Class<T> classOfT);


    <T> T fromJson(String json, Type typeOfT);

}
