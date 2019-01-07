package com.hyh.filedownloader.sample.net;

/**
 * @author Administrator
 * @description
 * @data 2019/1/3
 */

public interface Converter<T> {

    T convert(ResponseBody value);

}
