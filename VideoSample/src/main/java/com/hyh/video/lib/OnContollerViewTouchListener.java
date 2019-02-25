package com.hyh.video.lib;

/**
 * @author Administrator
 * @description
 * @data 2019/2/25
 */

public interface OnContollerViewTouchListener {

    void onHorizontalTouch(boolean rightward, float dx);

    void onVerticalTouch(boolean left, boolean downward, float dy);

}
