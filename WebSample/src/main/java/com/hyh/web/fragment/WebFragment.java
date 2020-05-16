package com.hyh.web.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.hyh.web.R;
import com.hyh.web.multi.ItemHolder;
import com.hyh.web.multi.MultiAdapter;
import com.hyh.web.multi.MultiModule;
import com.hyh.web.widget.web.CustomWebView;
import com.hyh.web.widget.web.ViewMoreWebView;
import com.hyh.web.widget.web.WebClient;

import java.util.ArrayList;


/**
 * Created by Eric_He on 2017/11/12.
 */

public class WebFragment extends Fragment {

    private CustomWebView mWebView;
    private WebClient mWebClient;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_scroll5, container, false);

        ViewMoreWebView webView = view.findViewById(R.id.web_view);
        webView.setFoldMode(ViewMoreWebView.FoldMode.NATIVE);
        mWebView = webView;


        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        MultiAdapter multiAdapter = new MultiAdapter(view.getContext());
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


        mWebClient = new WebClient(view.getContext(), mWebView, null, null, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //mWebClient.loadUrl("https://jumpluna.58.com/i/LZYBeQ6a1luDubj");
        mWebClient.loadUrl("http://www.baidu.com");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mWebClient.destroy();
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