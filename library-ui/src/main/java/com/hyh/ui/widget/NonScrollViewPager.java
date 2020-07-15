package com.hyh.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.hyh.common.log.Logger;
import com.laser.library_ui.R;


/**
 * @author Administrator
 * @description
 * @data 2019/10/19
 */

public class NonScrollViewPager extends CustomViewPager {

    private boolean mScrollEnabled = true;

    private boolean mUseScrollEnabled = false;

    public NonScrollViewPager(Context context) {
        this(context, null);
    }

    public NonScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NonScrollViewPager);
            mScrollEnabled =
                    typedArray.getBoolean(R.styleable.NonScrollViewPager_scrollEnabled, true);
            typedArray.recycle();
        }
    }

    public void setScrollEnable(boolean enable) {
        this.mScrollEnabled = enable;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            mUseScrollEnabled = mScrollEnabled;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mUseScrollEnabled) {
            try {
                return super.onInterceptTouchEvent(ev);
            } catch (IllegalArgumentException e) {
                Logger.d("NonScrollViewPager onInterceptTouchEvent error: ", e);
            }
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mUseScrollEnabled) {
            try {
                return super.onTouchEvent(ev);
            } catch (IllegalArgumentException e) {
                Logger.d("NonScrollViewPager onTouchEvent error: ", e);
            }
        }
        return false;
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return mUseScrollEnabled && super.canScrollHorizontally(direction);
    }
}