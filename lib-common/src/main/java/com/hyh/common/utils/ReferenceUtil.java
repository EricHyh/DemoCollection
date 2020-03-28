package com.hyh.common.utils;

import java.lang.ref.Reference;

/**
 * @author Administrator
 * @description
 * @data 2018/9/19
 */

public class ReferenceUtil {

    public static <T> void checkReference(Reference<T> reference, ReferenceRunnable<T> runnable) {
        if (reference == null) {
            return;
        }
        T t = reference.get();
        if (t != null && runnable != null) {
            runnable.run(t);
        }
    }

    public interface ReferenceRunnable<T> {

        void run(T t);

    }
}