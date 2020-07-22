package com.hyh.common.json;

import com.hyh.common.json.internal.TypeAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * @author Administrator
 * @description
 * @data 2020/7/15
 */
public class AJson {

    public AJson() {
    }

    public <T> T fromJson(String json, Class<T> classOfT) throws JSONException {
        return null;
    }

    public <T> T fromJson(String json, Type typeOfT) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        Iterator<String> keys = jsonObject.keys();
        //Object opt = jsonObject.opt();

        //new JSONObject().optBoolean()
        return null;
    }

    public String toJson(Object src) {
        return null;
    }

    public <T> TypeAdapter<T> getAdapter(TypeToken<T> type) {
        return null;
    }
}