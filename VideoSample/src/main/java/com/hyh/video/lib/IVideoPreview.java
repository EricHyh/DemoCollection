package com.hyh.video.lib;

import android.view.View;

/**
 * Created by Eric_He on 2019/2/24.
 */

public interface IVideoPreview {

    View getView();

    void setPreviewVisibility(int visibility);

    void onViewAdded();

    void onViewRemoved();

}