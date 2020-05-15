package com.hyh.web.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Administrator
 * @description
 * @data 2020/5/15
 */
public class MyViewPager extends ViewPager {

    private static final String TAG = "MyViewPager";


    public MyViewPager(@NonNull Context context) {
        super(context);
    }

    public MyViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean b = super.onInterceptTouchEvent(ev);
        if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
            Log.d(TAG, "onInterceptTouchEvent: b = " + b);
        }

        return b;
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        boolean canScroll = super.canScroll(v, checkV, dx, x, y);
        if (!checkV) {
            Log.d(TAG, "canScroll: " + canScroll);
        }
        return canScroll;
    }
}
