package com.hyh.tools.download.utils;

/**
 * @author Administrator
 * @description
 * @data 2018/2/28
 */

public class FD_NumberUtil {

    public static int getValue(Integer integer, int defaultValue) {
        return integer == null ? defaultValue : integer;
    }

    public static long getValue(Long l, long defaultValue) {
        return l == null ? defaultValue : l;
    }

    public static boolean getValue(Boolean b, boolean defaultValue) {
        return b == null ? defaultValue : b;
    }
}
