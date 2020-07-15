package com.hyh.web.js;

import android.webkit.WebView;

import java.util.Locale;

/**
 * @author Administrator
 * @description
 * @data 2020/7/13
 */
public class AdPosition {

    int id;

    int type;//文章顶部广告、文章结束广告、推荐列表广告

    String show;

    public void show(WebView webView, AdData adData) {
        webView.loadUrl("javascript:" + String.format(Locale.getDefault(), show, adData.toJson()));
    }
}