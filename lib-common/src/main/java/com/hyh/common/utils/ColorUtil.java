package com.hyh.common.utils;

import android.graphics.Color;

/**
 * @author Administrator
 * @description
 * @data 2019/9/23
 */

public class ColorUtil {

    // 获取更深颜色
    public static int getDarkerColor(int color, float degree) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv); // convert to hsv
        // make darker
        hsv[1] = hsv[1] + degree; // 饱和度更高
        hsv[2] = hsv[2] - degree; // 明度降低
        return Color.HSVToColor(hsv);
    }

    // 获取更浅的颜色
    public static int getBrighterColor(int color, float degree) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv); // convert to hsv

        hsv[1] = hsv[1] - degree; // less saturation
        hsv[2] = hsv[2] + degree; // more brightness
        return Color.HSVToColor(hsv);
    }
}
