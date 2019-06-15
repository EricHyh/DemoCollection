package com.hyh.web.behavior;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author Administrator
 * @description
 * @data 2019/5/29
 */

public class WebFooterBehavior extends BaseBehavior<View> {

    private static final String TAG = "Behavior";

    public WebFooterBehavior(Context context) {
        super(context);
    }

    public WebFooterBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getBehaviorType() {
        return WEB_FOOTER_BEHAVIOR;
    }
}