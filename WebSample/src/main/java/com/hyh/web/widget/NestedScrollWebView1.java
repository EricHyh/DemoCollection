package com.hyh.web.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingChild2;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.webkit.WebView;
import android.widget.OverScroller;

import static android.support.v4.view.ViewCompat.TYPE_NON_TOUCH;
import static android.support.v4.view.ViewCompat.TYPE_TOUCH;

/**
 * @author Administrator
 * @description
 * @data 2019/6/10
 */

public class NestedScrollWebView1 extends WebView implements NestedScrollingChild2 {

    private static final String TAG = "NestedScrollWebView";

    /**
     * The RecyclerView is not currently scrolling.
     */
    public static final int SCROLL_STATE_IDLE = 0;

    /**
     * The RecyclerView is currently being dragged by outside input such as user touch input.
     */
    public static final int SCROLL_STATE_DRAGGING = 1;

    /**
     * The RecyclerView is currently animating to a final position while not under
     * outside control.
     */
    public static final int SCROLL_STATE_SETTLING = 2;

    private final NestedScrollingChildHelper mChildHelper = new NestedScrollingChildHelper(this);
    private final ViewFlingHelper mViewFlingHelper = new ViewFlingHelper();
    private final int mMaximumFlingVelocity;
    private final int mTouchSlop;
    private final float mDensity;

    private int mInitialTouchX;
    private int mInitialTouchY;
    private int mLastTouchX;
    private int mLastTouchY;

    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private final int[] mNestedOffsets = new int[2];

    private VelocityTracker mVelocityTracker;

    public NestedScrollWebView1(Context context) {
        this(context, null);
    }

    public NestedScrollWebView1(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedScrollWebView1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setNestedScrollingEnabled(true);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        mTouchSlop = configuration.getScaledTouchSlop();
        mDensity = context.getResources().getDisplayMetrics().density;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        if (action == MotionEvent.ACTION_DOWN) {
            mVelocityTracker.clear();
            mViewFlingHelper.stop();
        }
        mVelocityTracker.addMovement(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mInitialTouchX = mLastTouchX = Math.round(event.getRawX());
                mInitialTouchY = mLastTouchY = Math.round(event.getRawY());
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int x = Math.round(event.getRawX());
                int y = Math.round(event.getRawY());
                int dx = mLastTouchX - x;
                int dy = mLastTouchY - y;
                mLastTouchX = x;
                mLastTouchY = y;

                int webScrollY = 0;
                if (dispatchNestedPreScroll(0, dy, mScrollConsumed, mScrollOffset, ViewCompat.TYPE_TOUCH)) {
                    dx -= mScrollConsumed[0];
                    dy -= mScrollConsumed[1];
                    webScrollY = dy;
                    if (dy > 0) {//向上滑动
                        int curMaxScrollY = onScrollDownMaxScrollY();
                        webScrollY = Math.min(webScrollY, curMaxScrollY);
                    } else {//向下滑动
                        int curMinScrollY = onScrollUpMinScrollY();
                        webScrollY = Math.max(webScrollY, curMinScrollY);
                    }
                    scrollBy(0, webScrollY);
                    dy -= webScrollY;
                } else {
                    webScrollY = dy;
                    if (dy > 0) {//向上滑动
                        int curMaxScrollY = onScrollDownMaxScrollY();
                        webScrollY = Math.min(webScrollY, curMaxScrollY);
                    } else {//向下滑动
                        int curMinScrollY = onScrollUpMinScrollY();
                        webScrollY = Math.max(webScrollY, curMinScrollY);
                    }
                    scrollBy(0, webScrollY);
                }
                dispatchNestedScroll(dx, webScrollY, 0, dy - webScrollY, mScrollOffset, ViewCompat.TYPE_TOUCH);
                if (Math.abs(mInitialTouchY - y) > mTouchSlop) {
                    //屏蔽WebView本身的滑动，滑动事件自己处理
                    event.setAction(MotionEvent.ACTION_CANCEL);
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                final float velocityY = -mVelocityTracker.getYVelocity();
                recycleVelocityTracker();
                flingScroll(0, Math.round(velocityY));
                resetTouch();
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                cancelTouch();
                break;
            }
        }
        super.onTouchEvent(event);
        return true;
    }

