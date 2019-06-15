package com.hyh.web.multi.load;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * @author Administrator
 * @description
 * @data 2019/5/30
 */

public class AutoDetectLoadingModule extends LoadingModule {

    private static final String TAG = "LoadingModule";

    public AutoDetectLoadingModule(Context context, IFootView footView) {
        super(footView);
        DetectLayout detectLayout = new DetectLayout(context);
        detectLayout.addView(mFootContent);
        detectLayout.setBackgroundColor(Color.RED);
        setView(detectLayout);
    }

    @Override
    public void executeLoadMore() {

    }

    public class DetectLayout extends FrameLayout {

        public DetectLayout(@NonNull Context context) {
            super(context);
            setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }


        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            Log.d(TAG, "onMeasure: width = " + width + ", height = " + height);
        }

        @Override
        protected void onScrollChanged(int l, int t, int oldl, int oldt) {
            super.onScrollChanged(l, t, oldl, oldt);
            Log.d(TAG, "onScrollChanged: ");
        }

        @Override
        protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
            super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
            Log.d(TAG, "onOverScrolled: ");
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            Log.d(TAG, "onSizeChanged: " + getMeasuredHeight());
        }
    }
}