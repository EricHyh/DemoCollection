package com.hyh.web.behavior;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingChild2;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.webkit.WebView;
import android.widget.Scroller;

import static android.support.v4.view.ViewCompat.TYPE_NON_TOUCH;
import static android.support.v4.view.ViewCompat.TYPE_TOUCH;

/**
 * @author Administrator
 * @description
 * @data 2019/6/10
 */

public class NestedScrollWebViewFailure1 extends WebView implements NestedScrollingChild2 {

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
    private final int mMinFlingVelocity;
    private final int mMaxFlingVelocity;
    private final int mTouchSlop;
    private final float mDensity;

    private int mScrollPointerId;
    private int mInitialTouchX;
    private int mInitialTouchY;
    private int mLastTouchX;
    private int mLastTouchY;
    private VelocityTracker mVelocityTracker;

    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private final int[] mNestedOffsets = new int[2];

    private int mScrollState = SCROLL_STATE_IDLE;

    public NestedScrollWebViewFailure1(Context context) {
        this(context, null);
    }

    public NestedScrollWebViewFailure1(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedScrollWebViewFailure1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setNestedScrollingEnabled(true);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mMinFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        mTouchSlop = configuration.getScaledTouchSlop();
        mDensity = context.getResources().getDisplayMetrics().density;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        if (action == MotionEvent.ACTION_DOWN) {
            stopFling();
        }
        mVelocityTracker.addMovement(event);

        final int actionIndex = event.getActionIndex();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mScrollPointerId = event.getPointerId(0);
                mInitialTouchX = mLastTouchX = Math.round(event.getX());
                mInitialTouchY = mLastTouchY = Math.round(event.getY());

                if (mScrollState == SCROLL_STATE_SETTLING) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    setScrollState(SCROLL_STATE_DRAGGING);
                }

