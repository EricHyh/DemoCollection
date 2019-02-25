package com.hyh.video.lib;

import android.view.View;

/**
 * @author Administrator
 * @description
 * @data 2019/2/25
 */

public interface IVideoBackground {

    View getView();

    void onViewAdded();

    void onViewRemoved();

}