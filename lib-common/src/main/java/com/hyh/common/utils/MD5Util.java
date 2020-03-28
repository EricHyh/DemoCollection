package com.hyh.common.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * @author Administrator
 * @description
 * @data 2018/12/24
 */

public class MD5Util {

    public static String string2MD5(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                String s = Integer.toHexString(0xff & bytes[i]);
                if (s.length() == 1) {
                    sb.append("0").append(s);
                } else {
                    sb.append(s);
                }
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return input;
    }

    public static String fileToMD5(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return "";
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[8 * 1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            StreamUtil.close(in);
        }
        if (digest != null) {
            return bytesToHexString(digest.digest());
        }
        return "";
    }

    public static String fileToMD5(String filePath) {
        return fileToMD5(new File(filePath));
    }


    public static String streamToMD5(InputStream inputStream) {
        if (inputStream == null) {
            return "";
        }
        MessageDigest digest = null;
        BufferedInputStream in = null;
        byte buffer[] = new byte[8 * 1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new BufferedInputStream(inputStream);
            while ((len = in.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            StreamUtil.close(in);
        }
        if (digest != null) {
            return bytesToHexString(digest.digest());
        }
        return "";
    }


    private static String bytesToHexString(byte[] src) {
        StringBuilder sb = new StringBuilder();
        if (src == null || src.length <= 0) {
            return "";
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                sb.append(0);
            }
            sb.append(hv);
        }
        return sb.toString();
    }



}
