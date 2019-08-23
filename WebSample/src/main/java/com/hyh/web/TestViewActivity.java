package com.hyh.web;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * @author Administrator
 * @description
 * @data 2019/7/17
 */

public class TestViewActivity extends AppCompatActivity {

    private static final String TAG = "TestViewActivity";


    int mOffsetTopAndBottom;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_view);
        final View view1 = findViewById(R.id.view1);
        final View view2 = findViewById(R.id.view2);




        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: view1 isViewInScreen " + ViewUtil.getVisibleScale(view1));
                Log.d(TAG, "run: view2 isViewInScreen " + ViewUtil.getVisibleScale(view2));
            }
        }, 2000);*/

        view1.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Log.d(TAG, "onPreDraw: ");
                return true;
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mOffsetTopAndBottom++;
                view1.offsetTopAndBottom(mOffsetTopAndBottom);
                new Handler().postDelayed(this, 200);
            }
        }, 2000);

    }
}