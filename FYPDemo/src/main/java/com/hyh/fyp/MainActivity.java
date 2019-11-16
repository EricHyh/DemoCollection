package com.hyh.fyp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hyh.fyp.behavior.HeaderNestedScrollView;
import com.hyh.fyp.behavior.ScrollableCoordinatorLayout;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ScrollableCoordinatorLayout mCoordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(new MyAdapter());

        HeaderNestedScrollView scrollView = findViewById(R.id.header_nested_scrollview);
        mCoordinatorLayout = findViewById(R.id.scrollable_coordinatorlayout);
        mCoordinatorLayout.setOnScrollChangeListener(new ScrollableCoordinatorLayout.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                Log.d(TAG, "onScrollChange: scrollY = " + scrollY);
            }
        });
    }

    public void expand(View view) {
        mCoordinatorLayout.setExpanded(true);
    }

    public void collapse(View view) {
        mCoordinatorLayout.setExpanded(false);
    }


    private static class ItemHolder extends RecyclerView.ViewHolder {

        ItemHolder(TextView itemView) {
            super(itemView);
        }
    }

    private static class MyAdapter extends RecyclerView.Adapter<ItemHolder> {

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(16);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(0, 30, 0, 30);
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new ItemHolder(textView);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            ((TextView) holder.itemView).setText("条目" + position);
        }

        @Override
        public int getItemCount() {
            return 100;
        }
    }
}