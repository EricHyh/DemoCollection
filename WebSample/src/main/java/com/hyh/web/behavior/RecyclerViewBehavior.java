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

    private static final String TAG = "Behavior";

    public RecyclerViewBehavior(Context context) {
        super(context);
    }

    public RecyclerViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getBehaviorType() {
        return RECYCLER_VIEW_BEHAVIOR;
    }
}