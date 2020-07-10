package com.hyh.common.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author Administrator
 * @description
 * @data 2018/3/26
 */

public class StreamUtil {


    public static String stream2String(InputStream inputStream) throws IOException {
        String result = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;
        try {
            if (inputStream != null) {
                bis = new BufferedInputStream(inputStream);
                baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[32 * 1024];
                int len;
                while ((len = bis.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                    baos.flush();
                }
                result = baos.toString("utf-8");
            }
        } finally {
            close(baos, bis);
        }
        return result;
    }


    public static byte[] stream2Bytes(InputStream inputStream) throws IOException {
        byte[] result = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;
        try {
            if (inputStream != null) {
                bis = new BufferedInputStream(inputStream);
                baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[32 * 1024];
                int len;
                while ((len = bis.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                    baos.flush();
                }
                result = baos.toByteArray();
            }
        } finally {
            close(baos, bis);
        }
        return result;
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
                //;
            }
        }
    }

    @Deprecated
    public static boolean copyFileToTargetPath(InputStream inputStream, String targetPath) {
        return streamToFile(inputStream, targetPath);
    }


    public static boolean streamToFile(InputStream inputStream, String targetPath) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(inputStream);
            bos = new BufferedOutputStream(new FileOutputStream(targetPath, false));
            byte[] buffer = new byte[32 * 1024];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            bos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            StreamUtil.close(bos, bis);
        }
        return false;
    }

    public static void streamToFileWithError(InputStream inputStream, String targetPath) throws IOException {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(inputStream);
            bos = new BufferedOutputStream(new FileOutputStream(targetPath, false));
            byte[] buffer = new byte[32 * 1024];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            bos.flush();
        } finally {
            StreamUtil.close(bos, bis);
        }
    }

    /**
     * 解压字符串内容
     */
    public static String unGZIP(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        GZIPInputStream gunzip = null;
        ByteArrayOutputStream out = null;
        try {
            gunzip = new GZIPInputStream(new ByteArrayInputStream(str.getBytes("ISO-8859-1")));
            out = new ByteArrayOutputStream();
            byte[] buffer = new byte[256];
            int len;
            while ((len = gunzip.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(out);
            close(gunzip);
        }
        if (out != null) {
            return out.toString();
        } else {
            return null;
        }
    }

    /**
     * 解压缩
     */
    public static byte[] unGZIP(InputStream ins) {
        GZIPInputStream gis = null;
        ByteArrayOutputStream baos = null;
        try {
            gis = new GZIPInputStream(ins);
            baos = new ByteArrayOutputStream();
            int b;
            while ((b = gis.read()) != -1) {
                baos.write(b);
            }
            byte[] result = baos.toByteArray();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(baos);
            close(gis);
        }
        return null;
    }
}