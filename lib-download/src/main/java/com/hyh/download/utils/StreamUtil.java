package com.hyh.download.utils;

import java.io.Closeable;

/**
 * @author Administrator
 * @description
 * @data 2018/12/20
 */

public class StreamUtil {


    public static void close(Closeable... closeables) {
        if (closeables == null || closeables.length <= 0) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable == null) {
                continue;
            }
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
