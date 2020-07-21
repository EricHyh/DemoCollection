package com.hyh.common.json.internal;

import com.hyh.common.json.AJson;
import com.hyh.common.json.TypeToken;

/**
 * @author Administrator
 * @description
 * @data 2020/7/21
 */
public interface TypeAdapterFactory {

    <T> TypeAdapter<T> create(AJson aJson, TypeToken<T> type);

}