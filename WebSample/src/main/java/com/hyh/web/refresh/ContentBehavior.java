package com.hyh.web.refresh;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

public class ContentBehavior extends CoordinatorLayout.Behavior {


    //private int mTotalDyConsumed;

    public ContentBehavior() {
    }

    public ContentBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        //mTotalDyConsumed = 0;
        return true;
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        //super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        if (type == ViewCompat.TYPE_NON_TOUCH) {//只对触摸事件处理，忽略惯性事件
            return;
        }
        consumed[1] = onNestedScroll(coordinatorLayout, child, dy);
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        /*super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        if (type == ViewCompat.TYPE_NON_TOUCH) {//只对触摸事件处理，忽略惯性事件
            return;
        }
        onNestedScroll(coordinatorLayout, child, dyUnconsumed);*/
    }

    private int onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, int dy) {
        int consumedDy = 0;
        final View contentView = child;
        int scrollY = coordinatorLayout.getScrollY();
        if (scrollY != 0) {
            if (((scrollY ^ dy) >>> 31 == 0)) {//正负同号
                consumedDy = Math.round(dy * 0.4f);
                coordinatorLayout.scrollBy(0, consumedDy);
            } else {
                consumedDy = dy < 0 ? Math.max(dy, -scrollY) : Math.min(dy, -scrollY);
                coordinatorLayout.scrollBy(0, Math.round(consumedDy * 0.4f));
            }
        } else if (!contentView.canScrollVertically(dy)) {//content滑动到顶部或者底部
            consumedDy = Math.round(dy * 0.4f);
            coordinatorLayout.scrollBy(0, consumedDy);
        }
        return consumedDy;
    }


    @Override
    public boolean onNestedPreFling(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, float velocityX, float velocityY) {
        int scrollY = coordinatorLayout.getScrollY();
        return scrollY != 0;
    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int type) {
        //super.onStopNestedScroll(coordinatorLayout, child, target, type);
        int scrollY = coordinatorLayout.getScrollY();
        if (scrollY != 0) {
            coordinatorLayout.scrollTo(0, 0);
        }
    }

    private View findContentView(CoordinatorLayout coordinatorLayout, View child) {
        return coordinatorLayout.getDependencies(child).get(0);
    }
}