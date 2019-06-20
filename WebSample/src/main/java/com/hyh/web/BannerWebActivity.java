package com.hyh.web;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

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
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        frameLayout.setBackgroundColor(Color.RED);


        WebView webView = new WebView(this);
        int width = DisplayUtil.getScreenWidth(this);
        int height = Math.round(width * 0.15f);
        webView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        frameLayout.addView(webView);

        setContentView(frameLayout);

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
