package com.hyh.page.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Administrator
 * @description
 * @data 2020/7/23
 */
public class PageLayout extends ViewGroup {

    public PageLayout(Context context) {
        super(context);
    }

    public PageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PageLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void dispatchWindowFocusChanged(boolean hasFocus) {
        //super.dispatchWindowFocusChanged(hasFocus);
        onWindowFocusChanged(hasFocus);

        View topChild = getTopChild();
        if (topChild != null) {
            topChild.dispatchWindowFocusChanged(hasFocus);
        }

        /*final int count = mChildrenCount;
        final View[] children = mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchWindowFocusChanged(hasFocus);
        }*/

    }

    private View getTopChild() {
        return getChildAt(getChildCount() - 1);
    }

    @Override
    public void bringChildToFront(View child) {
        super.bringChildToFront(child);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}