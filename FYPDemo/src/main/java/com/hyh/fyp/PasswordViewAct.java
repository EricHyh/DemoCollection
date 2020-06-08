package com.hyh.fyp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.hyh.fyp.widget.PasswordView;

/**
 * @author Administrator
 * @description
 * @data 2020/6/3
 */
public class PasswordViewAct extends Activity {

    private static final String TAG = "PasswordViewAct_";
    private PasswordView mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_view);
        mPasswordView = findViewById(R.id.password_view_1);
        mPasswordView.setPasswordListener(new PasswordView.PasswordListener() {
            @Override
            public void onCleared() {
                Log.d(TAG, "onCleared: ");
            }

            @Override
            public void onChanged(String password) {
                Log.d(TAG, "onChanged: " + password);
            }

            @Override
            public void onFinished(String password) {
                Log.d(TAG, "onFinished: " + password);
            }
        });
        mPasswordView.setText(null);
    }

    public void requestFocus(View view) {
        mPasswordView.requestFocus();
    }

    public void clearPassword(View view) {
        mPasswordView.setText(null);
    }
}