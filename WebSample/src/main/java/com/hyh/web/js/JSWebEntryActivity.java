package com.hyh.web.js;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.hyh.web.R;

/**
 * @author Administrator
 * @description
 * @data 2020/7/14
 */
public class JSWebEntryActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_js_web_entry);

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
