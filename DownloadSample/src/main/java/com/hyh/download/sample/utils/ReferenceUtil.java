package com.hyh.download.sample.utils;

import android.support.annotation.NonNull;

import java.lang.ref.Reference;

/**
 * Created by Eric_He on 2019/1/8.
 */

public class ReferenceUtil {

    public static <T> boolean checkReference(Reference<T> reference, ReferenceListener<T> listener) {
        if (reference == null) {
            return false;
        }
        T t = reference.get();
        if (t == null) {
            return false;
        }
        if (listener != null) {
            listener.nonNul(t);
        }
        return true;
    }

    public interface ReferenceListener<T> {

        void nonNul(@NonNull T t);
    }
}
