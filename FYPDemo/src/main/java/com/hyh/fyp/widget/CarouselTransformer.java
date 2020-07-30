package com.hyh.fyp.widget;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewParent;

/**
 * @author Administrator
 * @description
 * @data 2020/7/29
 */
public class CarouselTransformer implements ViewPager.PageTransformer {

    private float scale;

    public CarouselTransformer(@FloatRange(from = 0, to = 1) float scale) {
        this.scale = scale;
    }

    @Override
    public void transformPage(@NonNull View view, float position) {
        float scale = 1 - Math.abs(position * (1 - this.scale));
        view.setScaleX(scale);
        view.setScaleY(scale);

        int parentWidth = getParentWidth(view);
        int width = view.getWidth();


        float margin = parentWidth - width;
        float realMargin = margin + width * (1 - this.scale) * Math.abs(position);
        float translationX = width - realMargin * 0.5f;

        view.setTranslationX(translationX * -position);
    }

    private int getParentWidth(@NonNull View view) {
        ViewParent parent = view.getParent();
        if (parent instanceof View) {
            View parentView = (View) parent;
            parent = parentView.getParent();
            if (parent instanceof View) {
                parentView = (View) parent;
                return parentView.getWidth();
            } else {
                return parentView.getWidth();
            }
        }
        return view.getWidth();
    }
}