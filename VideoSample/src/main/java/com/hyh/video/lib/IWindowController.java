package com.hyh.video.lib;

/**
 * @author Administrator
 * @description
 * @data 2019/1/18
 */

public interface IWindowController {

    void addFlags(int flags);

    void clearFlags(int flags);

    void setRequestedOrientation(int requestedOrientation);

    void setScreenBrightness(float screenBrightness);

}
