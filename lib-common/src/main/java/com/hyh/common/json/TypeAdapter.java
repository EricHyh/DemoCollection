package com.hyh.common.json;

import org.json.JSONObject;

/**
 * @author Administrator
 * @description
 * @data 2020/7/15
 */
interface TypeAdapter<T> {

    T read(JSONObject in, String key);



    void write(JSONObject out, String key, T value);

}