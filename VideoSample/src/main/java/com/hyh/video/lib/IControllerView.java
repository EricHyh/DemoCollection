package com.hyh.video.lib;

import android.view.View;
import android.widget.SeekBar;

/**
 * @author Administrator
 * @description
 * @data 2019/2/25
 */

public interface IControllerView {

    View getView();

    void setup(CharSequence title, IMediaInfo mediaInfo);

    void setMediaProgress(int progress);

    void setBufferingProgress(int progress);

    void setCurrentPosition(long currentPosition);

    void setDuration(long duration);

    void setStartIconPlayStyle();

    void setStartIconPauseStyle();

    void setControllerViewClickListener(View.OnClickListener listener);

    void setStartIconClickListener(View.OnClickListener listener);

    void setReplayIconClickListener(View.OnClickListener listener);

    void setRetryButtonClickListener(View.OnClickListener listener);

    void setFullScreenToggleClickListener(View.OnClickListener listener);

    void setMobileDataConfirmButtonClickListener(View.OnClickListener listener);

    void setBackIconClickListener(View.OnClickListener listener);

    void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener);

    void showInitialView();

    void hideInitialView();

    void showMobileDataConfirm();

    void hideMobileDataConfirm();

    boolean isShowOperateView();

    void showOperateView();

    void hideOperateView();

    void showEndView();

    void hideEndView();

    void showErrorView();

    void hideErrorView();

    void showLoadingView();

    void hideLoadingView();

}