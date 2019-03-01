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

    void setCurrentPosition(int currentPosition);

    void setDuration(int duration);

    void setStarIconStartStyle();

    void setStartIconPauseStyle();

    void setInitialViewClickListener(View.OnClickListener listener);

    void setControllerViewClickListener(View.OnClickListener listener);

    void setStartIconClickListener(View.OnClickListener listener);

    void setReplayIconClickListener(View.OnClickListener listener);

    void setRetryIconClickListener(View.OnClickListener listener);

    void setFullScreenToggleClickListener(View.OnClickListener listener);

    void setMobileDataConfirmIconClickListener(View.OnClickListener listener);

    void setBackIconClickListener(View.OnClickListener listener);

    void showInitialView(DataSource source);

    void showControllerView();

    void hideControllerView();

    void showLoadingView();

    void hideLoadingView();

    void showErrorView();

    void showEndView();

    void showMobileDataConfirm();

    void onFullScreenOpen();

    void onFullScreenClose();

}