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
    public void setTitle(CharSequence title) {

    }

    @Override
    public void setMediaProgress(int progress) {

    }

    @Override
    public void setBufferingProgress(int progress) {

    }

    @Override
    public void setCurrentPosition(int currentPosition) {

    }

    @Override
    public void setDuration(int duration) {

    }

    @Override
    public void setStarIconStartStyle() {

    }

    @Override
    public void setStartIconPauseStyle() {

    }

    @Override
    public void setInitialViewClickListener(View.OnClickListener listener) {

    }

    @Override
    public void setControllerViewClickListener(View.OnClickListener listener) {

    }

    @Override
    public void setStartIconClickListener(View.OnClickListener listener) {

    }

    @Override
    public void setReplayIconClickListener(View.OnClickListener listener) {

    }

    @Override
    public void setRetryIconClickListener(View.OnClickListener listener) {

    }

    @Override
    public void setFullScreenToggleClickListener(View.OnClickListener listener) {

    }

    @Override
    public void setMobileDataConfirmIconClickListener(View.OnClickListener listener) {

    }

    @Override
    public void setBackIconClickListener(View.OnClickListener listener) {

    }

    @Override
    public void showInitialView(DataSource source) {

    }

    @Override
    public void showControllerView() {

    }

    @Override
    public void hideControllerView() {

    }

    @Override
    public void showLoadingView() {

    }

    @Override
    public void hideLoadingView() {

    }

    @Override
    public void showErrorView() {

    }

    @Override
    public void showEndView() {

    }

    @Override
    public void showMobileDataConfirm() {

    }

    @Override
    public void onFullScreenOpen() {

    }

    @Override
    public void onFullScreenClose() {

    }
}
