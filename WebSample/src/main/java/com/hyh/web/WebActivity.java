package com.hyh.web;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.hyh.web.widget.ViewMoreWebView;
import com.hyh.web.widget.WebClient;

/**
 * Created by Eric_He on 2019/8/25.
 */

public class WebActivity extends Activity {

    private WebClient mWebClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewMoreWebView viewMoreWebView = new ViewMoreWebView(getApplication());
        viewMoreWebView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(viewMoreWebView);
        mWebClient = new WebClient(getApplication(), viewMoreWebView);
        //mWebClient.loadUrl("https://jumpluna.58.com/i/LZYBeQ6a1luDubj");
        mWebClient.loadUrl("http://192.168.3.145/web/mobile/a/a.html");
    }


    @Override
    public void onBackPressed() {
        if (mWebClient != null && mWebClient.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebClient != null) {
            mWebClient.destroy();
        }
    }
}
