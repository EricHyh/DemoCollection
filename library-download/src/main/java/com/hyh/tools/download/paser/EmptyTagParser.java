package com.hyh.tools.download.paser;

/**
 * @author Administrator
 * @description
 * @data 2017/10/14
 */

public class EmptyTagParser implements TagParser {

    @Override
    public String toString(Object object) {
        return null;
    }

    @Override
    public <T> T fromString(String json, Class<T> classOfT) {
        return null;
    }

    @Override
    public String onTagClassNameChanged(String oldClassName) {
        return oldClassName;
    }

}
