package com.hyh.arithmetic;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * @author Administrator
 * @description
 * @data 2019/11/28
 */
public class MainActivity extends Activity {

    private static final String TAG = "MainActivity_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int[] array = {0, 1, 2, 3, 1, 0, 5, 3, 2, 1, 4};//5+6=11

        int leftNum = 0;
        int leftIndex = 0;
        int rightNum = 0;
        int rightIndex = 0;
        int lastNum = 0;
        int total = 0;


        for (int index = 0; index < array.length; index++) {
            int num = array[index];

            if (num >= lastNum) {
                if (index - leftIndex == 1) {
                    leftNum = num;
                    leftIndex = index;
                } else if ((leftIndex > 0 && rightIndex == 0) || index - rightIndex == 1) {
                    rightNum = num;
                    rightIndex = index;

                }
            }
            if (index == array.length - 1 || num < lastNum) {
                if (leftIndex > 0 && rightIndex > 0) {
                    //计算
                    int min = Math.min(leftNum, rightNum);
                    for (int i = leftIndex + 1; i < rightIndex; i++) {
                        total += (min - array[i]);
                    }
                    leftNum = rightNum;
                    leftIndex = rightIndex;
                    rightNum = 0;
                    rightIndex = 0;
                }
            }
            lastNum = num;
        }

        Log.d(TAG, "onCreate: total = " + total);
    }
}