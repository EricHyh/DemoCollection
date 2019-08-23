package com.hyh.web;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.hyh.web.widget.CustomWebView;
import com.hyh.web.widget.WebClient;
import com.hyh.web.window.FloatingWindow;

/**
 * @author Administrator
 * @description
 * @data 2019/6/20
 */

public class SplashWebActivity extends Activity {

    private WebClient mWebClient;
    private static final String TAG = "SplashWebActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            KeyguardManager systemService = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (systemService != null) {
                systemService.requestDismissKeyguard(this, new KeyguardManager.KeyguardDismissCallback() {
                    @Override
                    public void onDismissError() {
                    }

                    @Override
                    public void onDismissSucceeded() {
                    }

                    @Override
                    public void onDismissCancelled() {
                    }
                });
            }
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                //
                window.setStatusBarColor(Color.parseColor("#000000"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        final FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(frameLayout);





        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) frameLayout.getRootView().getLayoutParams();
                IBinder token = params.token;
                new FloatingWindow
                        .Builder(getApplicationContext(), token, WindowManager.LayoutParams.TYPE_APPLICATION)
                        .view(new WebChildView())
                        .size(FloatingWindow.MATCH_PARENT, FloatingWindow.MATCH_PARENT)
                        .build()
                        .show();

            }
        }, 3000);



        /*WebView webView = new WebView(this);
        webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(webView);
        mWebClient = new WebClient(this, webView);
        //webClient.loadUrl("https://jumpluna.58.com/i/LZYBeQ6a1luDubj");
        //mWebClient.loadUrl("http://192.168.3.145/Web/mobile/ad/index1.html");
        mWebClient.loadUrl("http://192.168.3.145/Web/mobile/new/index.html");*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*mWebClient.clearCache(true);
        mWebClient.destroy();*/
    }


    private static class WebChildView implements FloatingWindow.FloatChildView {

        private WebClient mWebClient;

        @Override
        public View onCreateView(Context context, FloatingWindow floatingWindow) {
            ScrollView scrollView = new ScrollView(context);
            scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            scrollView.setFillViewport(true);
            WebView webView = new CustomWebView(context) {
                @Override
                protected void onSizeChanged(int w, int h, int ow, int oh) {
                    super.onSizeChanged(w, h, ow, oh);
                    Log.d(TAG, "onSizeChanged: " + getMeasuredHeight());
                }
            };

            webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            scrollView.addView(webView);

            mWebClient = new WebClient(context, webView);
            //webClient.loadUrl("https://jumpluna.58.com/i/LZYBeQ6a1luDubj");
            //mWebClient.loadUrl("http://192.168.3.145/Web/mobile/ad/index1.html");
            mWebClient.loadUrl("http://192.168.3.145/Web/mobile/new/index.html");
            return scrollView;
        }

        @Override
        public boolean onKeyEvent(KeyEvent event) {
            return false;
        }

        @Override
        public void onAttachedToWindow() {

        }

        @Override
        public void onDetachedFromWindow() {

        }

        @Override
        public void onWindowFocusChanged(boolean hasWindowFocus) {

        }

        @Override
        public void onDestroyView() {
            if (mWebClient != null) {
                mWebClient.clearCache(true);
                mWebClient.destroy();
            }
        }
    }
}
