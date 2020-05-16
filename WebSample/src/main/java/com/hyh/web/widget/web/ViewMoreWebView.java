package com.hyh.web.widget.web;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.hyh.web.utils.DisplayUtil;
import com.hyh.web.widget.ViewMoreDrawable;


/**
 * Created by Eric_He on 2019/8/25.
 */

public class ViewMoreWebView extends NewNestedScrollWebView {

    private static final String TAG = "ViewMoreWebView";

    private int mInitialTouchX;
    private int mInitialTouchY;

    private int mFoldMode = FoldMode.NONE;

    private int mTouchSlop;

    private int mViewMoreAreaHeight;
    private ViewMoreDrawable mViewMoreDrawable;
    private Rect mViewMoreTouchRect = new Rect();
    private Rect mViewMoreClickRect = new Rect();
    private boolean mIsTouchViewMoreArea;
    private boolean mShouldHideViewMoreAreaOnTouchUp;

    private boolean mIsHandledUnfold;

    public ViewMoreWebView(Context context) {
        super(context);
        init();
    }

    public ViewMoreWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewMoreWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOverScrollMode(OVER_SCROLL_NEVER);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    public void setFoldMode(int foldMode) {
        this.mFoldMode = foldMode;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mFoldMode == FoldMode.HTML) {
            handleHtmlFoldMode();
        } else if (mFoldMode == FoldMode.NATIVE) {
            if (mIsHandledUnfold) {
                return;
            }
            if (mViewMoreDrawable != null && isAllowDrawViewMoreArea()) {
                mViewMoreDrawable.draw(canvas);
            }
        }
    }

    private void handleHtmlFoldMode() {
        int contentHeight = getWebContentHeight();
        if (mIsHandledUnfold) {
            if (contentHeight < Math.round(1.2f * DisplayUtil.getScreenHeight(getContext()))) {
                mIsHandledUnfold = false;
            }
            return;
        }
        if (contentHeight > Math.round(1.2f * DisplayUtil.getScreenHeight(getContext()))) {
            mIsHandledUnfold = true;
            ViewGroup parent = (ViewGroup) getParent();
            int parentScrollY = parent.getScrollY();
            if (parentScrollY > 0) {
                int[] location = new int[2];
                getLocationInWindow(location);
                int[] parentLocation = new int[2];
                parent.getLocationInWindow(parentLocation);
                int offsetRelativeToParent = location[1] - parentLocation[1];

                int currentMaxScrollUpDy = computeCurrentMaxScrollUpDy();
                currentMaxScrollUpDy = Math.min(currentMaxScrollUpDy, -offsetRelativeToParent);
                scrollBy(0, currentMaxScrollUpDy);
                parent.scrollBy(0, -currentMaxScrollUpDy);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        if (mFoldMode == FoldMode.NATIVE) {
            mViewMoreAreaHeight = DisplayUtil.dip2px(getContext(), 150);
            Rect rect = new Rect(0, height, width, height + mViewMoreAreaHeight);
            mViewMoreDrawable = new ViewMoreDrawable(getContext(), rect);
            mViewMoreDrawable.setCallback(this);
            mViewMoreTouchRect.set(mViewMoreDrawable.getBounds());
            mViewMoreClickRect.set(mViewMoreDrawable.getContentBounds());
        }
    }

    private boolean isAllowDrawViewMoreArea() {
        return getWebContentHeight() >= 1.8f * getMeasuredHeight();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mFoldMode != FoldMode.NATIVE) {
            return super.dispatchTouchEvent(event);
        }
        if (mIsHandledUnfold) {
            return super.dispatchTouchEvent(event);
        }
        int action = event.getActionMasked();
        if (action != MotionEvent.ACTION_DOWN && !mIsTouchViewMoreArea) {
            return super.dispatchTouchEvent(event);
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mIsTouchViewMoreArea = false;
                mShouldHideViewMoreAreaOnTouchUp = false;
                mInitialTouchX = Math.round(event.getX());
                mInitialTouchY = Math.round(event.getY());
                if (mViewMoreTouchRect.contains(mInitialTouchX, mInitialTouchY + getScrollY())) {
                    mIsTouchViewMoreArea = true;
                    if (mViewMoreClickRect.contains(mInitialTouchX, mInitialTouchY + getScrollY())) {
                        mShouldHideViewMoreAreaOnTouchUp = true;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int x = Math.round(event.getX());
                int y = Math.round(event.getY());
                int tx = mInitialTouchX - x;
                int ty = mInitialTouchY - y;
                if (Math.abs(tx) > mTouchSlop || Math.abs(ty) > mTouchSlop) {
                    mShouldHideViewMoreAreaOnTouchUp = false;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (mShouldHideViewMoreAreaOnTouchUp) {
                    handleNativeFoldMode();
                }
                event.setAction(MotionEvent.ACTION_CANCEL);
                mShouldHideViewMoreAreaOnTouchUp = false;
                mIsTouchViewMoreArea = false;
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                mShouldHideViewMoreAreaOnTouchUp = false;
                mIsTouchViewMoreArea = false;
                break;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void handleNativeFoldMode() {
        mIsHandledUnfold = true;
        postInvalidate();
        ViewGroup parent = (ViewGroup) getParent();
        int parentScrollY = parent.getScrollY();
        if (parentScrollY > 0) {
            int[] location = new int[2];
            getLocationInWindow(location);
            int[] parentLocation = new int[2];
            parent.getLocationInWindow(parentLocation);
            int offsetRelativeToParent = location[1] - parentLocation[1];

            int currentMaxScrollUpDy = computeCurrentMaxScrollUpDy();
            currentMaxScrollUpDy = Math.min(currentMaxScrollUpDy, -offsetRelativeToParent);
            scrollBy(0, currentMaxScrollUpDy);
            parent.scrollBy(0, -currentMaxScrollUpDy);
        }
    }

    private int computeCurrentMaxScrollUpDy() {
        int contentHeight = getWebContentHeight();
        return Math.max(contentHeight - getMeasuredHeight(), 0);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if (direction > 0
                && !mIsHandledUnfold && mViewMoreDrawable != null && isAllowDrawViewMoreArea()
                && super.canScrollVertically(direction)) {
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
        if (!mIsHandledUnfold && mViewMoreDrawable != null && isAllowDrawViewMoreArea()) {
            int scrollY = getScrollY();
            if (scrollY > mViewMoreAreaHeight) {
                scrollY = mViewMoreAreaHeight;
                scrollTo(0, scrollY);
            }
        }
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return who == mViewMoreDrawable || super.verifyDrawable(who);
    }

    public static class FoldMode {

        public static final int NONE = 0;
        public static final int HTML = 1;
        public static final int NATIVE = 2;

    }
}