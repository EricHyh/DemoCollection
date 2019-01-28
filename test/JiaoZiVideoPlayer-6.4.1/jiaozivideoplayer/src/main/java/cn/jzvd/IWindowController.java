package com.yly.mob.ssp.video;

import android.view.View;
import android.view.WindowManager;

/**
 * @author Administrator
 * @description
 * @data 2019/1/18
 */

public interface IWindowController {

    View getDecorView();

    void addFlags(int flags);

    void clearFlags(int flags);

    void setRequestedOrientation(int requestedOrientation);

    void setFlags(int flags, int mask);

    WindowManager.LayoutParams getAttributes();

    void setAttributes(WindowManager.LayoutParams a);

    void showActionBar();

    void hideActionBar();
}
