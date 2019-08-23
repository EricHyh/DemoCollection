package com.hyh.web;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.hyh.web.fling.AppBarLayout;
import com.hyh.web.multi.ItemHolder;
import com.hyh.web.multi.MultiModule;
import com.hyh.web.widget.CollapsingWebView;
import com.hyh.web.widget.IWebViewClient;
import com.hyh.web.widget.WebClient;

import java.util.ArrayList;

/**
 * @author Administrator
 * @description
 * @data 2019/5/30
 */

public class Scroll2Activity extends Activity implements IWebViewClient {

    private AppBarLayout mAppBarLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll2);
        mAppBarLayout = findViewById(R.id.app_bar_layout);
        final CollapsingWebView webView = (CollapsingWebView) findViewById(R.id.web_view);
        WebClient webClient = new WebClient(getApplicationContext(), webView);
        webClient.setOutWebViewClient(this);
        webClient.loadUrl("https://jumpluna.58.com/i/LZYBeQ6a1luDubj");

    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {

    }

    @Override
    public void onPageFinished(WebView view, String url) {
        /*RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        MultiAdapter multiAdapter = new MultiAdapter(getApplicationContext());
        final NormalDataModule normalDataModule = new NormalDataModule();
        multiAdapter.addMultiModule(normalDataModule);
        LastItemLoadingModule lastItemLoadingModule = new LastItemLoadingModule(
                new ChrysanthemumFootView(getApplicationContext(), Color.WHITE, Color.GRAY, 0xFFDDDDDD));
        lastItemLoadingModule.bindScrollListener(recyclerView);
        final LoadingModule loadingModule = lastItemLoadingModule;
        loadingModule.setLoadingListener(new LastItemLoadingModule.LoadingListener() {
            @Override
            public void onRefresh() {

            }

            @Override
            public void onLoadMore() {
                List<String> list = normalDataModule.getList();
                int size = list == null ? 0 : list.size();
                ArrayList<String> strings = new ArrayList<>();
                for (int index = size; index < size + 100; index++) {
                    strings.add("条目：" + index);
                }
                normalDataModule.addDataList(strings);
                loadingModule.loadMoreComplete(true);
            }
        });
        multiAdapter.addMultiModule(loadingModule);
        recyclerView.setAdapter(multiAdapter);*/

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) layoutParams.getBehavior();
        behavior.setNeedHandleBoundaryOffset(false);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

    }


    private static class NormalDataModule extends MultiModule<String> {

        NormalDataModule() {
            ArrayList<String> strings = new ArrayList<>();
            for (int index = 0; index < 100; index++) {
                strings.add("条目：" + index);
            }
            setDataList(strings);
        }

        @Override
        protected ItemHolder<String> onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setTextSize(30);
            textView.setTextColor(Color.BLACK);
            textView.setGravity(Gravity.CENTER);
            return new ItemHolder<String>(textView) {
                @Override
                protected void bindDataAndEvent() {
                    TextView textView = (TextView) itemView;
                    textView.setText(getData());
                }
            };
        }

        @Override
        protected int getItemViewType(int position) {
            return 1;
        }
    }
}
