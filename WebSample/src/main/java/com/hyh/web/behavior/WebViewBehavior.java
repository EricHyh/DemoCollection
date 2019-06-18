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
}