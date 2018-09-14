package com.hyh.tools.download.paser;

/**
 * @author Administrator
 * @description
 * @data 2017/10/14
 */

public interface TagParser {


    String toString(Object object);

    <T> T fromString(String str, Class<T> classOfT);

    String onTagClassNameChanged(String oldClassName);

}
