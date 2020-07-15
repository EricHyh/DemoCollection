package com.hyh.common.floating;

/**
 * @author Administrator
 * @description
 * @data 2018/1/24
 */
public interface SlideController {

    void bindFloatWindow(FloatingWindow floatingWindow);

    boolean tryDrag(int[] initialPosition, int[] touchPosition);

    void onDrag(int[] touchPosition);

    void onReleased(int[] touchPosition, ReleaseListener listener);

}
