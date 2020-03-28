package com.hyh.common.instance;

/**
 * @author Administrator
 * @description
 * @data 2020/3/14
 */
public abstract class InstanceHolderP1<T, P> {

    private T instance;

    public T get(P param) {
        if (instance != null) return instance;
        synchronized (this) {
            if (instance == null) {
                instance = create(param);
            }
            return instance;
        }
    }

    protected abstract T create(P param);

}