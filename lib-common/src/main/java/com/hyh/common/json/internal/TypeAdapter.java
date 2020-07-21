package com.hyh.common.json.internal;

/**
 * @author Administrator
 * @description
 * @data 2020/7/15
 */
public interface TypeAdapter<T> {

    T read(JSONElement in);

    JSONElement write(T value);

}