    private void resetTouch() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
        }
        stopNestedScroll(TYPE_TOUCH);
    }

    private void cancelTouch() {
        resetTouch();
    }

    private int onScrollUpMinScrollY() {
        return -getScrollY();
    }

    private int onScrollDownMaxScrollY() {
        return getWebContentHeight() - getMeasuredHeight() - getScrollY();
    }

    @Override
    public void flingScroll(int velocityX, int velocityY) {
        if (velocityY == 0) return;
        if (dispatchNestedPreFling(velocityX, velocityY)) {
            return;
        }
        dispatchNestedFling(velocityX, velocityY, canScrollVertically(velocityY));


        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH);

        velocityX = Math.max(-mMaximumFlingVelocity, Math.min(velocityX, mMaximumFlingVelocity));
        velocityY = Math.max(-mMaximumFlingVelocity, Math.min(velocityY, mMaximumFlingVelocity));
        mViewFlingHelper.fling(velocityX, velocityY);
    }

    public void stopFling() {
        mViewFlingHelper.stop();
    }

    private int getWebContentHeight() {
        return Math.round(getContentHeight() * mDensity);
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        recycleVelocityTracker();
        mViewFlingHelper.stop();
    }


    @Override
    public boolean canScrollVertically(int direction) {
        return super.canScrollVertically(direction);
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return mChildHelper.startNestedScroll(axes, type);
    }

    @Override
    public void stopNestedScroll(int type) {
        mChildHelper.stopNestedScroll(type);
    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return mChildHelper.hasNestedScrollingParent(type);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow, int type) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }

    private class ViewFlingHelper implements Runnable {

        private final Interpolator mInterpolator = new Interpolator() {
            @Override
            public float getInterpolation(float t) {
                t -= 1.0f;
                return t * t * t * t * t + 1.0f;
            }
        };
        private final OverScroller mScroller;

        private int mLastFlingX;
        private int mLastFlingY;

        ViewFlingHelper() {
            mScroller = new OverScroller(getContext(), mInterpolator);
        }

        void postOnAnimation() {
            removeCallbacks(this);
            ViewCompat.postOnAnimation(NestedScrollWebView1.this, this);
        }

        public void fling(int velocityX, int velocityY) {
            mLastFlingX = mLastFlingY = 0;
            mScroller.fling(0, 0, velocityX, velocityY,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            postOnAnimation();
        }

        void stop() {
            mScroller.abortAnimation();
        }

        @Override
        public void run() {

            // keep a local reference so that if it is changed during onAnimation method, it won't
            // cause unexpected behaviors
            final OverScroller scroller = mScroller;
            if (scroller.computeScrollOffset()) {
                final int[] scrollConsumed = mScrollConsumed;
                final int x = scroller.getCurrX();
                final int y = scroller.getCurrY();
                int dx = x - mLastFlingX;
                int dy = y - mLastFlingY;
                mLastFlingX = x;
                mLastFlingY = y;

                int webScrollY = 0;
                if (dispatchNestedPreScroll(dx, dy, scrollConsumed, null, TYPE_NON_TOUCH)) {
                    dx -= scrollConsumed[0];
                    dy -= scrollConsumed[1];
                    webScrollY = dy;
                    if (dy > 0) {//向上滑动
                        int curMaxScrollY = onScrollDownMaxScrollY();
                        webScrollY = Math.min(webScrollY, curMaxScrollY);
                    } else {//向下滑动
                        int curMinScrollY = onScrollUpMinScrollY();
                        webScrollY = Math.max(webScrollY, curMinScrollY);
                    }
                    scrollBy(0, webScrollY);
                    dy -= webScrollY;

                } else {
                    webScrollY = dy;
                    if (dy > 0) {//向上滑动
                        int curMaxScrollY = onScrollDownMaxScrollY();
                        webScrollY = Math.min(webScrollY, curMaxScrollY);
                    } else {//向下滑动
                        int curMinScrollY = onScrollUpMinScrollY();
                        webScrollY = Math.max(webScrollY, curMinScrollY);
                    }
                    scrollBy(0, webScrollY);
                }
                dispatchNestedScroll(dx, webScrollY, 0, dy - webScrollY, mScrollOffset, ViewCompat.TYPE_NON_TOUCH);

                if (scroller.isFinished()) {
                    // setting state to idle will stop this.
                    stopNestedScroll(TYPE_NON_TOUCH);
                } else {
                    postOnAnimation();
                }
            }
        }
    }
}