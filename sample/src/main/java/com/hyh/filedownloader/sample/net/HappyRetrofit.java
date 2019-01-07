package com.hyh.filedownloader.sample.net;

/**
 * @author Administrator
 * @description
 * @data 2019/1/3
 */

public class HappyRetrofit {

    private static final HappyRetrofit sInstance = new HappyRetrofit();

    public static HappyRetrofit getInstance() {
        return sInstance;
    }

    public <T> T create(Class<T> service) {
        return null;
    }
}
