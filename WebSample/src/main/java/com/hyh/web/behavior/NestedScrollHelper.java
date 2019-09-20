package com.hyh.web.behavior;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * @author Administrator
 * @description
 * @data 2019/6/19
 */

class NestedScrollHelper {

    private static final String TAG = "NestedScrollHelper";

    private final Scroller mScroller;

    private View mCurrentFlingTarget;

    NestedScrollHelper(Context context) {
        Interpolator interpolator = new Interpolator() {
            @Override
            public float getInterpolation(float t) {
                t -= 1.0f;
                return t * t * t * t * t + 1.0f;
            }
        };
        mScroller = new Scroller(context, interpolator);
    }

    int handleNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View target, int dy, int type) {
        if (type != ViewCompat.TYPE_TOUCH) {
            mCurrentFlingTarget = target;
        }
        if (dy == 0) return 0;

        int consumedDy = 0;
        int unconsumedDy = dy;

        // 先滑动parent，直到target处于合适的位置：
        // 1.当向上滑动时：
        //    （1）如果target的高度 > parent的高度，则将target与parent的底部对齐
        //    （2）如果target的高度 < parent的高度，则将target与parent的顶部对齐
        int parentScrollDy = scrollParentAtMostChildInRightPosition(coordinatorLayout, target, unconsumedDy);

        Log.d(TAG, "handleNestedScroll: parentScrollDy = " + parentScrollDy);


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
                nextScrollableView = findNextScrollableView(coordinatorLayout, nextScrollableView);
            }
            parentScrollDy = scrollUpParentAtMostBottom(coordinatorLayout, unconsumedDy);
            consumedDy += parentScrollDy;
            unconsumedDy -= parentScrollDy;
            return consumedDy;
        } else {//向下滑动
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
                lastScrollableView = findLastScrollableView(coordinatorLayout, lastScrollableView);
            }
            parentScrollDy = scrollDownParentAtMostTop(coordinatorLayout, unconsumedDy);
            consumedDy += parentScrollDy;
            unconsumedDy -= parentScrollDy;
            return consumedDy;
        }
    }

    boolean handleNestedFling(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, float velocityY) {
        if (NestedScrollHelper.isFirstBehavior(coordinatorLayout, child)) {
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

    @SuppressWarnings("unchecked")
    void stopFling() {
        View currentFlingTarget = mCurrentFlingTarget;
        if (currentFlingTarget != null) {
            BaseBehavior behavior = getBaseBehavior(currentFlingTarget);
            if (behavior != null) {
                behavior.stopFling(currentFlingTarget);
            }
        }
        mScroller.abortAnimation();
    }

    @SuppressWarnings("unchecked")
    private int scrollView(View view, int unconsumedDy) {
        BaseBehavior behavior = getBaseBehavior(view);
        if (behavior != null) {
            return behavior.scrollY(view, unconsumedDy);
        }
        return 0;
    }

    private boolean canScrollVertically(View target, int unconsumedDy) {
        //惯性滑动时，这里个判断有时会判断错，以后再优化
        return target.canScrollVertically(unconsumedDy);
    }

    private int scrollParentAtMostChildInRightPosition(CoordinatorLayout parent, View child, int unconsumedDy) {
        int scrollY = parent.getScrollY();
        int parentHeight = parent.getMeasuredHeight();
        int childHeight = child.getMeasuredHeight();
        if (unconsumedDy > 0) {//向上滑动
            if (parentHeight > childHeight) {
                int aboveHeight = NestedScrollHelper.getAboveHeight(parent, child);
                if (scrollY >= aboveHeight) {
                    return 0;
                } else {
                    int curMaxScrollDy = aboveHeight - scrollY;
                    curMaxScrollDy = Math.min(curMaxScrollDy, NestedScrollHelper.getBottomInvisibleHeight(parent));
                    int parentScrollDy = Math.min(curMaxScrollDy, unconsumedDy);
                    parent.scrollBy(0, parentScrollDy);
                    return parentScrollDy;
                }
            } else {
                int aboveHeightWithSelf = NestedScrollHelper.getAboveHeight(parent, child) + childHeight;
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
            int aboveHeight = NestedScrollHelper.getAboveHeight(parent, child);
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
        int aboveHeightWithSelf = NestedScrollHelper.getAboveHeight(parent, target) + target.getMeasuredHeight();
        if (scrollY >= aboveHeightWithSelf) {
            return 0;
        }
        int curMaxScrollDy = aboveHeightWithSelf - scrollY;
        curMaxScrollDy = Math.min(curMaxScrollDy, NestedScrollHelper.getBottomInvisibleHeight(parent));
        int parentScrollDy = Math.min(curMaxScrollDy, unconsumedDy);
        parent.scrollBy(0, parentScrollDy);
        return parentScrollDy;
    }

    private int scrollUpParentAtMostBottom(CoordinatorLayout parent, int unconsumedDy) {
        int parentScrollDy = Math.min(NestedScrollHelper.getBottomInvisibleHeight(parent), unconsumedDy);
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
        int aboveHeight = NestedScrollHelper.getAboveHeight(parent, child);
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
                mLastFlingY = currY;
                handleNestedScroll(mParent, mTarget, dy, ViewCompat.TYPE_NON_TOUCH);
                if (!mScroller.isFinished()) {
                    ViewCompat.postOnAnimation(mParent, this);
                }
            }
        }
    }


    static int getAboveHeight(CoordinatorLayout parent, View child) {
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

    static int getContentHeight(CoordinatorLayout parent) {
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

    static int getBottomInvisibleHeight(CoordinatorLayout parent) {
        int contentHeight = getContentHeight(parent);
        int measuredHeight = parent.getMeasuredHeight();
        return contentHeight - measuredHeight - parent.getScrollY();
    }

    static boolean checkIsBaseBehavior(CoordinatorLayout.LayoutParams layoutParams) {
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        return behavior != null && behavior instanceof BaseBehavior;
    }

    static boolean isAbove(CoordinatorLayout parent, View child, View dependency) {
        int childIndex = parent.indexOfChild(child);
        int dependencyIndex = parent.indexOfChild(dependency);
        return childIndex > dependencyIndex;
    }

    static boolean isFirstBehavior(CoordinatorLayout coordinatorLayout, View child) {
        return coordinatorLayout.indexOfChild(child) == 0;
    }

    static BaseBehavior getBaseBehavior(View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams != null && layoutParams instanceof CoordinatorLayout.LayoutParams) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) layoutParams;
            CoordinatorLayout.Behavior behavior = params.getBehavior();
            if (behavior != null && behavior instanceof BaseBehavior) {
                return (BaseBehavior) behavior;
            }
        }
        return null;
    }
}