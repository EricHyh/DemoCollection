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
import android.widget.TextView;
import android.widget.Toast;

import com.hyh.web.multi.ItemHolder;
import com.hyh.web.multi.MultiAdapter;
import com.hyh.web.multi.MultiModule;
import com.hyh.web.multi.load.ChrysanthemumFootView;
import com.hyh.web.multi.load.LastItemLoadingModule;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;

/**
 * @author Administrator
 * @description
 * @data 2019/5/30
 */

public class Scroll3Activity extends Activity {
    private static final String TAG = "Scroll3Activity";
    private SmartRefreshLayout mSmartRefreshLayout;

    private long mRefreshTimeMillis;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll3);

        mSmartRefreshLayout = findViewById(R.id.smart_refresh_layout);

        {

            ClassicsHeader classicsHeader = new ClassicsHeader(this);
            //classicsHeader.setArrowResource(R.drawable.flow_ic_pulltorefresh_dark_arrow);
            //smartRefreshLayout.setOnRefreshLoadMoreListener(this);
            mSmartRefreshLayout.setRefreshHeader(classicsHeader);
            mSmartRefreshLayout.setRefreshFooter(new ClassicsFooter(this));
            /*smartRefreshLayout.setEnableAutoLoadMore(true);
            smartRefreshLayout.setLoadMoreEnabled(true);*/
            mSmartRefreshLayout.setEnableAutoLoadMore(false);
            mSmartRefreshLayout.setEnableLoadMore(false);
            mSmartRefreshLayout.setEnableOverScrollDrag(false);
            mSmartRefreshLayout.setNestedScrollingEnabled(false);
            mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshLayout) {
                    mSmartRefreshLayout.finishRefresh( true);
                    mSmartRefreshLayout.setEnableRefresh(false);
                }
            });
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));


        MultiAdapter multiAdapter = new MultiAdapter(getApplicationContext());
        final NormalDataModule normalDataModule = new NormalDataModule();
        multiAdapter.addMultiModule(normalDataModule);
        LastItemLoadingModule lastItemLoadingModule = new LastItemLoadingModule(
                new ChrysanthemumFootView(getApplicationContext(), Color.WHITE, Color.GRAY, 0xFFDDDDDD));
        lastItemLoadingModule.bindScrollListener(recyclerView);
        /*final LoadingModule loadingModule = lastItemLoadingModule;
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
        recyclerView.setAdapter(multiAdapter);

    }

    public void executeRefresh(View view) {
        Log.d(TAG, "executeRefresh: ");
        mSmartRefreshLayout.autoRefresh();
    }

    public void finishRefresh(View view) {
        Log.d(TAG, "finishRefresh: ");
        mSmartRefreshLayout.finishRefresh();
    }

    public void enableRefresh(View view) {
        Log.d(TAG, "enableRefresh: ");
        mSmartRefreshLayout.setEnableRefresh(true);
    }

    public void disableRefresh(View view) {
        Log.d(TAG, "disableRefresh: ");
        mSmartRefreshLayout.setEnableRefresh(false);
    }

    public void enableRefreshAndExecuteRefresh(View view) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSmartRefreshLayout.setEnableRefresh(true);
                mSmartRefreshLayout.autoRefresh();
                mRefreshTimeMillis = System.currentTimeMillis();
            }
        },2000);

    }

    public void finishRefreshAndDisableRefresh(View view) {
        long currentTimeMillis = System.currentTimeMillis();
        long interval = currentTimeMillis - mRefreshTimeMillis;
        Log.d(TAG, "finishRefreshAndDisableRefresh: interval = " + interval);

        long delayed = 1000 - interval;

        mSmartRefreshLayout.setEnableRefresh(false);
        mSmartRefreshLayout.finishRefresh((int) delayed, true);
    }

    public void isRefreshing(View view) {
        Toast.makeText(this, "" + mSmartRefreshLayout.isRefreshing(), Toast.LENGTH_SHORT).show();
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
