package com.hyh.plg.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2016/11/28.
 */

public class StreamUtil {

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
                Logger.d(closeable.getClass().getName() + "close success");
            } catch (Exception e) {
                e.printStackTrace();
                Logger.d(closeable.getClass().getName() + "close failed");
            }
        }
    }

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


    public static String stream2String(InputStream inputStream) throws IOException {
        String result = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;
        try {
            if (inputStream != null) {
                bis = new BufferedInputStream(inputStream);
                baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = bis.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                    baos.flush();
                }
                result = baos.toString("utf-8");
            }
        } finally {
            close(baos, bis, inputStream);
        }
        return result;
    }


    /**
     * @param file 文件
     * @return 返回字符串
     * @des 将一个文件中的内容转化为字符串
     */
    public static String file2String(File file) {
        if (file == null) {
            throw new NullPointerException();
        }
        if (!file.exists()) {
            throw new RuntimeException("this file is not exists!!");
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            return stream2String(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
