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

    private boolean receiveActionDown;

    /*@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        boolean dispatched = super.dispatchTouchEvent(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                receiveActionDown = true;
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP: {
                if (!receiveActionDown) {
                    return false;
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                receiveActionDown = false;
                break;
            }
        }
        return dispatched;
    }*/
}
