package com.hyh.web.refresh;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

public class FooterBehavior extends CoordinatorLayout.Behavior {

    public FooterBehavior() {
    }

    public FooterBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        int width = child.getMeasuredWidth();
        int height = child.getMeasuredHeight();
        child.layout(0, parent.getMeasuredHeight(), width, parent.getMeasuredHeight() + height);
        return true;
    }
}