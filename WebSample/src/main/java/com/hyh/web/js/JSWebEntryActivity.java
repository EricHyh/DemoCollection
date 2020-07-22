package com.hyh.web.js;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.hyh.web.R;

import java.util.List;
import java.util.Set;

/**
 * @author Administrator
 * @description
 * @data 2020/7/14
 */
public class JSWebEntryActivity extends Activity {

    private static final String TAG = "JSWebEntry_";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_js_web_entry);


        String url = "https://jdda.jd.com/app/hd/?mp=1#/turntable2?token=cd89e910f5f5492db0191e0d46e00f66&from=txatyly";

        Uri parse = Uri.parse(url);

        Set<String> queryParameterNames = parse.getQueryParameterNames();
        for (String queryParameterName : queryParameterNames) {
            Log.d(TAG, "queryParameterName: " + queryParameterName);
        }

        String fragment = parse.getFragment();
        Log.d(TAG, "fragment: " + fragment);




        List<String> pathSegments = Uri.parse(url).getPathSegments();
        for (String pathSegment : pathSegments) {
            Log.d(TAG, "onCreate: " + pathSegment);
        }
    }

    public void test1(View view) {
        JSWeb1Activity.start(this);
    }

    public void test2(View view) {
        JSWeb2Activity.start(this);
    }

    public void test3(View view) {
        JSWeb3Activity.start(this);
    }
}
