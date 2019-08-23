package com.hyh.web.window;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * @author Administrator
 * @description 飘窗的根View，用于接收和分发触摸事件
 * @data 2017/7/25
 */
public class FloatingView extends FrameLayout implements ReleaseListener {

    private WindowEventListener mWindowEventListener;
    private SlideController mSlideController;
    private final int mTouchSlop;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private float mLastTouchX;
    private float mLastTouchY;
    private boolean tryInterceptTouchEvent;
    private FloatingWindow mFloatingWindow;
    private boolean mIsDragView;
    private int[] mInitialPosition = new int[2];
    private int[] mTouchPosition = new int[2];
    private boolean mIsInReleasing;

    public FloatingView(Context context) {
        super(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void bindFloatWindow(FloatingWindow floatingWindow, WindowEventListener listener, SlideController slideController) {
        this.mFloatingWindow = floatingWindow;
        this.mWindowEventListener = listener;
        this.mSlideController = slideController;
        WindowManager.LayoutParams params = mFloatingWindow.getParams();
        mInitialPosition[0] = params.x;
        mInitialPosition[1] = params.y;
        if (mSlideController != null) {
            mSlideController.bindFloatWindow(floatingWindow);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mWindowEventListener != null) {
            if (mWindowEventListener.onKeyEvent(event)) {
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (mWindowEventListener != null) {
            mWindowEventListener.onWindowFocusChanged(hasWindowFocus);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mWindowEventListener != null) {
            mWindowEventListener.onAttachedToWindow();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mWindowEventListener != null) {
            mWindowEventListener.onDetachedFromWindow();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mIsInReleasing) {
            return false;
        }
        if (mSlideController == null) {
            return super.dispatchTouchEvent(ev);
        }
        int action = ev.getActionMasked();
        if (tryInterceptTouchEvent) {
            if (action == MotionEvent.ACTION_UP) {
                tryInterceptTouchEvent = false;
            }
            return super.dispatchTouchEvent(ev);
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mInitialTouchX = mLastTouchX = ev.getRawX();
                mInitialTouchY = mLastTouchY = ev.getRawY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float rawX = ev.getRawX();
                float rawY = ev.getRawY();

                float tx = rawX - mInitialTouchX;
                float ty = rawY - mInitialTouchY;

                if (Math.abs(tx) > mTouchSlop || (Math.abs(ty) > mTouchSlop)) {
                    tryInterceptTouchEvent = true;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                tryInterceptTouchEvent = false;
                break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mSlideController == null) {
            return super.onInterceptTouchEvent(ev);
        }
        if (mIsDragView) {
            return true;
        }
        mIsDragView = false;
        if (tryInterceptTouchEvent) {
            mTouchPosition[0] = (int) (ev.getRawX() + 0.5f);
            mTouchPosition[1] = (int) (ev.getRawY() + 0.5f);
            mIsDragView = mSlideController.tryDrag(mInitialPosition.clone(), mTouchPosition.clone());
        }
        return mIsDragView || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mSlideController == null) {
            return super.onTouchEvent(event);
        }
        if (mIsDragView) {
            mTouchPosition[0] = (int) (event.getRawX() + 0.5f);
            mTouchPosition[1] = (int) (event.getRawY() + 0.5f);
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_MOVE) {
                mSlideController.onDrag(mTouchPosition);
            } else if (action == MotionEvent.ACTION_UP) {
                mIsInReleasing = true;
                mSlideController.onReleased(mTouchPosition, this);
            }
        }
        return super.onTouchEvent(event);
    }


    @Override
    public void onReleaseFinish(int[] lastPosition) {
        mInitialPosition = lastPosition.clone();
        mIsInReleasing = false;
        mIsDragView = false;
    }


    interface WindowEventListener {

        boolean onKeyEvent(KeyEvent event);

        void onAttachedToWindow();

        void onDetachedFromWindow();

        void onWindowFocusChanged(boolean hasWindowFocus);

    }
}
