package com.hyh.fyp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * @author Administrator
 * @description
 * @data 2020/6/3
 */
public class PasswordViewAct extends Activity {

    private static final String TAG = "PasswordViewAct_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_view);
    }

    public void requestFocus(View view) {
    }

    public void clearPassword(View view) {
    }
}