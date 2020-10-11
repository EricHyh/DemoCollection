package com.hyh.fyp;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.hyh.fyp.widget.ExpandableLayout;

/**
 * @author Administrator
 * @description
 * @data 2020/8/5
 */
public class ExpandActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expand);
        ExpandableLayout expandableLayout = findViewById(R.id.expandable_layout);
        /*Rect rect = new Rect(0, 0, DisplayUtil.getScreenWidth(this), DisplayUtil.dip2px(this, 150));
        expandableLayout.setArrowDrawable(new ViewMoreDrawable(this, rect));*/
    }
}
