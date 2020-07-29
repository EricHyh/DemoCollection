package com.hyh.fyp.widget;

import android.support.annotation.FloatRange;
import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * @author Administrator
 * @description
 * @data 2020/7/29
 */
public class CarouselTransformer implements ViewPager.PageTransformer {
    public static final float SCALE_MIN = 0.3f;
    public static final float SCALE_MAX = 1f;
    public float scale;

    private float pagerMargin;
    private float spaceValue;

    public CarouselTransformer(@FloatRange(from = 0,to = 1) float scale, float pagerMargin, float spaceValue) {
        this.scale = scale;
        this.pagerMargin = pagerMargin;
        this.spaceValue = spaceValue;
    }

    @Override
    public void transformPage(View page, float position) {

        if (scale != 0f) {
            float realScale = getAdapter(1 - Math.abs(position * scale), SCALE_MIN, SCALE_MAX);
            page.setScaleX(realScale);
            page.setScaleY(realScale);
        }

        if (pagerMargin != 0) {

            float realPagerMargin = position * (pagerMargin);
            page.setTranslationX(realPagerMargin);
        }
    }

    private float getAdapter(float value, float minValue, float maxValue) {
        return Math.min(maxValue, Math.max(minValue, value));
    }

}