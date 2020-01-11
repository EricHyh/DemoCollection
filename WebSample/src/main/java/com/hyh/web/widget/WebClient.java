package com.hyh.web.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2017/10/24
 */

public class WebClient implements IWebViewClient, IWebChromeClient {

    private static final String APP_CACHE_DIRNAME = "webcache";

    private Context mContext;

    private WebView mWebView;

    private String mCurrentUrl;

    private IWebViewClient mOutWebViewClient;

    private IWebChromeClient mOutWebChromeClient;

    public void setOutWebViewClient(IWebViewClient outWebViewClient) {
        mOutWebViewClient = outWebViewClient;
    }

    public void setOutWebChromeClient(IWebChromeClient outWebChromeClient) {
        mOutWebChromeClient = outWebChromeClient;
    }

    public WebClient(Context context, WebView webView) {
        mContext = context.getApplicationContext();
        mWebView = webView;
        init();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        mWebView.setVisibility(View.VISIBLE);
        WebSettings settings = mWebView.getSettings();
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.supportMultipleWindows(); //多窗口
        settings.setJavaScriptEnabled(true);//js交互
        settings.setUseWideViewPort(true);//设置此属性，可任意比例缩放
        settings.setLoadWithOverviewMode(true);// 缩放至屏幕的大小
        settings.setBuiltInZoomControls(true);//设置支持缩放
        settings.setSupportZoom(true);//支持缩放
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true); //设置可以访问文件
        settings.setNeedInitialFocus(true); //当webview调用requestFocus时为webview设置节点
        settings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        settings.setLoadsImagesAutomatically(true); //支持自动加载图片
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        //开启缓存
        settings.setDomStorageEnabled(true);
        settings.setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = mContext.getCacheDir().getAbsolutePath() + File.separator + APP_CACHE_DIRNAME;
        settings.setAppCachePath(appCachePath);
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);


        mWebView.setDownloadListener(new DownloadListenerImpl(mContext));
        mWebView.setWebViewClient(new WebViewClientImpl(mContext, this));
        mWebView.setWebChromeClient(new WebChromeClientImpl(this));
    }


    public void loadUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            showErroView();
        } else {
            url = addHttpFlag(url);
            mWebView.loadUrl(url);
        }
    }

    private void showErroView() {
        mWebView.setVisibility(View.GONE);
    }

    private void showSuccessView() {
        mWebView.setVisibility(View.VISIBLE);
    }


    private static String addHttpFlag(String url) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }
        String temp = url.toLowerCase();
        if (!temp.startsWith("http://") && !temp.startsWith("https://")) {
            url = "http://" + url;
        }
        return url;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        mCurrentUrl = url;
        if (mOutWebViewClient != null) {
            mOutWebViewClient.onPageStarted(view, url, favicon);
        }
        /* mWebView.loadUrl("javascript:(function() {" + "var parent = document.getElementsByTagName('head').item(0);" + "var style = document.createElement('style');" + "style.type = 'text/css';" + "style.innerHTML = window.atob('" + AppContext.nightCode + "');" + "parent.appendChild(style)" + "})();");*/

    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (mOutWebViewClient != null) {
            mOutWebViewClient.onPageFinished(view, url);
        }
        /*mWebView.loadUrl("javascript:(function() {" + "var parent = document.getElementsByTagName('head').item(0);" + "var style = document.createElement('style');" + "style.type = 'text/css';" + "style.innerHTML = window.atob('" + AppContext.nightCode + "');" + "parent.appendChild(style)" + "})();");*/
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (!TextUtils.equals(mCurrentUrl, failingUrl)) {
            return;
        }
        if (errorCode == WebViewClient.ERROR_CONNECT
                || errorCode == WebViewClient.ERROR_TIMEOUT
                || errorCode == WebViewClient.ERROR_HOST_LOOKUP) {
            showErroView();
            if (mOutWebViewClient != null) {
                mOutWebViewClient.onReceivedError(view, errorCode, description, failingUrl);
            }
        }
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        if (newProgress > 60) {
            showSuccessView();
        }
        if (mOutWebChromeClient != null) {
            mOutWebChromeClient.onProgressChanged(view, newProgress);
        }
        /*mWebView.loadUrl("javascript:(function() {" + "var parent = document.getElementsByTagName('head').item(0);" + "var style = document.createElement('style');" + "style.type = 'text/css';" + "style.innerHTML = window.atob('" + AppContext.nightCode + "');" + "parent.appendChild(style)" + "})();");*/
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        if (mOutWebChromeClient != null) {
            mOutWebChromeClient.onReceivedTitle(view, title);
        }
    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        if (mOutWebChromeClient != null) {
            mOutWebChromeClient.onReceivedIcon(view, icon);
        }
    }

    public boolean onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        } else {
            return false;
        }
    }

    public void destroy() {
        try {
            if (mWebView != null) {
                ViewParent parent = mWebView.getParent();
                if (parent != null) {
                    ((ViewGroup) parent).removeView(mWebView);
                }
                mWebView.stopLoading();
                // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
                mWebView.getSettings().setJavaScriptEnabled(false);
                mWebView.clearHistory();
                mWebView.clearView();
                mWebView.removeAllViews();
                mWebView.destroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearCache(boolean includeDiskFiles) {
        mWebView.clearCache(includeDiskFiles);
    }

    private static class DownloadListenerImpl implements DownloadListener {

        Context mContext;

        DownloadListenerImpl(Context context) {
            mContext = context;
        }


        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PackageManager packageManager = mContext.getPackageManager();
            /*查询是否存在该intent对应的浏览器*/
            List<ResolveInfo> infoList =
                    packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (infoList != null && infoList.size() != 0) {
                ResolveInfo resolveInfo = infoList.get(0);
                if (resolveInfo.activityInfo != null && resolveInfo.activityInfo.packageName != null) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    intent.setPackage(packageName);
                }
            }
            try {
                mContext.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class WebViewClientImpl extends WebViewClient {

        Context mContext;

        WeakReference<IWebViewClient> mClientReference;

        WebViewClientImpl(Context context, IWebViewClient webViewClient) {
            mContext = context;
            mClientReference = new WeakReference<>(webViewClient);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            boolean flag = false;
            if (!TextUtils.isEmpty(url) && url.length() > 4) {
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    flag = false;
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        mContext.startActivity(intent);
                        flag = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        flag = true;
                    }
                }
            }
            /*view.loadUrl("javascript:(function() {" + "var parent = document.getElementsByTagName('head').item(0);" + "var style = document.createElement('style');" + "style.type = 'text/css';" + "style.innerHTML = window.atob('" + AppContext.nightCode + "');" + "parent.appendChild(style)" + "})();");*/
            return flag;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            IWebViewClient webViewClient = mClientReference.get();
            if (webViewClient != null) {
                webViewClient.onPageStarted(view, url, favicon);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            IWebViewClient webViewClient = mClientReference.get();
            if (webViewClient != null) {
                webViewClient.onPageFinished(view, url);
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            IWebViewClient webViewClient = mClientReference.get();
            if (webViewClient != null) {
                webViewClient.onReceivedError(view, errorCode, description, failingUrl);
            }
        }
    }

    private static class WebChromeClientImpl extends WebChromeClient {


        WeakReference<IWebChromeClient> mClientReference;

        WebChromeClientImpl(IWebChromeClient webChromeClient) {
            mClientReference = new WeakReference<>(webChromeClient);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            IWebChromeClient webChromeClient = mClientReference.get();
            if (webChromeClient != null) {
                webChromeClient.onProgressChanged(view, newProgress);
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            IWebChromeClient webChromeClient = mClientReference.get();
            if (webChromeClient != null) {
                webChromeClient.onReceivedTitle(view, title);
            }
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
            IWebChromeClient webChromeClient = mClientReference.get();
            if (webChromeClient != null) {
                webChromeClient.onReceivedIcon(view, icon);
            }
        }
    }
}
