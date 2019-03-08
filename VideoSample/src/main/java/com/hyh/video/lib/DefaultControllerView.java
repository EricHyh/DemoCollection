package com.hyh.video.lib;

import android.content.Context;
import android.view.View;

/**
 * @author Administrator
 * @description
 * @data 2019/2/25
 */

public class DefaultControllerView implements IControllerView {

    private final Context mContext;

    public DefaultControllerView(Context context) {
        this.mContext = context;
    }

    @Override
    public View getView() {
        return new View(mContext);
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
    public void setCurrentPosition(long currentPosition) {

    }

    @Override
    public void setDuration(long duration) {

    }

    @Override
    public void setStartIconStartStyle() {

    }

    @Override
    public void setStartIconPauseStyle() {

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
    public void showMobileDataConfirm() {

    }

    @Override
    public boolean isShowOperateView() {
        return false;
    }

    @Override
    public void showOperateView() {

    }

    @Override
    public void hideOperateView() {

    }

    @Override
    public void showEndView() {

    }

    @Override
    public void showErrorView() {

    }

    @Override
    public void showLoadingView() {

    }

    @Override
    public void hideLoadingView() {

    }
}