                // Clear the nested offsets
                mNestedOffsets[0] = mNestedOffsets[1] = 0;
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, TYPE_TOUCH);
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                mScrollPointerId = event.getPointerId(actionIndex);
                mInitialTouchX = mLastTouchX = Math.round(event.getX(actionIndex));
                mInitialTouchY = mLastTouchY = Math.round(event.getY(actionIndex));
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int index = event.findPointerIndex(mScrollPointerId);
                if (index < 0) {
                    Log.e(TAG, "Error processing scroll; pointer index for id "
                            + mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                    return false;
                }

                final int x = (int) (event.getX(index) + 0.5f);
                final int y = (int) (event.getY(index) + 0.5f);
                if (mScrollState != SCROLL_STATE_DRAGGING) {
                    final int dx = x - mInitialTouchX;
                    final int dy = y - mInitialTouchY;
                    boolean startScroll = false;
                    if (Math.abs(dy) > mTouchSlop) {
                        mLastTouchY = y;
                        startScroll = true;
                    }
                    if (startScroll) {
                        setScrollState(SCROLL_STATE_DRAGGING);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                onPointerUp(event);
                break;
            }
            case MotionEvent.ACTION_UP: {
                mVelocityTracker.clear();
                stopNestedScroll(TYPE_TOUCH);
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                cancelTouch();
                break;
            }
        }
        return mScrollState == SCROLL_STATE_DRAGGING;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        boolean eventAddedToVelocityTracker = false;

        final MotionEvent vtev = MotionEvent.obtain(event);
        int action = event.getActionMasked();
        final int actionIndex = event.getActionIndex();

        if (action == MotionEvent.ACTION_DOWN) {
            mNestedOffsets[0] = mNestedOffsets[1] = 0;
            stopFling();
        }
        vtev.offsetLocation(mNestedOffsets[0], mNestedOffsets[1]);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mScrollPointerId = event.getPointerId(0);
                mInitialTouchX = mLastTouchX = Math.round(event.getX());
                mInitialTouchY = mLastTouchY = Math.round(event.getY());
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH);
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                mScrollPointerId = event.getPointerId(actionIndex);
                mInitialTouchX = mLastTouchX = Math.round(event.getX(actionIndex));
                mInitialTouchY = mLastTouchY = Math.round(event.getY(actionIndex));
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int index = event.findPointerIndex(mScrollPointerId);
                if (index < 0) {
                    Log.e(TAG, "Error processing scroll; pointer index for id "
                            + mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                    return false;
                }

                int x = Math.round(event.getX(index));
                int y = Math.round(event.getY(index));
                int dx = mLastTouchX - x;
                int dy = mLastTouchY - y;
                mLastTouchX = x;
                mLastTouchY = y;

                if (dispatchNestedPreScroll(dx, dy, mScrollConsumed, mScrollOffset, TYPE_TOUCH)) {
                    dx -= mScrollConsumed[0];
                    dy -= mScrollConsumed[1];
                    vtev.offsetLocation(mScrollOffset[0], mScrollOffset[1]);
                    // Updated the nested offsets
                    mNestedOffsets[0] += mScrollOffset[0];
                    mNestedOffsets[1] += mScrollOffset[1];
                }

                if (mScrollState != SCROLL_STATE_DRAGGING) {
                    boolean startScroll = false;
                    if (Math.abs(dy) > mTouchSlop) {
                        if (dy > 0) {
                            dy -= mTouchSlop;
                        } else {
                            dy += mTouchSlop;
                        }
                        startScroll = true;
                    }
                    if (startScroll) {
                        setScrollState(SCROLL_STATE_DRAGGING);
                    }
                }
                if (mScrollState == SCROLL_STATE_DRAGGING) {
                    mLastTouchX = x - mScrollOffset[0];
                    mLastTouchY = y - mScrollOffset[1];
                    if (scrollByInternal(dy, vtev)) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                if (Math.abs(mInitialTouchY - y) > mTouchSlop) {
                    //屏蔽WebView本身的滑动，滑动事件自己处理
                    event.setAction(MotionEvent.ACTION_CANCEL);
                }
                /*int webScrollY = 0;
                if (dispatchNestedPreScroll(0, dy, mScrollConsumed, mScrollOffset, ViewCompat.TYPE_TOUCH)) {
                    dx -= mScrollConsumed[0];
                    dy -= mScrollConsumed[1];
                    vtev.offsetLocation(mScrollOffset[0], mScrollOffset[1]);
                    // Updated the nested offsets
                    mNestedOffsets[0] += mScrollOffset[0];
                    mNestedOffsets[1] += mScrollOffset[1];



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
                }*/
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                onPointerUp(event);
                break;
            }
            case MotionEvent.ACTION_UP: {
                mVelocityTracker.addMovement(vtev);
                eventAddedToVelocityTracker = true;
                mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
                final float velocityY = -mVelocityTracker.getYVelocity();
                recycleVelocityTracker();
                if (velocityY == 0 || !flingY(Math.round(velocityY))) {
                    setScrollState(SCROLL_STATE_IDLE);
                }
                resetTouch();
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                cancelTouch();
                break;
            }
        }
        if (!eventAddedToVelocityTracker) {
            mVelocityTracker.addMovement(vtev);
        }
        vtev.recycle();
        super.onTouchEvent(event);
        return true;
    }

    private boolean scrollByInternal(int dy, MotionEvent vtev) {
        int webScrollY;
        if (dy > 0) {//向上滑动
            int curMaxScrollY = onScrollDownMaxScrollY();
            webScrollY = Math.min(dy, curMaxScrollY);
        } else {//向下滑动
            int curMinScrollY = onScrollUpMinScrollY();
            webScrollY = Math.max(dy, curMinScrollY);
        }
        scrollBy(0, webScrollY);
        dy -= webScrollY;

        if (dispatchNestedScroll(0, webScrollY, 0, dy, mScrollOffset, TYPE_TOUCH)) {
            mLastTouchX -= mScrollOffset[0];
            mLastTouchY -= mScrollOffset[1];
            if (vtev != null) {
                vtev.offsetLocation(mScrollOffset[0], mScrollOffset[1]);
            }
            mNestedOffsets[0] += mScrollOffset[0];
            mNestedOffsets[1] += mScrollOffset[1];
        }
        return webScrollY != 0;
    }

    private boolean flingY(int velocityY) {
        if (Math.abs(velocityY) < mMinFlingVelocity) {
            velocityY = 0;
        }
        if (velocityY == 0) {
            // If we don't have any velocity, return false
            return false;
        }
        if (!dispatchNestedPreFling(0, velocityY)) {
            final boolean canScroll = canScrollVertically(velocityY);
            dispatchNestedFling(0, velocityY, canScroll);
            if (canScroll) {
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, TYPE_NON_TOUCH);
                velocityY = Math.max(-mMaxFlingVelocity, Math.min(velocityY, mMaxFlingVelocity));
                mViewFlingHelper.fling(0, velocityY);
                return true;
            }
            return false;
        }
        return false;
    }

    public void stopFling() {
        mViewFlingHelper.stop();
    }

    private void onPointerUp(MotionEvent e) {
        final int actionIndex = e.getActionIndex();
        if (e.getPointerId(actionIndex) == mScrollPointerId) {
            // Pick a new pointer to pick up the slack.
            final int newIndex = actionIndex == 0 ? 1 : 0;
            mScrollPointerId = e.getPointerId(newIndex);
            mInitialTouchX = mLastTouchX = (int) (e.getX(newIndex) + 0.5f);
            mInitialTouchY = mLastTouchY = (int) (e.getY(newIndex) + 0.5f);
        }
    }

    private void resetTouch() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
        }
        stopNestedScroll(TYPE_TOUCH);
    }

    private void cancelTouch() {
        resetTouch();
        setScrollState(SCROLL_STATE_IDLE);
    }

    private int onScrollUpMinScrollY() {
        return -getScrollY();
    }

    private int onScrollDownMaxScrollY() {
        return getWebContentHeight() - getMeasuredHeight() - getScrollY();
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

    public void setScrollState(int scrollState) {
        this.mScrollState = scrollState;
    }

    private class ViewFlingHelper implements Runnable {

        private final Interpolator mInterpolator = new Interpolator() {
            @Override
            public float getInterpolation(float t) {
                t -= 1.0f;
                return t * t * t * t * t + 1.0f;
            }
        };
        private final Scroller mScroller;

        private int mLastFlingX;
        private int mLastFlingY;

        ViewFlingHelper() {
            mScroller = new Scroller(getContext(), mInterpolator);
        }

        void postOnAnimation() {
            removeCallbacks(this);
            ViewCompat.postOnAnimation(NestedScrollWebViewFailure1.this, this);
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
            final Scroller scroller = mScroller;
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