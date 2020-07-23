package com.hyh.plg.core;


import com.hyh.plg.reflect.Reflect;

/**
 * Created by tangdongwei on 2018/12/24.
 */
public class ParamWrapper {

    private Object original;
    private Object proxy;

    public ParamWrapper(Object original, Object proxy) {
        this.original = original;
        this.proxy = proxy;
    }

    public <T> T getTypedOriginal(Class<T> type) {
        return Reflect.safeCast(original, type);
    }

    public <T> T getTypedProxy(Class<T> type) {
        return Reflect.safeCast(proxy, type);
    }
}