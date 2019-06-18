package com.hyh.web.behavior;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Scroller;

import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2019/6/13
 */

public class BaseBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    private static final String TAG = "BaseBehavior";

    private final Scroller mScroller;

    public BaseBehavior(Context context) {
        this(context, null);
    }

    public BaseBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) mScroller.abortAnimation();
        return super.onInterceptTouchEvent(parent, child, ev);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) dependency.getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        return behavior != null && isAbove(parent, child, dependency);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        List<View> dependencies = parent.getDependencies(child);
        int dependenciesHeight = 0;
        if (!dependencies.isEmpty()) {
            for (View dependency : dependencies) {
                dependenciesHeight += dependency.getMeasuredHeight();
            }
        }
        int width = child.getMeasuredWidth();
        int height = child.getMeasuredHeight();
        child.layout(0, dependenciesHeight, width, height + dependenciesHeight);
        return true;
    }

    protected int getAboveHeight(CoordinatorLayout parent, View child) {
        int childIndex = parent.indexOfChild(child);
        int aboveHeight = 0;
        for (int index = 0; index < childIndex; index++) {
            View childAt = parent.getChildAt(index);
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) childAt.getLayoutParams();
            if (!checkIsBaseBehavior(layoutParams)) continue;
            aboveHeight += childAt.getMeasuredHeight();
        }
        return aboveHeight;
    }

    protected int getContentHeight(CoordinatorLayout parent) {
        int childCount = parent.getChildCount();
        int parentHeight = parent.getMeasuredHeight();
        if (childCount <= 0) {
            return parentHeight;
        }
        int childHeight = 0;
        for (int index = 0; index < childCount; index++) {
            View childAt = parent.getChildAt(index);
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) childAt.getLayoutParams();
            if (!checkIsBaseBehavior(layoutParams)) continue;
            childHeight += parent.getChildAt(index).getMeasuredHeight();
        }
        return Math.max(parentHeight, childHeight);
    }

    protected int getBottomInvisibleHeight(CoordinatorLayout parent) {
        int contentHeight = getContentHeight(parent);
        int measuredHeight = parent.getMeasuredHeight();
        return contentHeight - measuredHeight - parent.getScrollY();
    }

    private boolean checkIsBaseBehavior(CoordinatorLayout.LayoutParams layoutParams) {
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        return behavior != null && behavior instanceof BaseBehavior;
    }

    protected boolean isTouched(View view, MotionEvent event) {
        float rawX = event.getRawX();
        float rawY = event.getRawY();

        int[] viewLocation = new int[2];
        view.getLocationOnScreen(viewLocation);
        int measuredWidth = view.getMeasuredWidth();
        int measuredHeight = view.getMeasuredHeight();
        int left = viewLocation[0];
        int top = viewLocation[1];
        int right = left + measuredWidth;
        int bottom = top + measuredHeight;
        return rawX >= left && rawX <= right && rawY >= top && rawY <= bottom;
    }

    protected boolean isAbove(CoordinatorLayout parent, View child, View dependency) {
        int childIndex = parent.indexOfChild(child);
        int dependencyIndex = parent.indexOfChild(dependency);
        return childIndex > dependencyIndex;
    }

    protected boolean isFirstBehavior(CoordinatorLayout coordinatorLayout, View child) {
        return coordinatorLayout.indexOfChild(child) == 0;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull V child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return isFirstBehavior(coordinatorLayout, child);
    }

    @Override
    public void onNestedScrollAccepted(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull V child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        super.onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target, axes, type);
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                  @NonNull V child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        consumed[1] = handleNestedScroll(coordinatorLayout, target, dy, type);
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                               @NonNull V child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        handleNestedScroll(coordinatorLayout, target, dyUnconsumed, type);
    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                   @NonNull V child, @NonNull View target, int type) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type);
    }

    @Override
    public boolean onNestedPreFling(@NonNull CoordinatorLayout coordinatorLayout,
                                    @NonNull V child, @NonNull View target, float velocityX, float velocityY) {
       return handleNestedFling(coordinatorLayout, child, target, velocityY);
    }

    @Override
    public boolean onNestedFling(@NonNull CoordinatorLayout coordinatorLayout,
                                 @NonNull V child, @NonNull View target, float velocityX, float velocityY, boolean consumed) {
        return handleNestedFling(coordinatorLayout, child, target, velocityY);
    }

    private int handleNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View target, int dy, int type) {
        if (dy == 0) return 0;
        int consumedDy = 0;
        int unconsumedDy = dy;

        // 先滑动parent，直到target处于合适的位置：
        // 1.当向上滑动时：
        //    （1）如果target已经全部显示在parent中了，说明target已经处于合适的位置
        //    （2）如果target的高度 > parent的高度，则将target与parent的底部对齐
        //    （3）如果target的高度 < parent的高度，则将target与parent的顶部对齐
        int parentScrollDy = scrollParentAtMostChildInRightPosition(coordinatorLayout, target, unconsumedDy);
        consumedDy += parentScrollDy;
        unconsumedDy -= parentScrollDy;
        if (canScrollVertically(target, unconsumedDy)) {
            return consumedDy;
        }
        if (unconsumedDy == 0) {
            return consumedDy;
        }
        if (unconsumedDy > 0) {//向上滑动
            parentScrollDy = scrollUpParentAtMostChildInvisible(coordinatorLayout, target, unconsumedDy);
            consumedDy += parentScrollDy;
            unconsumedDy -= parentScrollDy;
            if (unconsumedDy == 0) {
                return consumedDy;
            }
            if (type != ViewCompat.TYPE_TOUCH) {
                View nextScrollableView = findNextScrollableView(coordinatorLayout, target);
                while (nextScrollableView != null) {
                    parentScrollDy = scrollParentAtMostChildInRightPosition(coordinatorLayout, nextScrollableView, unconsumedDy);
                    consumedDy += parentScrollDy;
                    unconsumedDy -= parentScrollDy;
                    if (unconsumedDy == 0) {
                        return consumedDy;
                    }
                    int childScrollDy = scrollView(nextScrollableView, unconsumedDy);
                    consumedDy += childScrollDy;
                    unconsumedDy -= childScrollDy;
                    if (unconsumedDy == 0) {
                        return consumedDy;
                    }
                    parentScrollDy = scrollUpParentAtMostChildInvisible(coordinatorLayout, nextScrollableView, unconsumedDy);
                    consumedDy += parentScrollDy;
                    unconsumedDy -= parentScrollDy;
                    if (unconsumedDy == 0) {
                        return consumedDy;
                    }
                    nextScrollableView = findNextScrollableView(coordinatorLayout, target);
                }
            }
            parentScrollDy = scrollUpParentAtMostBottom(coordinatorLayout, unconsumedDy);
            consumedDy += parentScrollDy;
            unconsumedDy -= parentScrollDy;
            return consumedDy;
        } else {//向下滑动
            if (type != ViewCompat.TYPE_TOUCH) {
                View lastScrollableView = findLastScrollableView(coordinatorLayout, target);
                while (lastScrollableView != null) {
                    parentScrollDy = scrollDownParentAtMostChildTop(coordinatorLayout, lastScrollableView, unconsumedDy);
                    consumedDy += parentScrollDy;
                    unconsumedDy -= parentScrollDy;
                    if (unconsumedDy == 0) {
                        return consumedDy;
                    }
                    int childScrollDy = scrollView(lastScrollableView, unconsumedDy);
                    consumedDy += childScrollDy;
                    unconsumedDy -= childScrollDy;
                    if (unconsumedDy == 0) {
                        return consumedDy;
                    }
                    lastScrollableView = findLastScrollableView(coordinatorLayout, target);
                }
            }
            parentScrollDy = scrollDownParentAtMostTop(coordinatorLayout, unconsumedDy);
            consumedDy += parentScrollDy;
            unconsumedDy -= parentScrollDy;
            return consumedDy;
        }
    }

    private boolean handleNestedFling(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target, float velocityY) {
        if (isFirstBehavior(coordinatorLayout, child)) {
            int yvel = Math.round(velocityY);
            if (canScrollVertically(target, yvel)) {
                return false;
            }
            mScroller.fling(0, 0, 0, yvel, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (mScroller.computeScrollOffset()) {
                ViewCompat.postOnAnimation(coordinatorLayout, new FlingRunnable(coordinatorLayout, target));
                return true;
            }
        }
        return false;
    }

    private int scrollView(View view, int unconsumedDy) {
        if (unconsumedDy > 0) {
            int curMaxScrollDy = computeCurrentMaxScrollUpDy(view);
            int viewScrollDy = Math.min(curMaxScrollDy, unconsumedDy);
            view.scrollBy(0, viewScrollDy);
            return viewScrollDy;
        } else {
            int curMinScrollDy = computeCurrentMinScrollDownDy(view);
            int viewScrollDy = Math.max(curMinScrollDy, unconsumedDy);
            view.scrollBy(0, viewScrollDy);
            return viewScrollDy;
        }
    }

    private boolean canScrollVertically(View target, int unconsumedDy) {
        return target.canScrollVertically(unconsumedDy);
    }

    private int scrollParentAtMostChildInRightPosition(CoordinatorLayout parent, View child, int unconsumedDy) {
        int scrollY = parent.getScrollY();
        int parentHeight = parent.getMeasuredHeight();
        int childHeight = child.getMeasuredHeight();
        if (unconsumedDy > 0) {//向上滑动
            if (parentHeight > childHeight) {
                int aboveHeight = getAboveHeight(parent, child);
                if (scrollY >= aboveHeight) {
                    return 0;
                } else {
                    int curMaxScrollDy = aboveHeight - scrollY;
                    curMaxScrollDy = Math.min(curMaxScrollDy, getBottomInvisibleHeight(parent));
                    int parentScrollDy = Math.min(curMaxScrollDy, unconsumedDy);
                    parent.scrollBy(0, parentScrollDy);
                    return parentScrollDy;
                }
            } else {
                int aboveHeightWithSelf = getAboveHeight(parent, child) + childHeight;
                int parentHeightWithScrollY = parentHeight + scrollY;
                if (parentHeightWithScrollY >= aboveHeightWithSelf) {
                    return 0;
                } else {
                    int curMaxScrollDy = aboveHeightWithSelf - parentHeightWithScrollY;
                    int parentScrollDy = Math.min(curMaxScrollDy, unconsumedDy);
                    parent.scrollBy(0, parentScrollDy);
                    return parentScrollDy;
                }
            }
        } else {//向下滑动
            int aboveHeight = getAboveHeight(parent, child);
            if (scrollY <= aboveHeight) {
                return 0;
            } else {
                int curMinScrollDy = aboveHeight - scrollY;
                int parentScrollDy = Math.max(curMinScrollDy, unconsumedDy);
                parent.scrollBy(0, parentScrollDy);
                return parentScrollDy;
            }
        }
    }

    private int scrollUpParentAtMostChildInvisible(CoordinatorLayout parent, View target, int unconsumedDy) {
        int scrollY = parent.getScrollY();
        int aboveHeightWithSelf = getAboveHeight(parent, target) + target.getMeasuredHeight();
        if (scrollY >= aboveHeightWithSelf) {
            return 0;
        }
        int curMaxScrollDy = aboveHeightWithSelf - scrollY;
        curMaxScrollDy = Math.min(curMaxScrollDy, getBottomInvisibleHeight(parent));
        int parentScrollDy = Math.min(curMaxScrollDy, unconsumedDy);
        parent.scrollBy(0, parentScrollDy);
        return parentScrollDy;
    }

    private int scrollUpParentAtMostBottom(CoordinatorLayout parent, int unconsumedDy) {
        int parentScrollDy = Math.min(getBottomInvisibleHeight(parent), unconsumedDy);
        parent.scrollBy(0, parentScrollDy);
        return parentScrollDy;
    }

    private int scrollDownParentAtMostTop(CoordinatorLayout parent, int unconsumedDy) {
        int parentScrollDy = Math.max(-parent.getScrollY(), unconsumedDy);
        parent.scrollBy(0, parentScrollDy);
        return parentScrollDy;
    }

    private int scrollDownParentAtMostChildTop(CoordinatorLayout parent, View child, int unconsumedDy) {
        int scrollY = parent.getScrollY();
        int aboveHeight = getAboveHeight(parent, child);
        if (scrollY <= aboveHeight) {
            return 0;
        }
        int curMinScrollDy = aboveHeight - scrollY;
        int parentScrollDy = Math.max(curMinScrollDy, unconsumedDy);
        parent.scrollBy(0, parentScrollDy);
        return parentScrollDy;
    }

    private View findNextScrollableView(CoordinatorLayout coordinatorLayout, View target) {
        int childCount = coordinatorLayout.getChildCount();
        int targetIndex = coordinatorLayout.indexOfChild(target);
        if (targetIndex + 1 < childCount) {
            for (int index = targetIndex + 1; index < childCount; index++) {
                View childAt = coordinatorLayout.getChildAt(index);
                if (canScrollVertically(childAt, 1)) {
                    return childAt;
                }
            }
        }
        return null;
    }

    private View findLastScrollableView(CoordinatorLayout coordinatorLayout, View target) {
        int targetIndex = coordinatorLayout.indexOfChild(target);
        if (targetIndex > 0) {
            for (int index = targetIndex - 1; index >= 0; index--) {
                View childAt = coordinatorLayout.getChildAt(index);
                if (canScrollVertically(childAt, -1)) {
                    return childAt;
                }
            }
        }
        return null;
    }

    private int computeCurrentMaxScrollUpDy(View view) {
        if (view instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) view;
            int verticalScrollOffset = recyclerView.computeVerticalScrollOffset();
            int verticalScrollRange = recyclerView.computeVerticalScrollRange();
            int verticalScrollExtent = recyclerView.computeVerticalScrollExtent();
            return verticalScrollRange - verticalScrollExtent - verticalScrollOffset;
        } else if (view instanceof WebView) {
            WebView webView = (WebView) view;
            int contentHeight = Math.round(webView.getContentHeight() * view.getResources().getDisplayMetrics().density);
            return Math.max(contentHeight - webView.getMeasuredHeight(), 0);
        }
        return 0;
    }

    private int computeCurrentMinScrollDownDy(View view) {
        return -view.getScrollY();
    }

    private class FlingRunnable implements Runnable {

        private CoordinatorLayout mParent;
        private View mTarget;
        private int mLastFlingY;

        FlingRunnable(CoordinatorLayout parent, View target) {
            this.mParent = parent;
            this.mTarget = target;
        }

        @Override
        public void run() {
            if (mScroller.computeScrollOffset()) {
                int currY = mScroller.getCurrY();
                int dy = currY - mLastFlingY;
                Log.d(TAG, "FlingRunnable run: dy = " + dy);
                mLastFlingY = currY;
                handleNestedScroll(mParent, mTarget, dy, ViewCompat.TYPE_NON_TOUCH);
                if (!mScroller.isFinished()) {
                    ViewCompat.postOnAnimation(mParent, this);
                }
            }
        }
    }
}