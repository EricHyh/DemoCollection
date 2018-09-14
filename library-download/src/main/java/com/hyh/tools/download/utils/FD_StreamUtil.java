package com.hyh.tools.download.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Administrator
 * @description
 * @data 2018/2/7
 */

public class FD_StreamUtil {


    public static void close(Closeable... closeables) {
        if (closeables == null || closeables.length == 0) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
