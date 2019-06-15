package com.hyh.web.behavior;

import android.content.Context;
import android.support.v4.view.NestedScrollingChildHelper;
import android.util.AttributeSet;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.WebView;

/**
 * @author Administrator
 * @description
 * @data 2019/6/10
 */

public class WebViewBehavior extends BaseBehavior<WebView> {

    private final int mMaximumFlingVelocity;
    private final int mTouchSlop;
    private final float mDensity;
    private NestedScrollingChildHelper mChildHelper;
    private boolean mIsActionDownTouched;

    private int mInitialTouchX;
    private int mInitialTouchY;
    private int mLastTouchX;
    private int mLastTouchY;

    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private final int[] mNestedOffsets = new int[2];

    private VelocityTracker mVelocityTracker;

    public WebViewBehavior(Context context) {
        this(context, null);
    }

    public WebViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        mTouchSlop = configuration.getScaledTouchSlop();
        mDensity = context.getResources().getDisplayMetrics().density;
    }

    /*@Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, WebView child, MotionEvent ev) {
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
    public boolean onTouchEvent(CoordinatorLayout parent, WebView child, MotionEvent ev) {
        int action = ev.getActionMasked();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        if (action == MotionEvent.ACTION_DOWN) {
            mVelocityTracker.clear();
        }
        mVelocityTracker.addMovement(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mInitialTouchX = mLastTouchX = Math.round(ev.getX());
                mInitialTouchY = mLastTouchY = Math.round(ev.getY());
                getChildHelper(child).startNestedScroll(SCROLL_AXIS_VERTICAL, TYPE_TOUCH);
                child.dispatchTouchEvent(ev);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int x = Math.round(ev.getX());
                int y = Math.round(ev.getY());
                int dx = mLastTouchX - x;
                int dy = mLastTouchY - y;
                mLastTouchX = x;
                mLastTouchY = y;
                if (getChildHelper(child).dispatchNestedPreScroll(0, dy, mScrollConsumed, mScrollOffset, TYPE_TOUCH)) {
                    dx -= mScrollConsumed[0];
                    dy -= mScrollConsumed[1];
                    int webScrollY = dy;
                    if (dy > 0) {//向上滑动
                        int curMaxScrollY = getWebContentHeight(child) - child.getScrollY();
                        webScrollY = Math.min(webScrollY, curMaxScrollY);
                    } else {//向下滑动
                        int curMinScrollY = -child.getScrollY();
                        webScrollY = Math.max(webScrollY, curMinScrollY);
                    }
                    child.scrollBy(0, webScrollY);
                    dy -= webScrollY;
                    if (dy != 0) {
                        getChildHelper(child).dispatchNestedScroll(dx, webScrollY, 0, dy, mScrollOffset, TYPE_TOUCH);
                    }
                } else {
                    int webScrollY = dy;
                    if (dy > 0) {//向上滑动
                        int curMaxScrollY = getWebContentHeight(child) - child.getScrollY();
                        webScrollY = Math.min(webScrollY, curMaxScrollY);
                    } else {//向下滑动
                        int curMinScrollY = -child.getScrollY();
                        webScrollY = Math.max(webScrollY, curMinScrollY);
                    }
                    child.scrollBy(0, webScrollY);
                }
                if (Math.abs(mInitialTouchY - y) > mTouchSlop) {
                    //屏蔽WebView本身的滑动，滑动事件自己处理
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                }
                child.dispatchTouchEvent(ev);
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                child.dispatchTouchEvent(ev);
                break;
            }
        }
        return mIsActionDownTouched || super.onTouchEvent(parent, child, ev);
    }*/

    private int getWebContentHeight(WebView child) {
        return Math.round(child.getContentHeight() * mDensity);
    }

    @Override
    protected int getBehaviorType() {
        return WEB_VIEW_BEHAVIOR;
    }

    private NestedScrollingChildHelper getChildHelper(WebView view) {
        if (mChildHelper != null) return mChildHelper;
        view.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mChildHelper = new NestedScrollingChildHelper(view);
        mChildHelper.setNestedScrollingEnabled(true);
        return mChildHelper;
    }
}