package com.hyh.web.widget.web;

import android.graphics.Bitmap;
import android.webkit.WebView;

/**
 * @author Administrator
 * @description
 * @data 2019/7/29
 */

public class BaseWebViewClient implements IWebViewClient {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {

    }

    @Override
    public void onPageFinished(WebView view, String url) {

    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

    }
}
