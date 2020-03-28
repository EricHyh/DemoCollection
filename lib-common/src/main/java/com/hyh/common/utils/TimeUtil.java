package com.hyh.common.utils;

import java.util.Calendar;

/**
 * @author Administrator
 * @description
 * @data 2019/7/10
 */

public class TimeUtil {

    public static boolean isSameDay(long timeMillis) {
        return isSameDay(timeMillis, System.currentTimeMillis());
    }

    public static boolean isSameDay(long timeMillis1, long timeMillis2) {
        if (timeMillis1 > 0 && timeMillis2 > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeMillis1);
            int year1 = calendar.get(Calendar.YEAR);
            int month1 = calendar.get(Calendar.MONTH);
            int day1 = calendar.get(Calendar.DATE);

            calendar.setTimeInMillis(timeMillis2);
            int year2 = calendar.get(Calendar.YEAR);
            int month2 = calendar.get(Calendar.MONTH);
            int day2 = calendar.get(Calendar.DATE);
            return year1 == year2 && month1 == month2 && day1 == day2;
        } else {
            return false;
        }
    }

    public static boolean isInIntervalDays(long timeMillis1, long timeMillis2, int days) {
        if (timeMillis1 > 0 && timeMillis2 > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeMillis1);
            int year1 = calendar.get(Calendar.YEAR);
            int month1 = calendar.get(Calendar.MONTH);
            int day1 = calendar.get(Calendar.DATE);

            calendar.setTimeInMillis(timeMillis2);
            int year2 = calendar.get(Calendar.YEAR);
            int month2 = calendar.get(Calendar.MONTH);
            int day2 = calendar.get(Calendar.DATE);
            return year1 == year2 && month1 == month2 && Math.abs(day1 - day2) <= days;
        } else {
            return false;
        }
    }

    public static long hourToMillionSecond(float hour) {
        return (long) (hour * 60 * 60 * 1000);
    }
}