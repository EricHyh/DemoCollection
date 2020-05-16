package com.hyh.web.behavior;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * @author Administrator
 * @description
 * @data 2019/6/10
 */

public class WebViewBehavior extends BaseBehavior<WebView> {

    public WebViewBehavior(Context context) {
        this(context, null);
    }

    public WebViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int scrollY(WebView view, int dy) {
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
    protected void stopFling(WebView view) {
        //view.stopFling();
    }

    private int computeCurrentMaxScrollUpDy(WebView view) {
        int contentHeight = Math.round(view.getContentHeight() * view.getResources().getDisplayMetrics().density);
        return Math.max(contentHeight - view.getMeasuredHeight(), 0);
    }

    private int computeCurrentMinScrollDownDy(WebView view) {
        return -view.getScrollY();
    }

}