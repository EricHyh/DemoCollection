package com.hyh.video.lib;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hyh.video.sample.R;

import java.util.Locale;

/**
 * @author Administrator
 * @description
 * @data 2019/2/25
 */

public class DefaultControllerView extends RelativeLayout implements IControllerView {

    protected final Context mContext;
    protected final View mTopContainer;
    protected final View mFullscreenBackIcon;
    protected final TextView mTitle;
    protected final View mBatteryTimeContainer;
    protected final ImageView mBatteryLevel;
    protected final TextView mSystemTime;
    protected final View mBottomContainer;
    protected final TextView mCurrentPosition;
    protected final SeekBar mSeekBar;
    protected final TextView mDuration;
    protected final TextView mDefinition;
    protected final ImageView mFullscreenToggle;
    protected final View mInitialInfoContainer;
    protected final TextView mInitialInfoPlayTimes;
    protected final TextView mInitialInfoDuration;
    protected final ProgressBar mBottomProgress;
    protected final ImageView mPlayOrPauseIcon;
    protected final FrameLayout mEndViewContainer;
    protected final View mReplayContainer;
    protected final View mRetryContainer;
    protected final View mRetryButton;
    protected final View mMobileDataConfirmContainer;
    protected final View mMobileDataSureButton;
    protected final ProgressBar mLoadingProgress;

    protected HappyVideo mHappyVideo;

    public DefaultControllerView(Context context) {
        super(context);
        long start = System.currentTimeMillis();
        this.mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.view_video_controller, this);
        long end = System.currentTimeMillis();
        Log.d("NativeVideoController", "DefaultControllerView: use time1 = " + (end - start));
        mTopContainer = findViewById(R.id.video_top_container);
        mFullscreenBackIcon = findViewById(R.id.video_fullscreen_back_icon);
        mTitle = findViewById(R.id.video_title);
        mBatteryTimeContainer = findViewById(R.id.video_battery_time_container);
        mBatteryLevel = findViewById(R.id.video_battery_level);
        mSystemTime = findViewById(R.id.video_system_time);
        mBottomContainer = findViewById(R.id.video_bottom_container);
        mCurrentPosition = findViewById(R.id.video_current_position);
        mSeekBar = findViewById(R.id.video_seek_bar);
        mDuration = findViewById(R.id.video_duration);
        mDefinition = findViewById(R.id.video_definition);
        mFullscreenToggle = findViewById(R.id.video_fullscreen_toggle);
        mInitialInfoContainer = findViewById(R.id.video_initial_info_container);
        mInitialInfoPlayTimes = findViewById(R.id.video_initial_info_play_times);
        mInitialInfoDuration = findViewById(R.id.video_initial_info_duration);
        mBottomProgress = findViewById(R.id.video_bottom_progress);
        mPlayOrPauseIcon = findViewById(R.id.video_play_or_pause_icon);
        mEndViewContainer = findViewById(R.id.video_end_view_container);
        mReplayContainer = findViewById(R.id.video_replay_container);
        mRetryContainer = findViewById(R.id.video_retry_container);
        mRetryButton = findViewById(R.id.video_retry_btn);
        mMobileDataConfirmContainer = findViewById(R.id.video_mobile_data_confirm_container);
        mMobileDataSureButton = findViewById(R.id.video_mobile_data_sure_btn);
        mLoadingProgress = findViewById(R.id.video_loading);
        end = System.currentTimeMillis();
        Log.d("NativeVideoController", "DefaultControllerView: use time2 = " + (end - start));
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setup(HappyVideo video, CharSequence title, IMediaInfo mediaInfo) {
        this.mHappyVideo = video;
        mTitle.setText(title);
        mSeekBar.setProgress(0);
        mBottomProgress.setProgress(0);
        mInitialInfoDuration.setVisibility(GONE);
        if (mediaInfo != null) {
            mediaInfo.getDuration(new IMediaInfo.Result<Long>() {
                @Override
                public void onResult(Long duration) {
                    if (duration != null) {
                        mInitialInfoDuration.setVisibility(VISIBLE);
                        mInitialInfoDuration.setText(formatTimeMillis(duration));
                    }
                }
            });
        }
    }

    @Override
    public void setMediaProgress(int progress) {
        mSeekBar.setProgress(progress);
        mBottomProgress.setProgress(progress);
    }

    @Override
    public void setBufferingProgress(int progress) {
        mSeekBar.setSecondaryProgress(progress);
    }

    @Override
    public void setCurrentPosition(long currentPosition) {
        mCurrentPosition.setText(formatTimeMillis(currentPosition));
    }

    @Override
    public void setDuration(long duration) {
        mDuration.setText(formatTimeMillis(duration));
        mInitialInfoDuration.setText(formatTimeMillis(duration));
    }

    @Override
    public void setStartIconPlayStyle() {
        mPlayOrPauseIcon.setImageResource(R.drawable.video_play_selector);
    }

    @Override
    public void setStartIconPauseStyle() {
        mPlayOrPauseIcon.setImageResource(R.drawable.video_pause_selector);
    }

    @Override
    public void setControllerViewClickListener(View.OnClickListener listener) {
        setOnClickListener(listener);
    }

    @Override
    public void setPlayOrPauseIconClickListener(View.OnClickListener listener) {
        mPlayOrPauseIcon.setOnClickListener(listener);
    }

    @Override
    public void setReplayIconClickListener(View.OnClickListener listener) {
        mReplayContainer.setOnClickListener(listener);
    }

    @Override
    public void setRetryButtonClickListener(View.OnClickListener listener) {
        mRetryButton.setOnClickListener(listener);
    }

    @Override
    public void setFullScreenToggleClickListener(View.OnClickListener listener) {
        mFullscreenToggle.setOnClickListener(listener);
    }

