package com.hyh.web.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * @author Administrator
 * @description
 * @data 2019/5/30
 */

public class SelfRecyclerView extends RecyclerView {

    private static final String TAG = "SelfRecyclerView";

    public SelfRecyclerView(Context context) {
        super(context);
    }

    public SelfRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int height = getMeasuredHeight();
        Log.d(TAG, "onSizeChanged: height = " + height);
    }
}
