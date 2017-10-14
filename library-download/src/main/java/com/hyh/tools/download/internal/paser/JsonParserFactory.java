package com.hyh.tools.download.internal.paser;

import com.hyh.tools.download.internal.Utils;

/**
 * @author Administrator
 * @description
 * @data 2017/10/14
 */

public class JsonParserFactory {

    private static final String GSON_CLASS_NAME = "com.google.gson.Gson";
    private static final String FASTJSON_CLASS_NAME = "com.alibaba.fastjson.JSON";

    public static JsonParser produce(JsonParser jsonParser) {
        if (jsonParser != null) {
            return jsonParser;
        } else if (Utils.isClassFound(GSON_CLASS_NAME)) {
            jsonParser = new GsonPaser();
        } else if (Utils.isClassFound(FASTJSON_CLASS_NAME)) {
            jsonParser = new FastJsonParser();
        } else {
            jsonParser = new EmptyJsonParser();
        }
        return jsonParser;
    }
}
