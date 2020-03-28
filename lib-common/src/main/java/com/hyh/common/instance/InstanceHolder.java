package com.hyh.common.instance;

/**
 * @author Administrator
 * @description
 * @data 2020/3/14
 */
public abstract class InstanceHolder<T> {

    private T instance;

    public T get() {
        if (instance != null) return instance;
        synchronized (this) {
            if (instance == null) {
                instance = create();
            }
            return instance;
        }
    }

    protected abstract T create();
}