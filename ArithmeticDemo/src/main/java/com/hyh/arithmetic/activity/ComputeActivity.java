package com.hyh.arithmetic.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.hyh.arithmetic.R;
import com.hyh.arithmetic.count.ComputeUtil;

/**
 * @author Administrator
 * @description
 * @data 2020/6/4
 */
public class ComputeActivity extends Activity {

    private static final String TAG = "ComputeActivity_";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compute);
    }

    public void compute1(View view) {
        String expression = "1+(2*3)-(5+6)/ 7";
        double compute = ComputeUtil.compute(expression);
        Log.d(TAG, "compute1: " + compute);
    }

    public void compute2(View view) {
        String expression = "1+((2*3)-(5+6)/ 7)";
        double compute = ComputeUtil.compute(expression);
        Log.d(TAG, "compute2: " + compute);
    }

    public void compute3(View view) {
        String expression = "1*((2*3)-(5+6)/ 7)";
        double compute = ComputeUtil.compute(expression);
        Log.d(TAG, "compute3: " + compute);
    }

    public void compute4(View view) {
        String expression = "1-((2*3)-(5+6)/ 7)";
        double compute = ComputeUtil.compute(expression);
        Log.d(TAG, "compute4: " + compute);
    }
}
