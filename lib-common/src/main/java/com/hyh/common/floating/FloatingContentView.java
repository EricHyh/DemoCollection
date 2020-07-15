package com.hyh.common.floating;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;

/**
 * @author Administrator
 * @description
 * @data 2019/8/30
 */
public interface FloatingContentView {

    View onCreateView(Context context, FloatingWindow floatingWindow);

    boolean onKeyEvent(KeyEvent event);

    void onAttachedToWindow(View view);

    void onDetachedFromWindow(View view);

    void onWindowFocusChanged(boolean hasWindowFocus);

    void onDestroyView();
}
