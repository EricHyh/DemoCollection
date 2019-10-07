package com.hyh.video.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.hyh.video.lib.DefaultVideoController;
import com.hyh.video.lib.HappyVideo;
import com.hyh.video.lib.IControllerView;
import com.hyh.video.lib.IMediaInfo;
import com.hyh.video.lib.IVideoController;
import com.hyh.video.lib.VideoDelegate;
import com.hyh.video.lib.VideoUtils;

/**
 * Created by Eric_He on 2019/10/2.
 */

public class SmallVideoView extends HappyVideo {

    public SmallVideoView(Context context) {
        super(context);
    }

    public SmallVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmallVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected VideoDelegate newVideoDelegate(Context context) {
        return new VideoDelegate(context) {
            @Override
            protected IVideoController newVideoController(Context context) {
                SmallVideoControllerView controllerView = new SmallVideoControllerView(context);
                return new DefaultVideoController(context, controllerView);
            }
        };
    }

    public static class SmallVideoControllerView extends RelativeLayout implements IControllerView {

        SmallVideoControllerView(Context context) {
            super(context);
            NiceImageView imageView = new NiceImageView(context);
            imageView.isCircle(true);
            int _36dp = VideoUtils.dp2px(context, 36);
            new LayoutParams(_36dp, _36dp);

        }

        @Override
        public View getView() {
            return null;
        }

        @Override
        public void setup(VideoDelegate videoDelegate, CharSequence title, long playCount, IMediaInfo mediaInfo) {

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
        public void setPlayStyle() {

        }

        @Override
        public void setPauseStyle() {

        }

        @Override
        public void setControllerViewTouchListener(OnTouchListener listener) {

        }

        @Override
        public void setControllerViewClickListener(OnClickListener listener) {

        }

        @Override
        public void setPlayOrPauseClickListener(OnClickListener listener) {

        }

        @Override
        public void setReplayClickListener(OnClickListener listener) {

        }

        @Override
        public void setRetryClickListener(OnClickListener listener) {

        }

        @Override
        public void setFullScreenToggleClickListener(OnClickListener listener) {

        }

        @Override
        public void setMobileDataConfirmClickListener(OnClickListener listener) {

        }

        @Override
        public void setFullscreenBackClickListener(OnClickListener listener) {

        }

        @Override
        public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener) {

        }

        @Override
        public void showInitialView() {

        }

        @Override
        public void hideInitialView() {

        }

        @Override
        public void showMobileDataConfirm() {

        }

        @Override
        public void hideMobileDataConfirm() {

        }

        @Override
        public boolean isShowOperateView() {
            return false;
        }

        @Override
        public void showOperateView(int mode) {

        }

        @Override
        public void showEndView() {

        }

        @Override
        public void hideEndView() {

        }

        @Override
        public void showErrorView() {

        }

        @Override
        public void hideErrorView() {

        }

        @Override
        public void showLoadingView() {

        }

        @Override
        public void showLoadingViewDelayed(long delayMillis) {

        }

        @Override
        public void hideLoadingView() {

        }

        @Override
        public void onVideoSceneChanged(FrameLayout videoContainer, int scene) {

        }

        @Override
        public boolean isFullScreenLocked() {
            return false;
        }

        @Override
        public void showToast(CharSequence text) {

        }
    }
}