package com.hyh.fyp.behavior;

import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * @author Administrator
 * @description
 * @data 2019/11/15
 */
class NestedScrollHelper {

    static boolean isAbove(CoordinatorLayout parent, View child, View dependency) {
        int childIndex = parent.indexOfChild(child);
        int dependencyIndex = parent.indexOfChild(dependency);
        return childIndex > dependencyIndex;
    }

    static BaseBehavior getBaseBehavior(View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof CoordinatorLayout.LayoutParams) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) layoutParams;
            CoordinatorLayout.Behavior behavior = params.getBehavior();
            if (behavior instanceof BaseBehavior) {
                return (BaseBehavior) behavior;
            }
        }
        return null;
    }

    static int getTotalHeaderHeight(CoordinatorLayout parent) {
        int totalHeaderHeight = 0;
        int childCount = parent.getChildCount();
        for (int index = 0; index < childCount; index++) {
            View childAt = parent.getChildAt(index);
            if (childAt == null) continue;
            ViewGroup.LayoutParams layoutParams = childAt.getLayoutParams();
            if (!(layoutParams instanceof CoordinatorLayout.LayoutParams)) continue;
            CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) layoutParams).getBehavior();
            if (!(behavior instanceof HeaderNestedScrollView.HeaderBehavior)) continue;
            totalHeaderHeight += childAt.getMeasuredHeight();
        }
        return totalHeaderHeight;
    }

    static boolean isContentView(@NonNull View child) {
        ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
        if (layoutParams instanceof CoordinatorLayout.LayoutParams) {
            CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) layoutParams).getBehavior();
            return behavior instanceof ContentBehavior;
        }
        ViewParent parent = child.getParent();
        if (parent instanceof ViewGroup) {
            return isContentView((View) parent);
        }
        return false;
    }
}