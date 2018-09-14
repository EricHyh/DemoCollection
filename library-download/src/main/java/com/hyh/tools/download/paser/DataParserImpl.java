package com.hyh.tools.download.paser;

import android.text.TextUtils;

/**
 * @author Administrator
 * @description
 * @data 2018/3/1
 */

public class DataParserImpl implements TagParser {

    private TagParser mParser;

    public DataParserImpl(TagParser parser) {
        mParser = parser;
    }

    @Override
    public String toString(Object object) {
        if (object == null) {
            return null;
        }
        if (SimpleDataHelper.isSimpleData(object)) {
            return object.toString();
        }
        return mParser.toString(object);
    }

    @Override
    public <T> T fromString(String json, Class<T> classOfT) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        if (SimpleDataHelper.isSimpleData(classOfT)) {
            return null;
        }
        return null;
    }

    @Override
    public String onTagClassNameChanged(String oldClassName) {
        return mParser.onTagClassNameChanged(oldClassName);
    }
}
