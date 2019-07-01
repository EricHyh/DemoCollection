package com.hyh.web;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyh.web.utils.DisplayUtil;
import com.hyh.web.widget.WebClient;

/**
 * @author Administrator
 * @description
 * @data 2019/6/15
 */

public class BannerWebActivity extends Activity {

    private static final String TAG = "BannerWebActivity";
    private WebClient mWebClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.setBackgroundColor(Color.RED);

        final WebView webView = new WebView(this);
        int width = DisplayUtil.getScreenWidth(this);
        int height = Math.round(width * 0.15f);
        webView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        linearLayout.addView(webView);


        final TextView textView = new TextView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        layoutParams.weight = 1;
        textView.setLayoutParams(layoutParams);
        textView.setTextSize(30);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);

        Log.d(TAG, "onCreate: " + getResources().getDisplayMetrics().density);

        linearLayout.addView(textView);

        webView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                int measuredWidth = webView.getMeasuredWidth();
                int measuredHeight = webView.getMeasuredHeight();
                if (measuredWidth > 0 && measuredHeight > 0) {
                    textView.setText(measuredWidth + "-" + measuredHeight + ", height/width = " + (measuredHeight * 1.0f / measuredWidth));
                }
                return true;
            }
        });

        setContentView(linearLayout);

        mWebClient = new WebClient(this, webView);

        //webClient.loadUrl("https://jumpluna.58.com/i/LZYBeQ6a1luDubj");
        mWebClient.loadUrl("http://192.168.3.145/Web/mobile/ad/index.html");

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebClient.clearCache(true);
        mWebClient.destroy();
    }
}
