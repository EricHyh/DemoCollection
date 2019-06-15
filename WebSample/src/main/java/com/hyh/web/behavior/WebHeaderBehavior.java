package com.hyh.web.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Administrator
 * @description
 * @data 2019/5/29
 */

public class WebHeaderBehavior extends BaseBehavior<View> {

    private static final String TAG = "WebHeaderBehavior";
    private boolean mIsActionDownTouched;
    private float mLastTouchY;


    public WebHeaderBehavior(Context context) {
        super(context);
    }

    public WebHeaderBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getBehaviorType() {
        return WEB_HEADER_BEHAVIOR;
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, View child, MotionEvent ev) {
        int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mIsActionDownTouched = isTouched(child, ev);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                mIsActionDownTouched = false;
                break;
            }
        }
        return super.onInterceptTouchEvent(parent, child, ev) || mIsActionDownTouched;
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, View child, MotionEvent ev) {
        int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mLastTouchY = ev.getY();
                mTempHeaderHeight = null;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float curY = ev.getY();
                float dy = curY - mLastTouchY;
                mLastTouchY = curY;

                int scrollDy = -Math.round(dy);


                int scrollY = parent.getScrollY();
                if (scrollDy > 0) {
                    int curMaxScrollDy = getHeaderHeight(parent) - scrollY;
                    if (scrollDy > curMaxScrollDy) {
                        scrollDy = curMaxScrollDy;
                    }
                } else {
                    int curMinScrollDy = -scrollY;
                    if (scrollDy < curMinScrollDy) {
                        scrollDy = curMinScrollDy;
                    }
                }
                parent.scrollBy(0, scrollDy);
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                break;
            }
        }
        return mIsActionDownTouched;
    }
}