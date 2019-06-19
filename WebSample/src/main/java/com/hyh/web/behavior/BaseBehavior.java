package com.hyh.web.behavior;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2019/6/13
 */

public abstract class BaseBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    private static final String TAG = "BaseBehavior";
    private NestedScrollHelper mScrollHelper;

    public BaseBehavior(Context context) {
        this(context, null);
    }

    public BaseBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScrollHelper = new NestedScrollHelper(context);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) mScrollHelper.stopFling();
        return super.onInterceptTouchEvent(parent, child, ev);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) dependency.getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        return behavior != null && NestedScrollHelper.isAbove(parent, child, dependency);
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

    protected abstract int scrollY(V view, int dy);

    protected abstract void stopFling(V view);

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull V child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return NestedScrollHelper.isFirstBehavior(coordinatorLayout, child);
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
        consumed[1] = mScrollHelper.handleNestedScroll(coordinatorLayout, target, dy, type);
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                               @NonNull V child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        mScrollHelper.handleNestedScroll(coordinatorLayout, target, dyUnconsumed, type);
    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                   @NonNull V child, @NonNull View target, int type) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type);
    }

    @Override
    public boolean onNestedPreFling(@NonNull CoordinatorLayout coordinatorLayout,
                                    @NonNull V child, @NonNull View target, float velocityX, float velocityY) {
        return mScrollHelper.handleNestedFling(coordinatorLayout, child, target, velocityY);
    }

    @Override
    public boolean onNestedFling(@NonNull CoordinatorLayout coordinatorLayout,
                                 @NonNull V child, @NonNull View target, float velocityX, float velocityY, boolean consumed) {
        return mScrollHelper.handleNestedFling(coordinatorLayout, child, target, velocityY);
    }
}