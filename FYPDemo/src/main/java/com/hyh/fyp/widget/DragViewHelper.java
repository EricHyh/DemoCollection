package com.hyh.fyp.widget;

import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;

import com.hyh.common.utils.ViewUtil;

/**
 * @author Administrator
 * @description
 * @data 2020/7/21
 */
public class DragViewHelper implements View.OnTouchListener, View.OnAttachStateChangeListener, ViewTreeObserver.OnGlobalLayoutListener {

    private static final String TAG = "DragViewHelper";

    private final View view;
    private final int mTouchSlop;

    private float mInitialTouchX;
    private float mInitialTouchY;

    private float mLastTranslationX;
    private float mLastTranslationY;

    private boolean mInDragging;

    private RectF mViewRectF;
    private RectF mParentRectF;

    private float mMinTranslationX;
    private float mMaxTranslationX;
    private float mMinTranslationY;
    private float mMaxTranslationY;


    public DragViewHelper(View view) {
        this.view = view;
        this.mTouchSlop = ViewConfiguration.get(view.getContext()).getScaledTouchSlop();

        mLastTranslationX = view.getTranslationX();
        mLastTranslationY = view.getTranslationY();
        view.setOnTouchListener(this);
        view.addOnAttachStateChangeListener(this);
        view.getViewTreeObserver().addOnGlobalLayoutListener(this);
        if (ViewUtil.isAttachedToWindow(view)) {
            onViewAttachedToWindow(view);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getActionMasked();

        boolean result = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mInitialTouchX = event.getRawX();
                mInitialTouchY = event.getRawY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float x = event.getRawX();
                float y = event.getRawY();

                float tx = x - mInitialTouchX;
                float ty = y - mInitialTouchY;

                if (Math.abs(tx) > mTouchSlop || Math.abs(ty) > mTouchSlop) {
                    mInDragging = true;
                }

                if (mInDragging) {
                    float translationX = mLastTranslationX + tx;
                    float translationY = mLastTranslationY + ty;

                    translationX = Math.max(mMinTranslationX, translationX);
                    translationX = Math.min(mMaxTranslationX, translationX);

                    translationY = Math.max(mMinTranslationY, translationY);
                    translationY = Math.min(mMaxTranslationY, translationY);

                    view.setTranslationX(translationX);
                    view.setTranslationY(translationY);
                }
                result = mInDragging;
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                result = mInDragging;
                mInDragging = false;
                mLastTranslationX = view.getTranslationX();
                mLastTranslationY = view.getTranslationY();
                break;
            }
        }
        return result;
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        computeBound();
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
    }

    @Override
    public void onGlobalLayout() {
        computeBound();
    }

    private void computeBound() {
        int[] v_location = new int[2];
        int[] p_location = new int[2];
        view.getLocationInWindow(v_location);
        View parent = (View) view.getParent();
        parent.getLocationInWindow(p_location);

        int x = v_location[0] - p_location[0];
        int y = v_location[1] - p_location[1];

        mViewRectF = new RectF(x, y, x + view.getWidth(), y + view.getHeight());

        int width = parent.getWidth();
        int height = parent.getHeight();

        mParentRectF = new RectF(0, 0, width, height);

        mMinTranslationX = -x;
        mMaxTranslationX = parent.getWidth() - view.getWidth() - x;

        mMinTranslationY = -y;
        mMaxTranslationY = parent.getHeight() - view.getHeight() - y;

    }
}