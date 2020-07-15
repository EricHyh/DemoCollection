package com.hyh.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

/**
 * @author Administrator
 * @description
 * @data 2019/8/30
 */

public class SlideToCloseLayout extends FrameLayout {

    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;
    private static final int MIN_FLING_VELOCITY = 400; // dips per second

    private final DragHelperCallback mDragCallback = new DragHelperCallback();
    private final ViewDragHelper mDragHelper;
    private final Drawable mShadow = newDefaultShadow();
    private final int mShadowWidth;
    private final int mTouchSlop;
    private final int mSlideCloseArea;
    private final int mScrimColor = DEFAULT_SCRIM_COLOR;
    private Paint mScrimPaint = new Paint();

    private OnSlideToCloseListener mSlideToCloseListener;

    private boolean mIsTouchDownCloseArea;
    private boolean mIsOverTouchSlop;
    private boolean mShouldInterceptTouchEvent;

    private int mInitialTouchX;
    private int mInitialTouchY;


    public SlideToCloseLayout(@NonNull Context context) {
        this(context, null);
    }

    public SlideToCloseLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mDragHelper = ViewDragHelper.create(this, 0.5F, this.mDragCallback);
        final float density = getResources().getDisplayMetrics().density;
        final float minVel = MIN_FLING_VELOCITY * density;
        mDragHelper.setMinVelocity(minVel);
        this.mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        this.mSlideCloseArea = mTouchSlop * 4;
        final float scale = getContext().getResources().getDisplayMetrics().density;
        this.mShadowWidth = (int) (16 * scale + 0.5f);
    }

    private Drawable newDefaultShadow() {
        GradientDrawable shadow = new GradientDrawable();
        shadow.setGradientCenter(0.7f, 0.5f);
        shadow.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        shadow.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        shadow.setShape(GradientDrawable.RECTANGLE);
        int[] colors = {0x00000000, 0x30000000, 0x50000000};
        shadow.setColors(colors);
        return shadow;
    }

    public void setSlideToCloseListener(OnSlideToCloseListener slideToCloseListener) {
        mSlideToCloseListener = slideToCloseListener;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        int childCount = getChildCount();
        if (childCount > 0 && mDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE) {
            View child = getChildAt(0);
            drawScrim(canvas);
            drawShadow(canvas, child);
        }
    }


    private void drawScrim(Canvas canvas) {
        float left = 0;
        float top = 0;
        float right = mDragCallback.mCurrentLeft;
        float bottom = getMeasuredHeight();

        final int showing = getMeasuredWidth() - mDragCallback.mCurrentLeft;
        float scrimOpacity = Math.max(0, Math.min((float) showing / getMeasuredWidth(), 1.f));
        if (scrimOpacity > 0) {
            final int baseAlpha = (mScrimColor & 0xFF000000) >>> 24;
            final int image = (int) (baseAlpha * scrimOpacity);
            final int color = image << 24;
            mScrimPaint.setColor(color);
            canvas.drawRect(left, top, right, bottom, mScrimPaint);
        }
    }

    private void drawShadow(Canvas canvas, View child) {
        if (mShadow != null) {
            final int shadowWidth = mShadowWidth;
            final int childLeft = mDragCallback.mCurrentLeft;
            final int showing = getMeasuredWidth() - childLeft;
            final float alpha =
                    Math.max(0, Math.min((float) showing / getMeasuredWidth(), 1.f));
            mShadow.setBounds(childLeft - shadowWidth, child.getTop(), childLeft, child.getBottom());
            mShadow.setAlpha((int) (0xff * alpha));
            mShadow.draw(canvas);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            mIsTouchDownCloseArea = false;
            mIsOverTouchSlop = false;
            mShouldInterceptTouchEvent = false;
        }
        if (mDragHelper.getViewDragState() == ViewDragHelper.STATE_SETTLING) {
            getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        }
        if (mSlideCloseArea <= 0) {
            return super.dispatchTouchEvent(event);
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mInitialTouchX = Math.round(event.getX());
                mInitialTouchY = Math.round(event.getY());

                mIsTouchDownCloseArea = mInitialTouchX <= mSlideCloseArea;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mIsTouchDownCloseArea) {
                    int x = Math.round(event.getX());
                    int y = Math.round(event.getY());
                    int tx = x - mInitialTouchX;
                    int ty = y - mInitialTouchY;

                    if (!mIsOverTouchSlop) {
                        if (Math.abs(tx) > mTouchSlop || Math.abs(ty) > mTouchSlop) {
                            mIsOverTouchSlop = true;
                        }
                        if (mIsOverTouchSlop && Math.abs(tx) > Math.abs(ty)) {
                            mShouldInterceptTouchEvent = true;
                        }
                    }
                }
                break;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mIsTouchDownCloseArea) {
            mDragHelper.shouldInterceptTouchEvent(ev);

            if (!mShouldInterceptTouchEvent) {
                mDragHelper.processTouchEvent(ev);
            }
        }
        return mShouldInterceptTouchEvent || super.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsTouchDownCloseArea) {
            mDragHelper.processTouchEvent(event);
        }
        return super.onTouchEvent(event) || getChildCount() > 1;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childCount = getChildCount();
        if (childCount > 0) {
            View child = getChildAt(0);
            if (mDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE) {
                child.layout(mDragCallback.mCurrentLeft, 0, mDragCallback.mCurrentLeft + getWidth(), getHeight());
            } else {
                super.onLayout(changed, left, top, right, bottom);
            }
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private class DragHelperCallback extends ViewDragHelper.Callback {

        private int mCurrentLeft;

        private boolean mShouldClose;


        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            return true;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
            if (state == ViewDragHelper.STATE_IDLE) {
                mCurrentLeft = 0;
                if (mShouldClose && mSlideToCloseListener != null) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            mSlideToCloseListener.onClosed();
                        }
                    });
                }
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            mCurrentLeft = left;
            invalidate();
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (xvel > 1500 || releasedChild.getLeft() > getMeasuredWidth() / 2) {
                mDragHelper.smoothSlideViewTo(releasedChild, getMeasuredWidth(), releasedChild.getTop());
                mShouldClose = true;
            } else {
                mDragHelper.smoothSlideViewTo(releasedChild, 0, releasedChild.getTop());
            }
            ViewCompat.postInvalidateOnAnimation(SlideToCloseLayout.this);
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            //left = (int) (left - dx + dx * 0.75);
            if (left < 0) {
                left = 0;
            }
            return left;
        }
    }

    public interface OnSlideToCloseListener {

        void onClosed();

    }
}