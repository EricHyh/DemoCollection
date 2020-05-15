package com.hyh.web;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.hyh.web.behavior.NestedScrollWebView;
import com.hyh.web.widget.CustomWebView;
import com.hyh.web.widget.WebClient;

/**
 * Created by Eric_He on 2019/8/25.
 */

public class WebActivity extends Activity {

    private WebClient mWebClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomWebView customWebView = new NestedScrollWebView(getApplication());
        customWebView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(customWebView);
        mWebClient = new WebClient(getApplication(), customWebView, null, null, null);
        //mWebClient.loadUrl("https://jumpluna.58.com/i/LZYBeQ6a1luDubj");
        mWebClient.addJavascriptInterface(new TestJs(this), "testJs");
        //mWebClient.loadUrl("http://192.168.3.145/web/mobile/a/a.html");
        //mWebClient.loadUrl("http://192.168.3.145/web/mobile/3/index.html");
        mWebClient.loadUrl("https://jumpluna.58.com/i/LZYBeQ6a1luDubj");
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

    public static class TestJs {

        private Context mContext;

        public TestJs(Context context) {
            this.mContext = context;
        }

        /*@JavascriptInterface
        public void test() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "test test", Toast.LENGTH_SHORT).show();
                }
            });
        }*/

    }

}
