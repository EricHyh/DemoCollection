package com.hyh.video.lib;

import android.view.View;

/**
 * @author Administrator
 * @description
 * @data 2019/2/25
 */

public interface IControllerView {

    View getView();

    void setTitle(CharSequence title);

    void setMediaProgress(int progress);

    void setBufferingProgress(int progress);

    void setCurrentPosition(long currentPosition);

    void setDuration(long duration);

    void setStartIconStartStyle();

    void setStartIconPauseStyle();

    void setControllerViewClickListener(View.OnClickListener listener);

    void setStartIconClickListener(View.OnClickListener listener);

    void setReplayIconClickListener(View.OnClickListener listener);

    void setRetryIconClickListener(View.OnClickListener listener);

    void setFullScreenToggleClickListener(View.OnClickListener listener);

    void setMobileDataConfirmIconClickListener(View.OnClickListener listener);

    void setBackIconClickListener(View.OnClickListener listener);

    void showInitialView(DataSource source);

    void showMobileDataConfirm();

    boolean isShowOperateView();

    void showOperateView();

    void hideOperateView();

    void showEndView();

    void showErrorView();

    void showLoadingView();

    void hideLoadingView();

}