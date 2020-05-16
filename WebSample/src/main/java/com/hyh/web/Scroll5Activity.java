package com.hyh.web;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.hyh.web.multi.ItemHolder;
import com.hyh.web.multi.MultiAdapter;
import com.hyh.web.multi.MultiModule;
import com.hyh.web.widget.web.ViewMoreWebView;
import com.hyh.web.widget.web.WebClient;

import java.util.ArrayList;

/**
 * @author Administrator
 * @description
 * @data 2019/6/10
 */

public class Scroll5Activity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll5);
        final ViewMoreWebView webView = findViewById(R.id.web_view);
        webView.setFoldMode(ViewMoreWebView.FoldMode.NATIVE);
        WebClient webClient = new WebClient(getApplicationContext(), webView,null,null,null);
        webClient.loadUrl("https://jumpluna.58.com/i/LZYBeQ6a1luDubj");
        //webClient.loadUrl("http://xsh5f.ylyweb.com/");
        /*webClient.loadUrl("http://image.baidu.com/search/index?tn=baiduimage&ct=201326592&lm=-1&cl=2&ie=gb18030&word=%BA%DA%C9%AB%D6%F7%CC%E2&fr=ala&ala=1&alatpl=adress&pos=0&hs=2&xthttps=000000");*/


        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        MultiAdapter multiAdapter = new MultiAdapter(getApplicationContext());
        final NormalDataModule normalDataModule = new NormalDataModule();
        multiAdapter.addMultiModule(normalDataModule);
        /*LastItemLoadingModule lastItemLoadingModule = new LastItemLoadingModule(
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
        multiAdapter.addMultiModule(loadingModule);*/

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                normalDataModule.insertData("条目新", 0);
            }
        }, 10 * 1000);


        recyclerView.setAdapter(multiAdapter);
    }

    private static class NormalDataModule extends MultiModule<String> {

        private static final String TAG = "NormalDataModule";

        NormalDataModule() {
            ArrayList<String> strings = new ArrayList<>();
            for (int index = 0; index < 100; index++) {
                strings.add("条目：" + index);
            }
            setDataList(strings);
        }

        @Override
        protected ItemHolder<String> onCreateViewHolder(ViewGroup parent, int viewType) {
            final TextView textView = new TextView(parent.getContext());
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setTextSize(30);
            textView.setTextColor(Color.BLACK);
            textView.setGravity(Gravity.CENTER);


            textView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                }
            });

            final ViewTreeObserver.OnScrollChangedListener scrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    Log.d("TextView", "onScrollChanged: " + textView.getText());
                }
            };

            textView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(final View v) {
                    v.getViewTreeObserver().addOnScrollChangedListener(scrollChangedListener);
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    v.getViewTreeObserver().removeOnScrollChangedListener(scrollChangedListener);
                }
            });

            return new ItemHolder<String>(textView) {
                @Override
                protected void bindDataAndEvent() {
                    TextView textView = (TextView) itemView;
                    textView.setText(getData());
                    Log.d(TAG, "bindDataAndEvent: " + getData());
                }
            };
        }

        @Override
        protected int getItemViewType(int position) {
            return 1;
        }
    }
}