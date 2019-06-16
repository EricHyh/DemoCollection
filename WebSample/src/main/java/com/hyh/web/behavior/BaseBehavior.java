package com.hyh.web.behavior;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
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

public abstract class BaseBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    public static final int OTHER_BEHAVIOR = 0;
    public static final int WEB_HEADER_BEHAVIOR = 1;
    public static final int WEB_VIEW_BEHAVIOR = 2;
    public static final int WEB_FOOTER_BEHAVIOR = 3;
    public static final int RECYCLER_VIEW_BEHAVIOR = 4;

    private static final String TAG = "BaseBehavior";

    private final Scroller mScroller;
    Integer mTempHeaderHeight;
    Integer mTempFooterHeight;

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

    protected int getTopInvisibleHeight(CoordinatorLayout parent) {
        return parent.getScrollY();
    }

    protected int getBottomInvisibleHeight(CoordinatorLayout parent) {
        int contentHeight = getContentHeight(parent);
        int measuredHeight = parent.getMeasuredHeight();
        return contentHeight - measuredHeight - parent.getScrollY();
    }

    protected int getHeaderHeight(CoordinatorLayout parent) {
        if (mTempHeaderHeight != null) {
            return mTempHeaderHeight;
        }
        int childCount = parent.getChildCount();
        int headerHeight = 0;
        for (int index = 0; index < childCount; index++) {
            View childAt = parent.getChildAt(index);
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) childAt.getLayoutParams();
            if (!checkIsBaseBehavior(layoutParams)) continue;
            BaseBehavior behavior = (BaseBehavior) layoutParams.getBehavior();
            if (behavior != null && getBehaviorType() == WEB_HEADER_BEHAVIOR) {
                headerHeight += childAt.getMeasuredHeight();
            }
        }
        mTempHeaderHeight = headerHeight;
        return headerHeight;
    }

    protected int getFooterHeight(CoordinatorLayout parent) {
        if (mTempFooterHeight != null) {
            return mTempFooterHeight;
        }
        int childCount = parent.getChildCount();
        int footerHeight = 0;
        for (int index = 0; index < childCount; index++) {
            View childAt = parent.getChildAt(index);
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) childAt.getLayoutParams();
            if (!checkIsBaseBehavior(layoutParams)) continue;
            BaseBehavior behavior = (BaseBehavior) layoutParams.getBehavior();
            if (behavior != null && getBehaviorType() == WEB_FOOTER_BEHAVIOR) {
                footerHeight += childAt.getMeasuredHeight();
            }
        }
        mTempFooterHeight = footerHeight;
        return footerHeight;
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

    protected abstract int getBehaviorType();


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
        consumed[1] = handleNestedScroll(coordinatorLayout, target, dy, type, true);
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                               @NonNull V child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        handleNestedScroll(coordinatorLayout, target, dyUnconsumed, type, false);
    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                   @NonNull V child, @NonNull View target, int type) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type);
    }

    @Override
    public boolean onNestedPreFling(@NonNull CoordinatorLayout coordinatorLayout,
                                    @NonNull V child, @NonNull View target, float velocityX, float velocityY) {
        /*if (isFirstBehavior(coordinatorLayout, child)) {
            if (velocityY > 0) {//向上滑动
                boolean canScrollUp = canScrollUp(coordinatorLayout, target);//发起嵌套滑动的View是否能够向上滑动
                if (!canScrollUp) {
                    //如果不能向上滑动了,那就把看父布局能不能向上滑动
                    if (canScrollUp(coordinatorLayout, coordinatorLayout)) {
                        //父布局可以滑动,那么先滑动父布局
                        mScroller.fling(0,
                                coordinatorLayout.getScrollY(),
                                0,
                                Math.round(velocityY),
                                0,
                                0,
                                getParentCurrentMinScrollY(coordinatorLayout),
                                getParentCurrentMaxScrollY(coordinatorLayout));
                        if (mScroller.computeScrollOffset()) {
                            ViewCompat.postOnAnimation(coordinatorLayout, new FlingRunnable(coordinatorLayout));
                            return true;
                        }
                    }
                }
            } else {//向下滑动
                boolean canScrollDown = canScrollDown(coordinatorLayout, target);//发起嵌套滑动的View是否能够向下滑动


            }
        }*/
        return false;
    }

    @Override
    public boolean onNestedFling(@NonNull CoordinatorLayout coordinatorLayout,
                                 @NonNull V child, @NonNull View target, float velocityX, float velocityY, boolean consumed) {
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
    }

    private int handleNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View target,
                                   int dy, int type, boolean nestedPreScroll) {
        int consumedDy = dy;
        int scrollY = coordinatorLayout.getScrollY();

        if (dy > 0) {//向上滑动
            boolean canScrollUp = canScrollUp(coordinatorLayout, target);//发起嵌套滑动的View是否能够向上滑动
            if (canScrollUp) {
                //判断
                int aboveHeight = getAboveHeight(coordinatorLayout, target);//在target之上的View的高度
                if (scrollY > aboveHeight) {
                    //如果发起嵌套滑动的View没有展示完全，那么把滑动事件交给target
                    consumedDy = 0;
                } else {
                    int aboveVisibleHeight = aboveHeight - scrollY;//在target之上的View的可见高度
                    int bottomInvisibleHeight = getBottomInvisibleHeight(coordinatorLayout);//底部不可见的高度
                    int curMaxScrollDy = Math.min(aboveVisibleHeight, bottomInvisibleHeight);

                    consumedDy = Math.min(consumedDy, curMaxScrollDy);
                }
            } else {
                //如果target不能向上滑动，那么
                int aboveHeight = getAboveHeight(coordinatorLayout, target);//在target之上的View的高度
                int aboveHeightIncludeSelf = aboveHeight + target.getMeasuredHeight();//在target之上并且包含target的View的高度
                int aboveVisibleHeightIncludeSelf = aboveHeightIncludeSelf - scrollY;//在target之上并包括target的View的可见高度
                int bottomInvisibleHeight = getBottomInvisibleHeight(coordinatorLayout);//底部不可见的高度
                int curMaxScrollDy = Math.min(aboveVisibleHeightIncludeSelf, bottomInvisibleHeight);

                consumedDy = Math.min(consumedDy, curMaxScrollDy);

            }
        } else {//向下滑动
            //判断发起嵌套滑动的View是否没有展示完全
            int aboveHeight = getAboveHeight(coordinatorLayout, target);
            if (scrollY > aboveHeight) {
                //发起嵌套滑动的View没有展示完全,先滑动父布局，让发起嵌套滑动的View展示完全
                int curMinScrollDy = aboveHeight - scrollY;
                consumedDy = Math.max(consumedDy, curMinScrollDy);

                curMinScrollDy = -scrollY;
                consumedDy = Math.max(consumedDy, curMinScrollDy);
            } else {//发起嵌套滑动的View展示完全
                //发起嵌套滑动的View是否能够向下滑动，如果能向下滑动，那么把滑动事件交给target
                boolean canScrollDown = canScrollDown(coordinatorLayout, target);
                if (canScrollDown) {
                    consumedDy = 0;
                } else {
                    if (!nestedPreScroll && type != ViewCompat.TYPE_TOUCH) {
                        View lastVisibleScrollableView = findLastScrollableView(coordinatorLayout, target);
                        if (lastVisibleScrollableView != null) {
                            /*int curMinScrollDy = -getAboveHeight(coordinatorLayout, lastVisibleScrollableView) - scrollY;
                            consumedDy = Math.max(consumedDy, curMinScrollDy);*/
                        } else {
                            int curMinScrollDy = -scrollY;
                            consumedDy = Math.max(consumedDy, curMinScrollDy);
                        }
                    } else {
                        int curMinScrollDy = -scrollY;
                        consumedDy = Math.max(consumedDy, curMinScrollDy);
                    }
                }
            }
        }
        coordinatorLayout.scrollBy(0, consumedDy);
        if (!nestedPreScroll && type != ViewCompat.TYPE_TOUCH) {
            if (consumedDy != dy) {
                consumedDy += dispatchNestedScrollToNext(coordinatorLayout, target, (dy - consumedDy));
            }
        }
        return consumedDy;
    }

    private View findLastScrollableView(CoordinatorLayout coordinatorLayout, View target) {
        int targetIndex = coordinatorLayout.indexOfChild(target);
        if (targetIndex > 0) {
            for (int index = targetIndex - 1; index >= 0; index--) {
                View childAt = coordinatorLayout.getChildAt(index);
                if (childAt.canScrollVertically(-1)) {
                    return childAt;
                }
            }
        }
        return null;
    }

    private int dispatchNestedScrollToNext(CoordinatorLayout coordinatorLayout, View target, int unconsumedDy) {
        View view = null;
        int childCount = coordinatorLayout.getChildCount();
        int targetIndex = coordinatorLayout.indexOfChild(target);
        if (unconsumedDy > 0) {
            if (childCount > targetIndex + 1) {
                for (int index = targetIndex + 1; index < childCount; index++) {
                    View childAt = coordinatorLayout.getChildAt(index);
                    if (childAt.canScrollVertically(unconsumedDy)) {
                        view = childAt;
                    }
                }
            }
        } else {
            if (targetIndex > 0) {
                for (int index = targetIndex - 1; index >= 0; index--) {
                    View childAt = coordinatorLayout.getChildAt(index);
                    if (childAt.canScrollVertically(unconsumedDy)) {
                        view = childAt;
                    }
                }
            }
        }
        int viewConsumedDy = 0;
        if (view != null) {
            if (unconsumedDy > 0) {//向上滑动
                int curMaxScrollDy = getCurrentMaxScrollDy(view);
                viewConsumedDy = Math.min(unconsumedDy, curMaxScrollDy);
            } else {
                int curMinScrollDy = -view.getScrollY();
                viewConsumedDy = Math.max(unconsumedDy, curMinScrollDy);
            }
            view.scrollBy(0, viewConsumedDy);
            if (viewConsumedDy != unconsumedDy) {
                viewConsumedDy += dispatchNestedScrollToNext(coordinatorLayout, view, (unconsumedDy - viewConsumedDy));
            }
        }

        return unconsumedDy - viewConsumedDy;
    }

    private int getCurrentMaxScrollDy(View view) {
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

    private boolean canScrollUp(@NonNull CoordinatorLayout parent, @NonNull View target) {
        if (parent == target) {
            int scrollY = parent.getScrollY();
            int parentMaxScrollY = getParentMaxScrollY(parent);
            return scrollY < parentMaxScrollY;
        }
        return target.canScrollVertically(1);
    }

    private boolean canScrollDown(@NonNull CoordinatorLayout parent, @NonNull View target) {
        if (parent == target) {
            return parent.getScrollY() > 0;
        }
        return target.canScrollVertically(-1);
    }

    private int getParentMaxScrollY(@NonNull CoordinatorLayout parent) {
        return getContentHeight(parent) - parent.getMeasuredHeight();
    }

    private int getParentCurrentMaxScrollY(@NonNull CoordinatorLayout parent) {
        return getContentHeight(parent) - parent.getMeasuredHeight() - parent.getScrollY();
    }

    private int getParentCurrentMinScrollY(@NonNull CoordinatorLayout parent) {
        return -parent.getScrollY();
    }

    private class FlingRunnable implements Runnable {

        private CoordinatorLayout mParent;

        FlingRunnable(CoordinatorLayout parent) {
            this.mParent = parent;
        }

        @Override
        public void run() {
            if (mScroller.computeScrollOffset()) {
                int currY = mScroller.getCurrY();
                mParent.scrollTo(0, currY);
                ViewCompat.postOnAnimation(mParent, this);
            }
        }
    }
}