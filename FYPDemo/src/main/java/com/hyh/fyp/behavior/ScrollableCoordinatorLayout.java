package com.hyh.fyp.behavior;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

/**
 * @author Administrator
 * @description
 * @data 2019/11/16
 */
public class ScrollableCoordinatorLayout extends CoordinatorLayout {

    private OnScrollChangeListener mScrollChangeListener;
    private Scroller mScroller;

    public ScrollableCoordinatorLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ScrollableCoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ScrollableCoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        mScroller = new Scroller(context);
    }

    public void setOnScrollChangeListener(OnScrollChangeListener listener) {
        this.mScrollChangeListener = listener;
    }

    public void setExpanded(boolean expanded) {
        int scrollRange = getScrollRange();
        int scrollY = getScrollY();
        if (expanded) {
            //scrollTo(0, 0);
            mScroller.startScroll(0, scrollY, 0, -scrollY);
            postInvalidate();
        } else {
            mScroller.startScroll(0, scrollY, 0, scrollRange - scrollY);
            postInvalidate();
            //scrollTo(0, getScrollRange());
        }
    }


    public int getScrollRange() {
        return NestedScrollHelper.getTotalHeaderHeight(this);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        final OnScrollChangeListener listener = this.mScrollChangeListener;
        if (listener != null) {
            listener.onScrollChange(this, l, t, oldl, oldt);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            if (mScroller != null && !mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            invalidate();
        }
    }

    public interface OnScrollChangeListener {
        /**
         * Called when the scroll position of a view changes.
         *
         * @param v          The view whose scroll position has changed.
         * @param scrollX    Current horizontal scroll origin.
         * @param scrollY    Current vertical scroll origin.
         * @param oldScrollX Previous horizontal scroll origin.
         * @param oldScrollY Previous vertical scroll origin.
         */
        void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY);
    }
}