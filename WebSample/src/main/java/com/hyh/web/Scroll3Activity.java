package com.hyh.web;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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
import com.hyh.web.multi.MultiItemFactory;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.util.ArrayList;

public class Scroll3Activity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll3);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        MultiAdapter multiAdapter = new MultiAdapter(this);

        multiAdapter.addMultiModule(new NormalDataModule());

        recyclerView.setAdapter(multiAdapter);


        SmartRefreshLayout smartRefreshLayout = findViewById(R.id.smart_refresh_layout);
        smartRefreshLayout.setNestedScrollingEnabled(true);
    }


    private static class NormalDataModule extends MultiItemFactory<String> {

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
