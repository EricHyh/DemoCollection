package com.hyh.web.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import com.hyh.web.utils.DisplayUtil;


/**
 * Created by Administrator on 2017/4/15.
 */

public class CustomWebView extends WebView {

    private boolean needHandlerTouchEvent = false;
    private float FLING_DISTANCE;
    private float SLIDE_AREA;
    private float mDownX;
    private float mDownY;
    private float mLastX;
    private float mLastY;
    private boolean requestTouchEvent = false;
    private boolean initialRequestTouchEvent = false;

    public void setNeedHandlerTouchEvent(boolean needHandlerTouchEvent) {
        this.needHandlerTouchEvent = needHandlerTouchEvent;
    }

    public CustomWebView(Context context) {
        super(context.getApplicationContext());
        init(context.getApplicationContext());
    }

    public CustomWebView(Context context, AttributeSet attrs) {
        super(context.getApplicationContext(), attrs);
        init(context.getApplicationContext());
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context.getApplicationContext(), attrs, defStyleAttr);
        init(context.getApplicationContext());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context.getApplicationContext(), attrs, defStyleAttr, defStyleRes);
        init(context.getApplicationContext());
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context.getApplicationContext(), attrs, defStyleAttr, privateBrowsing);
        init(context.getApplicationContext());
    }

    private void init(Context context) {
        FLING_DISTANCE = DisplayUtil.dip2px(context, 8);
        SLIDE_AREA = DisplayUtil.dip2px(getContext(), 20);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!needHandlerTouchEvent) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                // Ignoring pointerId=1
                // because ACTION_DOWN was not received for this pointer before ACTION_MOVE.
                // It likely happened because  ViewDragHelper did not receive all the events in the event stream.
                // super.onTouchEvent(event);
                return true;
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                requestTouchEvent = false;
                initialRequestTouchEvent = false;
                requestDisallowInterceptTouchEvent(false);
                mLastX = event.getX();
                mLastY = event.getY();
                super.onTouchEvent(event);
                return true;
            //break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                float dx = moveX - mLastX;
                float dy = moveY - mLastY;
                mLastX = moveX;
                mLastY = moveY;
                if (Math.abs(mDownX - moveX) < FLING_DISTANCE && Math.abs(mDownY - moveY) < FLING_DISTANCE) {
                    requestDisallowInterceptTouchEvent(true);
                } else {
                    if (!initialRequestTouchEvent) {
                        initialRequestTouchEvent = true;
                        boolean scrollVertical = Math.abs(dy) > Math.abs(dx) * 0.5;

                        if (scrollVertical || mDownX > SLIDE_AREA) {
                            requestTouchEvent = true;
                        } else {
                            requestTouchEvent = false;
                        }
                    }
                    requestDisallowInterceptTouchEvent(requestTouchEvent);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                requestTouchEvent = false;
                initialRequestTouchEvent = false;
                requestDisallowInterceptTouchEvent(false);
                break;
        }
        return super.onTouchEvent(event);
    }
}
