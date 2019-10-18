package com.hyh.video.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Eric_He on 2019/9/22.
 */

public class TestActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        final View decorView = getWindow().getDecorView();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                TextView textView = findViewById(R.id.text);
                textView.setText("1 2 3 4 5 6 7 8 9 a b c d e f g h i");
                textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                textView.setSingleLine(true);
                textView.setSelected(true);
                textView.setFocusable(true);
                textView.setFocusableInTouchMode(true);
                //new Handler().postDelayed(this,300);
            }
        });





    }
}
