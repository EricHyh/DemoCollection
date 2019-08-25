package com.hyh.web.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.hyh.web.behavior.NestedScrollWebView;
import com.hyh.web.utils.DisplayUtil;

/**
 * Created by Eric_He on 2019/8/25.
 */

public class ViewMoreWebView extends NestedScrollWebView {

    private static final String TAG = "ViewMoreWebView";

    private RectF mViewMoreRectF = new RectF();
    private Paint mPaint;

    private int mInitialTouchX;
    private int mInitialTouchY;

    private boolean mIsViewMoreAreaEnabled = true;
    private boolean mIsHideViewMoreArea;
    private boolean mIsDrawViewMoreArea;


    private int mViewMoreAreaHeight;

    private int mTouchSlop;
    private float mDensity;
    private boolean mShouldHideViewMoreAreaOnActionUp;

    public ViewMoreWebView(Context context) {
        super(context);
        init();

    }

    public ViewMoreWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOverScrollMode(OVER_SCROLL_NEVER);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mDensity = getContext().getResources().getDisplayMetrics().density;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mIsViewMoreAreaEnabled && !mIsHideViewMoreArea) {
            int height = getMeasuredHeight();
            int contentHeight = Math.round(getContentHeight() * mDensity);
            if (contentHeight >= 1.8f * height) {
                canvas.drawRect(mViewMoreRectF, mPaint);
                mIsDrawViewMoreArea = true;
            } else {
                mIsDrawViewMoreArea = false;
            }
        } else {
            mIsDrawViewMoreArea = false;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        mViewMoreAreaHeight = DisplayUtil.dip2px(getContext(), 60);
        mViewMoreRectF.set(0, height, width, height + mViewMoreAreaHeight);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mIsHideViewMoreArea) {
            return super.dispatchTouchEvent(event);
        }
        int action = event.getActionMasked();
        if (action != MotionEvent.ACTION_DOWN && !mShouldHideViewMoreAreaOnActionUp) {
            return super.dispatchTouchEvent(event);
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mShouldHideViewMoreAreaOnActionUp = false;
                mInitialTouchX = Math.round(event.getX());
                mInitialTouchY = Math.round(event.getY());
                if (mViewMoreRectF.contains(mInitialTouchX, mInitialTouchY + getScrollY())) {
                    mShouldHideViewMoreAreaOnActionUp = true;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int x = Math.round(event.getX());
                int y = Math.round(event.getY());
                int tx = mInitialTouchX - x;
                int ty = mInitialTouchY - y;
                if (Math.abs(tx) > mTouchSlop || Math.abs(ty) > mTouchSlop) {
                    mShouldHideViewMoreAreaOnActionUp = false;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                hideViewMoreArea();
                event.setAction(MotionEvent.ACTION_CANCEL);
                mShouldHideViewMoreAreaOnActionUp = false;
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                mShouldHideViewMoreAreaOnActionUp = false;
                break;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void hideViewMoreArea() {
        mIsHideViewMoreArea = true;
        postInvalidate();
        ViewGroup parent = (ViewGroup) getParent();
        int parentScrollY = parent.getScrollY();
        if (parentScrollY > 0) {
            int[] location = new int[2];
            getLocationInWindow(location);
            int[] parentLocation = new int[2];
            parent.getLocationInWindow(parentLocation);
            int offsetRelativeToParent = location[1] - parentLocation[1];

            parent.scrollBy(0, offsetRelativeToParent);
            int currentMaxScrollUpDy = computeCurrentMaxScrollUpDy();
            scrollBy(0, Math.min(currentMaxScrollUpDy, -offsetRelativeToParent));
        }
    }

    private int computeCurrentMaxScrollUpDy() {
        int contentHeight = Math.round(getContentHeight() * mDensity);
        return Math.max(contentHeight - getMeasuredHeight(), 0);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if (direction > 0 && !mIsHideViewMoreArea && mIsDrawViewMoreArea && super.canScrollVertically(direction)) {
            final int offset = computeVerticalScrollOffset();
            final int range = mViewMoreAreaHeight;
            if (range == 0) return false;
            if (direction < 0) {
                return offset > 0;
            } else {
                return offset < range - 1;
            }
        }
        return super.canScrollVertically(direction);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mIsViewMoreAreaEnabled && mIsDrawViewMoreArea && !mIsHideViewMoreArea) {
            int scrollY = getScrollY();
            if (scrollY > mViewMoreAreaHeight) {
                scrollY = mViewMoreAreaHeight;
                scrollTo(0, scrollY);
            }
        }
    }
}