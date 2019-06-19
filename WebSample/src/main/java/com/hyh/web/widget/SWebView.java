package com.hyh.web.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.webkit.WebView;

/**
 * @author Administrator
 * @description
 * @data 2019/6/15
 */

public class SWebView extends WebView {

    private static final String TAG = "SWebView";

    private VelocityTracker mVelocityTracker = VelocityTracker.obtain();
    private final int mMinFlingVelocity;
    private final int mMaxFlingVelocity;

    public SWebView(Context context) {
        this(context, null);
    }

    public SWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mMinFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean b = super.onInterceptTouchEvent(ev);
        return b;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mVelocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP: {
                int scrollY = getScrollY();
                Log.d(TAG, "onTouchEvent: ACTION_UP scrollY = " + scrollY);
                mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
                final float velocityX = -mVelocityTracker.getXVelocity();
                final float velocityY = -mVelocityTracker.getYVelocity();
                Log.d(TAG, "onTouchEvent: ACTION_UP velocityY = " + velocityY);
                recycleVelocityTracker();
                //flingScroll(0,0);
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                recycleVelocityTracker();
                break;
            }
        }
        return super.onTouchEvent(event);
    }

    private void recycleVelocityTracker() {
        mVelocityTracker.clear();
    }

    /*@Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        Log.d(TAG, "overScrollBy: start---------------------------------------------");
        Log.d(TAG, "overScrollBy: deltaX = " + deltaX);
        Log.d(TAG, "overScrollBy: deltaY = " + deltaY);
        Log.d(TAG, "overScrollBy: scrollX = " + scrollX);
        Log.d(TAG, "overScrollBy: scrollY = " + scrollY);
        Log.d(TAG, "overScrollBy: scrollRangeX = " + scrollRangeX);
        Log.d(TAG, "overScrollBy: scrollRangeY = " + scrollRangeY);
        Log.d(TAG, "overScrollBy: maxOverScrollX = " + maxOverScrollX);
        Log.d(TAG, "overScrollBy: maxOverScrollY = " + maxOverScrollY);
        Log.d(TAG, "overScrollBy: isTouchEvent = " + isTouchEvent);
        Log.d(TAG, "overScrollBy: end---------------------------------------------");
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }*/

    @Override
    public void flingScroll(int vx, int vy) {
        super.flingScroll(vx, vy);
        Log.d(TAG, "flingScroll: vx = " + vx + ", vy = " + vy);
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        Log.d(TAG, "onScrollChanged: " + getScrollY());
    }
}