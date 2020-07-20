package com.hyh.common.json.internal;

/**
 * @author Administrator
 * @description
 * @data 2020/7/15
 */
interface TypeAdapter<T> {

    T read(JSONElement in);


    void write(JSONElement out, T value);

}