package com.hyh.video.lib;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * @author Administrator
 * @description
 * @data 2019/3/21
 */

public class DefaultVideoBackground implements IVideoBackground {

    @Override
    public Drawable getBackgroundDrawable() {
        return new ColorDrawable(Color.BLACK);
    }

    @Override
    public View getBackgroundView() {
        return null;
    }
}
