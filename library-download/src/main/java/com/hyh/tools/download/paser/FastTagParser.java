package com.hyh.tools.download.paser;

import com.alibaba.fastjson.JSON;

/**
 * @author Administrator
 * @description
 * @data 2017/10/14
 */

public class FastTagParser implements TagParser {

    @Override
    public String toString(Object object) {
        return JSON.toJSONString(object, true);
    }

    @Override
    public <T> T fromString(String str, Class<T> classOfT) {
        return JSON.parseObject(str, classOfT);
    }

    @Override
    public String onTagClassNameChanged(String oldClassName) {
        return oldClassName;
    }

}
