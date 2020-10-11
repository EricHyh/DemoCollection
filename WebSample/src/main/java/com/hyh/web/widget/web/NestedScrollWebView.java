package com.hyh.web.widget.web;

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
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.OverScroller;

import static android.support.v4.view.ViewCompat.TYPE_NON_TOUCH;
import static android.support.v4.view.ViewCompat.TYPE_TOUCH;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;

/**
 * @author Administrator
 * @description
 * @data 2019/6/10
 */

public class NestedScrollWebView extends CustomWebView implements NestedScrollingChild2 {

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

    static final Interpolator sQuinticInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    private final NestedScrollingChildHelper mChildHelper = new NestedScrollingChildHelper(this);
    private final ViewFlinger mViewFlinger = new ViewFlinger();

    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;


    private int mTouchSlop;
    private float mDensity;

    private int mScrollState = SCROLL_STATE_IDLE;
    private int mScrollPointerId;
    private int mInitialTouchX;
    private int mInitialTouchY;
    private int mLastTouchX;
    private int mLastTouchY;


    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private final int[] mNestedOffsets = new int[2];

    private boolean mOverScrolled;
    private boolean mOverTouchSlop;
    private int mTouchOrientation;

    private static final int ORIENTATION_NONE = 0;
    private static final int ORIENTATION_VERTICAL = 1;
    private static final int ORIENTATION_HORIZONTAL = 2;

    private VelocityTracker mVelocityTracker;
    private int mCurTouchAction;

    private static final int SCROLL_HORIZONTAL_NONE = 0;
    private static final int SCROLL_HORIZONTAL_ALLOW = 1;
    private static final int SCROLL_HORIZONTAL_TEMP_ALLOW = 2;
    //private static final int SCROLL_HORIZONTAL_TEMP_DISALLOW = 3;
    private static final int SCROLL_HORIZONTAL_DISALLOW = 4;
    private static final int SCROLL_HORIZONTAL_END = 5;

    private int mScrollHorizontalState;

    public NestedScrollWebView(Context context) {
        super(context.getApplicationContext());
        init();
    }

    public NestedScrollWebView(Context context, AttributeSet attrs) {
        super(context.getApplicationContext(), attrs);
        init();
    }

