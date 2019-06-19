package com.hyh.web.behavior;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * @author Administrator
 * @description
 * @data 2019/5/29
 */

public class RecyclerViewBehavior extends BaseBehavior<RecyclerView> {

    public RecyclerViewBehavior(Context context) {
        super(context);
    }

    public RecyclerViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int scrollY(RecyclerView view, int dy) {
        if (dy > 0) {
            int curMaxScrollDy = computeCurrentMaxScrollUpDy(view);
            int viewScrollDy = Math.min(curMaxScrollDy, dy);
            view.scrollBy(0, viewScrollDy);
            return viewScrollDy;
        } else {
            int curMinScrollDy = computeCurrentMinScrollDownDy(view);
            int viewScrollDy = Math.max(curMinScrollDy, dy);
            view.scrollBy(0, viewScrollDy);
            return viewScrollDy;
        }
    }

    @Override
    protected void stopFling(RecyclerView view) {
        view.stopScroll();
    }

    private int computeCurrentMaxScrollUpDy(RecyclerView view) {
        int verticalScrollOffset = view.computeVerticalScrollOffset();
        int verticalScrollRange = view.computeVerticalScrollRange();
        int verticalScrollExtent = view.computeVerticalScrollExtent();
        return verticalScrollRange - verticalScrollExtent - verticalScrollOffset;
    }

    private int computeCurrentMinScrollDownDy(RecyclerView view) {
        return -view.getScrollY();
    }
}