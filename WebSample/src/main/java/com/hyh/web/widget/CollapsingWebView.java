package com.hyh.web.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * @author Administrator
 * @description
 * @data 2019/5/30
 */

public class CollapsingWebView extends WebView {

    private static final String TAG = "CollapsingWebView";

    private boolean mIsCollapsing = false;

    public CollapsingWebView(Context context) {
        super(context);
    }

    public CollapsingWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCollapsing(boolean collapsing) {
        mIsCollapsing = collapsing;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
    }
}
