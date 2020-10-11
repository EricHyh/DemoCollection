package com.hyh.web.refresh;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

public class HeaderBehavior extends CoordinatorLayout.Behavior {


    public HeaderBehavior() {
    }

    public HeaderBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) dependency.getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        return behavior instanceof ContentBehavior;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        int width = child.getMeasuredWidth();
        int height = child.getMeasuredHeight();
        child.layout(0, -height, width, 0);
        return true;
    }
}