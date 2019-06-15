package com.hyh.web;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.hyh.web.widget.WebClient;

/**
 * @author Administrator
 * @description
 * @data 2019/6/15
 */

public class WebActivity extends Activity {

    private static final String TAG = "WebActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final WebView webView = new CustomWebView(this);
        webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(webView);
        WebClient webClient = new WebClient(this, webView);

        webClient.loadUrl("https://jumpluna.58.com/i/LZYBeQ6a1luDubj");

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int scrollY = webView.getScrollY();
                Log.d(TAG, "run: scrollY = " + scrollY);
                handler.postDelayed(this, 2000);
            }
        }, 2000);
    }


    private static class CustomWebView extends WebView {

        public CustomWebView(Context context) {
            super(context);
        }

        @Override
        public void scrollTo(int x, int y) {
            super.scrollTo(x, y);
            Log.d(TAG, "scrollTo: y = " + y);
        }

        @Override
        protected void onScrollChanged(int l, int t, int oldl, int oldt) {
            super.onScrollChanged(l, t, oldl, oldt);
        }
    }
}
