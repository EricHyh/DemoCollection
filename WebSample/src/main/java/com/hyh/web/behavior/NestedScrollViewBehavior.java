package com.hyh.web.behavior;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.widget.OverScroller;

import com.hyh.web.reflect.Reflect;

/**
 * @author Administrator
 * @description
 * @data 2019/5/29
 */

public class NestedScrollViewBehavior extends BaseBehavior<NestedScrollView> {

    public NestedScrollViewBehavior(Context context) {
        super(context);
    }

    public NestedScrollViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int scrollY(NestedScrollView view, int dy) {
        return 0;
    }

    @Override
    protected void stopFling(NestedScrollView view) {
        OverScroller scroller = Reflect.from(NestedScrollView.class).filed("mScroller", OverScroller.class).get(view);
        if (scroller != null) {
            scroller.abortAnimation();
        }
    }
}