package com.hyh.video.lib;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
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

    private final Context mContext;
    private final View mTopContainer;
    private final View mFullscreenBackIcon;
    private final TextView mTitle;
    private final View mBatteryTimeContainer;
    private final ImageView mBatteryLevel;
    private final TextView mSystemTime;
    private final View mBottomContainer;
    private final TextView mCurrentPosition;
    private final SeekBar mSeekBar;
    private final TextView mDuration;
    private final TextView mDefinition;
    private final ImageView mFullscreenToggle;
    private final View mInitialInfoContainer;
    private final TextView mInitialInfoPlayTimes;
    private final TextView mInitialInfoDuration;
    private final ProgressBar mBottomProgress;
    private final ImageView mPlayOrPauseIcon;
    private final View mReplayContainer;
    private final View mRetryContainer;
    private final View mRetryButton;
    private final View mMobileDataConfirmContainer;
    private final View mMobileDataSureButton;
    private final ProgressBar mLoadingProgress;

    public DefaultControllerView(Context context) {
        super(context);
        this.mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.view_video_controller, this);
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
        mReplayContainer = findViewById(R.id.video_replay_container);
        mRetryContainer = findViewById(R.id.video_retry_container);
        mRetryButton = findViewById(R.id.video_retry_btn);
        mMobileDataConfirmContainer = findViewById(R.id.video_mobile_data_confirm_container);
        mMobileDataSureButton = findViewById(R.id.video_mobile_data_sure_btn);
        mLoadingProgress = findViewById(R.id.video_loading);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setup(CharSequence title, IMediaInfo mediaInfo) {
        mTitle.setText(title);
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
    public void setStartIconClickListener(View.OnClickListener listener) {
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
        mTopContainer.setVisibility(VISIBLE);
        mInitialInfoContainer.setVisibility(VISIBLE);
        mPlayOrPauseIcon.setVisibility(VISIBLE);
        mPlayOrPauseIcon.setImageResource(R.drawable.video_play_selector);

        mBottomProgress.setVisibility(GONE);
        mBottomContainer.setVisibility(GONE);
        mMobileDataConfirmContainer.setVisibility(GONE);
        mRetryContainer.setVisibility(GONE);
        mReplayContainer.setVisibility(GONE);
    }

    @Override
    public void hideInitialView() {
        mTopContainer.setVisibility(GONE);
        mInitialInfoContainer.setVisibility(GONE);
        mPlayOrPauseIcon.setVisibility(GONE);
    }

    @Override
    public void showMobileDataConfirm() {
        setBackgroundColor(Color.BLACK);
        mMobileDataConfirmContainer.setVisibility(VISIBLE);

        mTopContainer.setVisibility(GONE);
        mInitialInfoContainer.setVisibility(GONE);
        mPlayOrPauseIcon.setVisibility(GONE);

        mBottomProgress.setVisibility(GONE);
        mBottomContainer.setVisibility(GONE);
        mRetryContainer.setVisibility(GONE);
        mReplayContainer.setVisibility(GONE);
    }

    @Override
    public void hideMobileDataConfirm() {
        setBackgroundColor(Color.TRANSPARENT);
        mMobileDataConfirmContainer.setVisibility(GONE);
    }

    @Override
    public boolean isShowOperateView() {
        return mPlayOrPauseIcon.getVisibility() == VISIBLE;
    }

    @Override
    public void showOperateView() {
        setBackgroundColor(Color.TRANSPARENT);
        mTopContainer.setVisibility(VISIBLE);
        mBottomContainer.setVisibility(VISIBLE);
        mPlayOrPauseIcon.setVisibility(VISIBLE);

        mBottomProgress.setVisibility(GONE);
    }

    @Override
    public void hideOperateView() {
        mTopContainer.setVisibility(GONE);
        mBottomContainer.setVisibility(GONE);
        mPlayOrPauseIcon.setVisibility(GONE);

        mBottomProgress.setVisibility(VISIBLE);
    }

    @Override
    public void showEndView() {
        setBackgroundColor(0x55000000);
        mReplayContainer.setVisibility(VISIBLE);

        mTopContainer.setVisibility(GONE);
        mInitialInfoContainer.setVisibility(GONE);
        mPlayOrPauseIcon.setVisibility(GONE);
        mBottomContainer.setVisibility(GONE);

        mBottomProgress.setVisibility(GONE);

        mRetryContainer.setVisibility(GONE);
    }

    @Override
    public void hideEndView() {
        setBackgroundColor(Color.TRANSPARENT);
        mReplayContainer.setVisibility(GONE);
    }

    @Override
    public void showErrorView() {
        setBackgroundColor(Color.TRANSPARENT);
        mRetryContainer.setVisibility(VISIBLE);

        mTopContainer.setVisibility(GONE);
        mInitialInfoContainer.setVisibility(GONE);
        mPlayOrPauseIcon.setVisibility(GONE);

        mBottomProgress.setVisibility(GONE);
        mBottomContainer.setVisibility(GONE);
        mReplayContainer.setVisibility(GONE);
    }

    @Override
    public void hideErrorView() {
        mRetryContainer.setVisibility(GONE);
    }

    @Override
    public void showLoadingView() {
        mLoadingProgress.setVisibility(VISIBLE);
    }

    @Override
    public void hideLoadingView() {
        mLoadingProgress.setVisibility(GONE);
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