package com.hyh.web;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
        setContentView(R.layout.activity_web);
        final WebView webView = findViewById(R.id.web_view);
        WebClient webClient = new WebClient(this, webView);

        webClient.loadUrl("https://jumpluna.58.com/i/LZYBeQ6a1luDubj");

    }

}
