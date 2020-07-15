package com.hyh.web.widget.web;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

/**
 * @author Administrator
 * @description
 * @data 2020/7/13
 */
public class JSWebView extends WebView {

    public JSWebView(Context context) {
        super(context);
    }

    public JSWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TextView textView = new TextView(context);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200));
        textView.setText("这是一段文字");
        textView.setTextSize(30);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);

    }
}