    public NestedScrollWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context.getApplicationContext(), attrs, defStyleAttr);
        init();
    }

    private void init() {
        setNestedScrollingEnabled(true);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());

        mMinFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = configuration.getScaledMaximumFlingVelocity();

        mTouchSlop = configuration.getScaledTouchSlop();
        mDensity = getContext().getResources().getDisplayMetrics().density;
    }


    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        mOverScrolled = true;

        if (mScrollHorizontalState == SCROLL_HORIZONTAL_TEMP_ALLOW) {
            mScrollHorizontalState = SCROLL_HORIZONTAL_DISALLOW;
        }

        Log.d(TAG, "LOOK- onOverScrolled: ");
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mCurTouchAction = ev.getActionMasked();
        if (mCurTouchAction == MotionEvent.ACTION_POINTER_DOWN) {
            Log.d(TAG, "dispatchTouchEvent: ");
        }
        /*if (mCurTouchAction == MotionEvent.ACTION_DOWN) {
            if (mScrollHorizontalState == SCROLL_HORIZONTAL_DISALLOW) {
                int x = (int) (ev.getX() + 0.5f);
                int y = (int) (ev.getY() + 0.5f);
                int dx = mLastTouchX - x;
                int dy = mLastTouchY - y;
                ev.offsetLocation(dx, dy);
            }
        }*/
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(e);

        final int action = e.getActionMasked();
        final int actionIndex = e.getActionIndex();


        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mScrollPointerId = e.getPointerId(0);
                if (mScrollHorizontalState != SCROLL_HORIZONTAL_DISALLOW) {
                    mInitialTouchX = mLastTouchX = (int) (e.getX() + 0.5f);
                    mInitialTouchY = mLastTouchY = (int) (e.getY() + 0.5f);
                }

                if (mScrollState == SCROLL_STATE_SETTLING) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    setScrollState(SCROLL_STATE_DRAGGING);
                }

                // Clear the nested offsets
                mNestedOffsets[0] = mNestedOffsets[1] = 0;

                int nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE;
                nestedScrollAxis |= ViewCompat.SCROLL_AXIS_VERTICAL;
                startNestedScroll(nestedScrollAxis, TYPE_TOUCH);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mScrollPointerId = e.getPointerId(actionIndex);
                mInitialTouchX = mLastTouchX = (int) (e.getX(actionIndex) + 0.5f);
                mInitialTouchY = mLastTouchY = (int) (e.getY(actionIndex) + 0.5f);
                break;

            case MotionEvent.ACTION_MOVE: {
                final int index = e.findPointerIndex(mScrollPointerId);
                if (index < 0) {
                    Log.e(TAG, "Error processing scroll; pointer index for id "
                            + mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                    return false;
                }

                final int x = (int) (e.getX(index) + 0.5f);
                final int y = (int) (e.getY(index) + 0.5f);
                if (mScrollState != SCROLL_STATE_DRAGGING) {
                    final int dx = x - mInitialTouchX;
                    final int dy = y - mInitialTouchY;
                    boolean startScroll = false;
                    if (abs(dy) > mTouchSlop) {
                        mLastTouchY = y;
                        startScroll = true;
                    }
                    if (startScroll) {
                        setScrollState(SCROLL_STATE_DRAGGING);
                    }
                }
            }
            break;

            case MotionEvent.ACTION_POINTER_UP: {
                onPointerUp(e);
            }
            break;

            case MotionEvent.ACTION_UP: {
                mVelocityTracker.clear();
                stopNestedScroll(TYPE_TOUCH);
            }
            break;

            case MotionEvent.ACTION_CANCEL: {
                cancelTouch();
            }
            break;
            default:
                throw new IllegalStateException("Unexpected value: " + action);
        }
        return mScrollState == SCROLL_STATE_DRAGGING;
        //return super.onInterceptTouchEvent(e);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        final int actionIndex = event.getActionIndex();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }

        if (action == MotionEvent.ACTION_DOWN) {
            mVelocityTracker.clear();
            mViewFlinger.stop();
        }

        boolean eventAddedToVelocityTracker = false;
        final MotionEvent vtev = MotionEvent.obtain(event);
        if (action == MotionEvent.ACTION_DOWN) {
            mNestedOffsets[0] = mNestedOffsets[1] = 0;
        }
        vtev.offsetLocation(mNestedOffsets[0], mNestedOffsets[1]);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mScrollPointerId = event.getPointerId(0);

                if (mScrollHorizontalState != SCROLL_HORIZONTAL_DISALLOW) {
                    mInitialTouchX = mLastTouchX = (int) (event.getX() + 0.5f);
                    mInitialTouchY = mLastTouchY = (int) (event.getY() + 0.5f);
                }

                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH);


                mOverTouchSlop = false;
                mOverScrolled = false;
                mTouchOrientation = ORIENTATION_NONE;

                if (mScrollHorizontalState != SCROLL_HORIZONTAL_DISALLOW) {
                    mScrollHorizontalState = SCROLL_HORIZONTAL_NONE;
                } else {
                    mScrollHorizontalState = SCROLL_HORIZONTAL_END;
                }

                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                mScrollPointerId = event.getPointerId(actionIndex);
                mInitialTouchX = mLastTouchX = (int) (event.getX(actionIndex) + 0.5f);
                mInitialTouchY = mLastTouchY = (int) (event.getY(actionIndex) + 0.5f);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int index = event.findPointerIndex(mScrollPointerId);
                if (index < 0) {
                    Log.e(TAG, "Error processing scroll; pointer index for id "
                            + mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                    return false;
                }

                if (mScrollHorizontalState == SCROLL_HORIZONTAL_DISALLOW) {


                    final int x = (int) (event.getX(index) + 0.5f);
                    final int y = (int) (event.getY(index) + 0.5f);


                    mLastTouchX = x - mScrollOffset[0];
                    mLastTouchY = y - mScrollOffset[1];


                    MotionEvent newEvent = MotionEvent.obtain(event);
                    newEvent.setAction(MotionEvent.ACTION_DOWN);

                    float[] newEventOffset = computeNewEventOffset();

                    newEvent.offsetLocation(newEventOffset[0], newEventOffset[1]);

                    cancelTouch();

                    getRootView().dispatchTouchEvent(newEvent);

                    return false;
                }

                final int x = (int) (event.getX(index) + 0.5f);
                final int y = (int) (event.getY(index) + 0.5f);


                Log.d(TAG, "onTouchEvent: mLastTouchX = " + mLastTouchX + ", mLastTouchY = " + mLastTouchY);

                int dx = mLastTouchX - x;
                int dy = mLastTouchY - y;

                Log.d(TAG, "onTouchEvent: x = " + x + ", y = " + y + ", dy = " + dy);

                int tx = x - mInitialTouchX;
                int ty = y - mInitialTouchY;

                if (handMotionEventByHorizontal(event, dx, dy, tx, ty)) return true;


                if (dispatchNestedPreScroll(dx, dy, mScrollConsumed, mScrollOffset, TYPE_TOUCH)) {
                    dx -= mScrollConsumed[0];
                    dy -= mScrollConsumed[1];
                    vtev.offsetLocation(mScrollOffset[0], mScrollOffset[1]);
                    // Updated the nested offsets
                    mNestedOffsets[0] += mScrollOffset[0];
                    mNestedOffsets[1] += mScrollOffset[1];
                }

                if (mScrollState != SCROLL_STATE_DRAGGING) {
                    if (abs(dy) > mTouchSlop) {
                        if (dy > 0) {
                            dy -= mTouchSlop;
                        } else {
                            dy += mTouchSlop;
                        }
                    }
                    setScrollState(SCROLL_STATE_DRAGGING);
                }


                if (mScrollState == SCROLL_STATE_DRAGGING) {
                    mLastTouchX = x - mScrollOffset[0];
                    mLastTouchY = y - mScrollOffset[1];
                    if (scrollByInternal(0, dy, vtev)) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                if (abs(mInitialTouchY - y) > mTouchSlop) {
                    //屏蔽WebView本身的滑动，滑动事件自己处理
                    event.setAction(MotionEvent.ACTION_CANCEL);
                }

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
                final float xvel = 0;
                final float yvel = -mVelocityTracker.getYVelocity(mScrollPointerId);
                if (!((yvel != 0) && fling((int) xvel, (int) yvel))) {
                    setScrollState(SCROLL_STATE_IDLE);
                }
                resetTouch();

                mScrollHorizontalState = SCROLL_HORIZONTAL_NONE;
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                cancelTouch();
                if (mScrollHorizontalState != SCROLL_HORIZONTAL_DISALLOW) {
                    mScrollHorizontalState = SCROLL_HORIZONTAL_NONE;
                }
                break;
            }
        }
        if (!eventAddedToVelocityTracker) {
            mVelocityTracker.addMovement(vtev);
        }
        vtev.recycle();
        if (action == MotionEvent.ACTION_DOWN && mScrollHorizontalState == SCROLL_HORIZONTAL_END) {
            return true;
        }
        super.onTouchEvent(event);
        return true;
    }

    private float[] computeNewEventOffset() {
        float[] offset = new float[2];
        View rootView = getRootView();
        /**
         *  final float offsetX = mScrollX - child.mLeft;
         *  final float offsetY = mScrollY - child.mTop;
         *  transformedEvent.offsetLocation(offsetX, offsetY);
         */
        float offsetX = 0.0f;
        float offsetY = 0.0f;

        View child = this;
        ViewParent parent = getParent();

        while (parent instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) parent;
            int parentScrollX = viewGroup.getScrollX();
            int parentScrollY = viewGroup.getScrollY();
            int childLeft = child.getLeft();
            int childTop = child.getTop();

            offsetX += parentScrollX - childLeft;
            offsetY += parentScrollY - childTop;

            if (viewGroup == rootView) break;

            child = viewGroup;
            parent = viewGroup.getParent();
        }

        offset[0] = -offsetX;
        offset[1] = -offsetY;

        Log.d(TAG, "computeNewEventOffset: offsetX = " + offsetX + ", offsetY = " + offsetY);

        return offset;
    }

    boolean scrollByInternal(int x, int y, MotionEvent ev) {
        int unconsumedX = 0, unconsumedY;
        int consumedX = 0, consumedY;


        int webScrollY = y;
        if (y > 0) {//向上滑动
            int curMaxScrollY = onScrollDownMaxScrollY();
            webScrollY = min(webScrollY, curMaxScrollY);
        } else {//向下滑动
            int curMinScrollY = onScrollUpMinScrollY();
            webScrollY = max(webScrollY, curMinScrollY);
        }
        scrollBy(0, webScrollY);
        consumedY = webScrollY;
        unconsumedY = y - webScrollY;


        if (dispatchNestedScroll(consumedX, consumedY, unconsumedX, unconsumedY, mScrollOffset, TYPE_TOUCH)) {
            // Update the last touch co-ords, taking any scroll offset into account
            mLastTouchX -= mScrollOffset[0];
            mLastTouchY -= mScrollOffset[1];
            if (ev != null) {
                ev.offsetLocation(mScrollOffset[0], mScrollOffset[1]);
            }
            mNestedOffsets[0] += mScrollOffset[0];
            mNestedOffsets[1] += mScrollOffset[1];
        }
        if (!awakenScrollBars()) {
            invalidate();
        }
        return consumedY != 0;
    }


    @Override
    public void scrollBy(int x, int y) {
        super.scrollBy(x, y);
    }

    private boolean fling(int velocityX, int velocityY) {
        velocityX = 0;
        if (abs(velocityY) < mMinFlingVelocity) {
            velocityY = 0;
        }
        if (velocityY == 0) {
            // If we don't have any velocity, return false
            return false;
        }
        if (!dispatchNestedPreFling(velocityX, velocityY)) {
            final boolean canScroll = true;
            dispatchNestedFling(velocityX, velocityY, canScroll);

            int nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE;
            nestedScrollAxis |= ViewCompat.SCROLL_AXIS_VERTICAL;
            startNestedScroll(nestedScrollAxis, TYPE_NON_TOUCH);

            velocityX = max(-mMaxFlingVelocity, min(velocityX, mMaxFlingVelocity));
            velocityY = max(-mMaxFlingVelocity, min(velocityY, mMaxFlingVelocity));
            mViewFlinger.fling(velocityX, velocityY);
            return true;
        }
        return false;
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

    private boolean handMotionEventByHorizontal(MotionEvent event, int dx, int dy, int tx, int ty) {
        if (mOverScrolled) {
            return false;
        }
        if (mTouchOrientation == ORIENTATION_HORIZONTAL) {
            super.onTouchEvent(event);
            /*if (mScrollHorizontalState == SCROLL_HORIZONTAL_TEMP_ALLOW
                    && abs(tx) > 4 * mTouchSlop) {
                mScrollHorizontalState = SCROLL_HORIZONTAL_ALLOW;
            }*/
            return true;
        } else {
            if (!mOverTouchSlop) {
                if (abs(tx) > mTouchSlop || abs(ty) > mTouchSlop) {
                    mOverTouchSlop = true;
                }
                if (mOverTouchSlop) {
                    mTouchOrientation = (abs(tx) > abs(ty)) ? ORIENTATION_HORIZONTAL : ORIENTATION_VERTICAL;
                }
                if (mTouchOrientation == ORIENTATION_HORIZONTAL) {
                    super.onTouchEvent(event);
                    return true;
                }
            }
        }
        return false;
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

    @Override
    public boolean canScrollHorizontally(int direction) {
        if (mScrollHorizontalState == SCROLL_HORIZONTAL_DISALLOW) {
            Log.d(TAG, "LOOK-  canScrollHorizontally: SCROLL_HORIZONTAL_DISALLOW");
            return false;
        }
        if (mScrollHorizontalState == SCROLL_HORIZONTAL_ALLOW) {
            Log.d(TAG, "LOOK-  canScrollHorizontally: SCROLL_HORIZONTAL_ALLOW");
            return true;
        }

        boolean scrollHorizontally = super.canScrollHorizontally(direction);
        if (scrollHorizontally) return true;

        Log.d(TAG, "LOOK- canScrollHorizontally:"
                + " | mCurTouchAction = " + mCurTouchAction
                + " | mScrollHorizontalState = " + mScrollHorizontalState);

        if (mCurTouchAction == MotionEvent.ACTION_DOWN || mCurTouchAction == MotionEvent.ACTION_MOVE) {
            if (mScrollHorizontalState == SCROLL_HORIZONTAL_NONE) {
                boolean tempCanScrollHorizontally = isTempCanScrollHorizontally();
                mScrollHorizontalState = tempCanScrollHorizontally ?
                        SCROLL_HORIZONTAL_TEMP_ALLOW : SCROLL_HORIZONTAL_DISALLOW;
                Log.d(TAG, "LOOK-  canScrollHorizontally: tempCanScrollHorizontally = " + tempCanScrollHorizontally);
            }

            boolean scroll_horizontal_temp_allow = mScrollHorizontalState == SCROLL_HORIZONTAL_TEMP_ALLOW;
            Log.d(TAG, "LOOK-  canScrollHorizontally: scroll_horizontal_temp_allow = " + scroll_horizontal_temp_allow);
            return scroll_horizontal_temp_allow;
        }

        return false;
    }

    private boolean isTempCanScrollHorizontally() {
        return (mTouchOrientation == ORIENTATION_NONE)
                || (mTouchOrientation == ORIENTATION_HORIZONTAL && !mOverScrolled);
    }

    @Override
    public void flingScroll(int velocityX, int velocityY) {
        if (velocityY == 0) return;
        velocityX = 0;
        if (abs(velocityY) < mMinFlingVelocity) {
            velocityY = 0;
        }
        if (velocityY == 0) {
            // If we don't have any velocity, return false
            return;
        }
        if (!dispatchNestedPreFling(velocityX, velocityY)) {
            final boolean canScroll = true;
            dispatchNestedFling(velocityX, velocityY, canScroll);

            int nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE;
            nestedScrollAxis |= ViewCompat.SCROLL_AXIS_VERTICAL;
            startNestedScroll(nestedScrollAxis, TYPE_NON_TOUCH);

            velocityX = max(-mMaxFlingVelocity, min(velocityX, mMaxFlingVelocity));
            velocityY = max(-mMaxFlingVelocity, min(velocityY, mMaxFlingVelocity));
            mViewFlinger.fling(velocityX, velocityY);
        }
    }

    public void stopFling() {
        mViewFlinger.stop();
    }

    protected int getWebContentHeight() {
        return round(getContentHeight() * mDensity);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mViewFlinger.stop();
    }

    void setScrollState(int state) {
        if (state == mScrollState) {
            return;
        }
        mScrollState = state;
        if (state != SCROLL_STATE_SETTLING) {
            stopScrollersInternal();
        }
    }

    private void stopScrollersInternal() {
        mViewFlinger.stop();
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

    private class ViewFlinger implements Runnable {

        private int mLastFlingX;
        private int mLastFlingY;
        private OverScroller mScroller;
        Interpolator mInterpolator = sQuinticInterpolator;


        // When set to true, postOnAnimation callbacks are delayed until the run method completes
        private boolean mEatRunOnAnimationRequest = false;

        // Tracks if postAnimationCallback should be re-attached when it is done
        private boolean mReSchedulePostAnimationCallback = false;

        ViewFlinger() {
            mScroller = new OverScroller(getContext(), mInterpolator);
        }

        @Override
        public void run() {
            disableRunOnAnimationRequests();
            // keep a local reference so that if it is changed during onAnimation method, it won't
            // cause unexpected behaviors
            final OverScroller scroller = mScroller;

            if (scroller.computeScrollOffset()) {
                final int[] scrollConsumed = mScrollConsumed;
                final int x = scroller.getCurrX();
                final int y = scroller.getCurrY();
                int dx = x - mLastFlingX;
                int dy = y - mLastFlingY;
                int hresult = 0;
                int vresult = 0;
                mLastFlingX = x;
                mLastFlingY = y;
                int overscrollX = 0, overscrollY = 0;

                if (dispatchNestedPreScroll(dx, dy, scrollConsumed, null, TYPE_NON_TOUCH)) {
                    dx -= scrollConsumed[0];
                    dy -= scrollConsumed[1];
                }

                int webScrollY = dy;
                if (dy > 0) {//向上滑动
                    int curMaxScrollY = onScrollDownMaxScrollY();
                    webScrollY = min(webScrollY, curMaxScrollY);
                } else {//向下滑动
                    int curMinScrollY = onScrollUpMinScrollY();
                    webScrollY = max(webScrollY, curMinScrollY);
                }
                scrollBy(0, webScrollY);
                vresult = webScrollY;
                overscrollY = dy - webScrollY;


                if (!dispatchNestedScroll(hresult, vresult, overscrollX, overscrollY, null,
                        TYPE_NON_TOUCH) && overscrollY != 0) {
                    final int vel = (int) scroller.getCurrVelocity();
                    int velX = 0;
                    int velY = 0;
                    if (overscrollY != y) {
                        velY = overscrollY < 0 ? -vel : vel;
                    }
                    if ((overscrollX == x || scroller.getFinalX() == 0) && (velY != 0 || overscrollY == y || scroller.getFinalY() == 0)) {
                        scroller.abortAnimation();
                    }
                }

                if (!awakenScrollBars()) {
                    invalidate();
                }

                final boolean fullyConsumedVertical = dy != 0 && vresult == dy;
                final boolean fullyConsumedAny = (dx == 0 && dy == 0) || fullyConsumedVertical;

                if (scroller.isFinished() || (!fullyConsumedAny
                        && !hasNestedScrollingParent(TYPE_NON_TOUCH))) {
                    // setting state to idle will stop this.
                    setScrollState(SCROLL_STATE_IDLE);
                    stopNestedScroll(TYPE_NON_TOUCH);
                } else {
                    postOnAnimation();
                }
            }
            // call this after the onAnimation is complete not to have inconsistent callbacks etc.
            enableRunOnAnimationRequests();
        }

        private void disableRunOnAnimationRequests() {
            mReSchedulePostAnimationCallback = false;
            mEatRunOnAnimationRequest = true;
        }

        private void enableRunOnAnimationRequests() {
            mEatRunOnAnimationRequest = false;
            if (mReSchedulePostAnimationCallback) {
                postOnAnimation();
            }
        }

        void postOnAnimation() {
            if (mEatRunOnAnimationRequest) {
                mReSchedulePostAnimationCallback = true;
            } else {
                removeCallbacks(this);
                ViewCompat.postOnAnimation(NestedScrollWebView.this, this);
            }
        }

        public void fling(int velocityX, int velocityY) {
            setScrollState(SCROLL_STATE_SETTLING);
            mLastFlingX = mLastFlingY = 0;
            mScroller.fling(0, 0, velocityX, velocityY,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            postOnAnimation();
        }


        public void stop() {
            removeCallbacks(this);
            mScroller.abortAnimation();
        }
    }
}