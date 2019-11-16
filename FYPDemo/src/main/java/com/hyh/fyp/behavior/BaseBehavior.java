package com.hyh.fyp.behavior;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2019/11/15
 */
public abstract class BaseBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    private static final String TAG = "BaseBehavior";

    public BaseBehavior() {
    }

    public BaseBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull View child, View dependency) {
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) dependency.getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        return behavior != null && NestedScrollHelper.isAbove(parent, child, dependency);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, @NonNull View child, int layoutDirection) {
        List<View> dependencies = parent.getDependencies(child);
        int dependenciesHeight = 0;
        if (!dependencies.isEmpty()) {
            for (View dependency : dependencies) {
                dependenciesHeight += dependency.getMeasuredHeight();
            }
        }
        int width = child.getMeasuredWidth();
        int height = child.getMeasuredHeight();
        try {
            child.layout(0, dependenciesHeight, width, height + dependenciesHeight);
        } catch (Exception e) {
            Log.d(TAG, "BaseBehavior onLayoutChild child = " + child + " ", e);
        }
        return true;
    }
}