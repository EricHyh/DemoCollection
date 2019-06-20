package com.hyh.web;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.hyh.web.widget.WebClient;

/**
 * @author Administrator
 * @description
 * @data 2019/6/20
 */

public class SplashWebActivity extends Activity {

    private WebClient mWebClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webView = new WebView(this);
        webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(webView);
        mWebClient = new WebClient(this, webView);
        //webClient.loadUrl("https://jumpluna.58.com/i/LZYBeQ6a1luDubj");
        mWebClient.loadUrl("http://192.168.3.145/Web/mobile/ad/index1.html");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebClient.clearCache(true);
        mWebClient.destroy();
    }
}
