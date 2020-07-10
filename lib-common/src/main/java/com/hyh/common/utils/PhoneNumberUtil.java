package com.hyh.common.utils;

import android.text.TextUtils;

/**
 * @author Administrator
 * @description
 * @data 2020/7/10
 */
public class PhoneNumberUtil {

    private static final String TEL_REGEX = "((13)|(17)|(14)|(15)|(18))\\d{9}";

    public static boolean isMobileNumber(String input) {
        return !TextUtils.isEmpty(input) && input.matches(TEL_REGEX);
    }
}