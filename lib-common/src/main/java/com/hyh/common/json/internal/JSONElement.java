package com.hyh.common.json.internal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONElement {

    private final String key;
    private final Object value;

    public JSONElement(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public JSONElement(String key, JSONObject object) {
        this.key = key;
        this.value = object;
    }

    public JSONElement(String key, JSONArray array) {
        this.key = key;
        this.value = array;
    }

    void write(JSONObject parent) throws JSONException {
        parent.put(key, value);
    }
}