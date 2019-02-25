package com.hyh.video.lib;

import android.content.Context;
import android.view.View;

/**
 * @author Administrator
 * @description
 * @data 2019/2/25
 */

public class DefaultControllerView implements IControllerView {

    public DefaultControllerView(Context context) {
    }

    @Override
    public View getView() {
        return null;
    }

    @Override
    public void setTitle(CharSequence text) {

    }

    @Override
    public void setMediaProgress(int progress) {

    }

    @Override
    public void setBufferingProgress(int progress) {

    }

    @Override
    public void setCurrentPosition(long currentPosition) {

    }

    @Override
    public void setDuration(long duration) {

    }

    @Override
    public void setStartButtonStartStyle() {

    }

    @Override
    public void setStartButtonPauseStyle() {

    }

    @Override
    public void setControllerViewClickListener(View.OnClickListener listener) {

    }

    @Override
    public void setStartButtonClickListener(View.OnClickListener listener) {

    }

    @Override
    public void setReplayButtonClickListener(View.OnClickListener listener) {

    }

    @Override
    public void setRetryButtonClickListener(View.OnClickListener listener) {

    }

    @Override
    public void setFullScreenToggleClickListener(View.OnClickListener listener) {

    }

    @Override
    public void setBackClickListener(View.OnClickListener listener) {

    }

    @Override
    public void showControllerView() {

    }

    @Override
    public void hideControllerView() {

    }

    @Override
    public void showBottomProgress() {

    }

    @Override
    public void hideBottomProgress() {

    }

    @Override
    public void onFullScreenOpen() {

    }

    @Override
    public void onFullScreenClose() {

    }
}
