package com.hyh.fyp;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hyh.fyp.widget.CarouselTransformer;

/**
 * @author Administrator
 * @description
 * @data 2020/7/29
 */
public class ViewPagerActivity extends AppCompatActivity {

    private static final String TAG = "ViewPagerActivity_";

    private ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        setContentView(R.layout.activity_view_pager);
        mViewPager = findViewById(R.id.banner_view_pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setCurrentItem(10*10000);

        mViewPager.setPageTransformer(false, new CarouselTransformer(0.75f));
    }

    private PagerAdapter mPagerAdapter = new PagerAdapter() {

        private int[] colors = new int[]{Color.BLACK, Color.BLUE, Color.RED, Color.YELLOW, Color.CYAN, Color.GREEN, Color.GRAY, Color.MAGENTA,
                Color.DKGRAY, Color.LTGRAY};

        private String[] strs = new String[]{"黑色", "蓝色", "红色", "黄色", "亮蓝色", "绿色", "灰色", "MAGENTA", "DKGRAY", "LTGRAY"};

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            super.setPrimaryItem(container, position, object);
            Log.d(TAG, "setPrimaryItem: " + position);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {

            int color = colors[position % colors.length];
            TextView view = new TextView(container.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            view.setBackgroundColor(color);
            view.setText(strs[position % colors.length]);
            view.setTag(position);
            view.setGravity(Gravity.CENTER);
            view.setTextColor(Color.WHITE);
            view.setTextSize(20);

            container.addView(view);

            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return (int) ((View) object).getTag();
        }
    };
}