    @Override
    public void setMobileDataConfirmButtonClickListener(View.OnClickListener listener) {
        mMobileDataSureButton.setOnClickListener(listener);
    }

    @Override
    public void setBackIconClickListener(View.OnClickListener listener) {
        mFullscreenBackIcon.setOnClickListener(listener);
    }

    @Override
    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener) {
        mSeekBar.setOnSeekBarChangeListener(listener);
    }

    @Override
    public void showInitialView() {
        setBackgroundColor(Color.TRANSPARENT);
        mPlayOrPauseIcon.setImageResource(R.drawable.video_play_selector);

        setVisibility(mTopContainer, VISIBLE);
        setVisibility(mInitialInfoContainer, VISIBLE);
        setVisibility(mPlayOrPauseIcon, VISIBLE);

        setVisibility(mBottomProgress, GONE);
        setVisibility(mBottomContainer, GONE);
        setVisibility(mMobileDataConfirmContainer, GONE);
        setVisibility(mRetryContainer, GONE);
        setVisibility(mEndViewContainer, GONE);
    }

    @Override
    public void hideInitialView() {
        setVisibility(mTopContainer, GONE);
        setVisibility(mInitialInfoContainer, GONE);
        setVisibility(mPlayOrPauseIcon, GONE);
    }

    @Override
    public void showMobileDataConfirm() {
        setBackgroundColor(Color.BLACK);

        setVisibility(mMobileDataConfirmContainer, VISIBLE);

        setVisibility(mTopContainer, GONE);
        setVisibility(mInitialInfoContainer, GONE);
        setVisibility(mPlayOrPauseIcon, GONE);

        setVisibility(mBottomProgress, GONE);
        setVisibility(mBottomContainer, GONE);
        setVisibility(mRetryContainer, GONE);
        setVisibility(mEndViewContainer, GONE);
    }

    @Override
    public void hideMobileDataConfirm() {
        setBackgroundColor(Color.TRANSPARENT);
        setVisibility(mMobileDataConfirmContainer, GONE);
    }

    @Override
    public boolean isShowOperateView() {
        return mPlayOrPauseIcon.getVisibility() == VISIBLE;
    }

    @Override
    public void showOperateView() {
        setBackgroundColor(Color.TRANSPARENT);
        setVisibility(mTopContainer, VISIBLE);
        setVisibility(mBottomContainer, VISIBLE);
        setVisibility(mPlayOrPauseIcon, VISIBLE);

        setVisibility(mBottomProgress, GONE);
        setVisibility(mInitialInfoContainer, GONE);
    }

    @Override
    public void hideOperateView() {
        setVisibility(mTopContainer, GONE);
        setVisibility(mBottomContainer, GONE);
        setVisibility(mPlayOrPauseIcon, GONE);

        setVisibility(mBottomProgress, VISIBLE);
    }

    @Override
    public void showEndView() {
        setVisibility(mEndViewContainer, VISIBLE);

        setVisibility(mTopContainer, GONE);
        setVisibility(mInitialInfoContainer, GONE);
        setVisibility(mPlayOrPauseIcon, GONE);
        setVisibility(mBottomContainer, GONE);
        setVisibility(mBottomProgress, GONE);
        setVisibility(mRetryContainer, GONE);
    }

    @Override
    public void hideEndView() {
        setVisibility(mEndViewContainer, GONE);
    }

    @Override
    public void showErrorView() {
        setBackgroundColor(Color.TRANSPARENT);

        setVisibility(mRetryContainer, VISIBLE);

        setVisibility(mTopContainer, GONE);
        setVisibility(mInitialInfoContainer, GONE);
        setVisibility(mPlayOrPauseIcon, GONE);

        setVisibility(mBottomProgress, GONE);
        setVisibility(mBottomContainer, GONE);
        setVisibility(mEndViewContainer, GONE);
    }

    @Override
    public void hideErrorView() {
        setVisibility(mRetryContainer, GONE);
    }

    @Override
    public void showLoadingView() {
        setVisibility(mLoadingProgress, VISIBLE);
    }

    @Override
    public void hideLoadingView() {
        setVisibility(mLoadingProgress, GONE);
    }


    protected void setVisibility(View view, int visibility) {
        if (view.getVisibility() == visibility) return;
        view.setVisibility(visibility);
    }

    //00:00:00
    private String formatTimeMillis(long timeMills) {
        long seconds = timeMills / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        long hours = minutes / 60;
        minutes = minutes % 60;
        if (hours <= 0) {
            String[] minutes_split = split(minutes);
            String[] seconds_split = split(seconds);
            return String.format(Locale.getDefault(), "%s%s:%s%s",
                    minutes_split[0], minutes_split[1],
                    seconds_split[0], seconds_split[1]);
        } else {
            String[] hours_split = split(hours);
            String[] minutes_split = split(minutes);
            String[] seconds_split = split(seconds);
            return String.format(Locale.getDefault(), "%s%s:%s%s:%s%s",
                    hours_split[0], hours_split[1],
                    minutes_split[0], minutes_split[1],
                    seconds_split[0], seconds_split[1]);
        }
    }

    private String[] split(long num) {
        String[] num_split = new String[2];
        String minutes_str = String.valueOf(num);
        if (minutes_str.length() == 1) {
            num_split[0] = "0";
            num_split[1] = minutes_str;
        } else if (minutes_str.length() >= 2) {
            num_split[0] = String.valueOf(minutes_str.charAt(0));
            num_split[1] = String.valueOf(minutes_str.charAt(1));
        } else {
            num_split[0] = "0";
            num_split[1] = "0";
        }
        return num_split;
    }
}