package com.hyh.video.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hyh.video.sample.R;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Locale;

/**
 * @author Administrator
 * @description
 * @data 2019/2/25
 */

public class DefaultControllerView extends RelativeLayout implements IControllerView, View.OnClickListener {

    private static final String TAG = "DefaultControllerView";

    public final ControllerListenerInfo mListenerInfo = new ControllerListenerInfo();
    public final ShowLoadingViewTask mShowLoadingViewTask = new ShowLoadingViewTask(this);
    public final LazyView<TopContainer> mTopContainer;
    public final LazyView<BottomContainer> mBottomContainer;
    public final LazyView<InitialInfoContainer> mInitialInfoContainer;
    public final LazyView<ProgressBar> mBottomProgress;
    public final LazyView<ImageView> mPlayOrPauseIcon;
    public final LazyView<EndViewContainer> mEndViewContainer;
    public final LazyView<ErrorViewContainer> mErrorViewContainer;
    public final LazyView<MobileDataConfirmContainer> mMobileDataConfirmContainer;
    public final LazyView<ProgressBar> mLoadingProgress;
    public final LazyView<CheckBox> mLockCheckBox;
    public final LazyView<ToastView> mToastView;

    public VideoDelegate mVideoDelegate;

    public DefaultControllerView(final Context context) {
        super(context);
        setOnClickListener(this);
        {
            mTopContainer = new LazyView<TopContainer>() {
                @Override
                public TopContainer create() {
                    return new TopContainer(context);
                }
            };
            int _48dp = VideoUtils.dp2px(context, 48);
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, _48dp);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            mTopContainer.addToParent(this, params);
        }
        {
            mBottomContainer = new LazyView<BottomContainer>() {
                @Override
                public BottomContainer create() {
                    return new BottomContainer(context);
                }
            };
            int _48dp = VideoUtils.dp2px(context, 48);
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, _48dp);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mBottomContainer.addToParent(this, params);
        }
        {
            mInitialInfoContainer = new LazyView<InitialInfoContainer>() {
                @Override
                public InitialInfoContainer create() {
                    return new InitialInfoContainer(context);
                }
            };
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.bottomMargin = VideoUtils.dp2px(context, 10);
            mInitialInfoContainer.addToParent(this, params);
        }
        {
            mBottomProgress = new LazyView<ProgressBar>() {
                @Override
                public ProgressBar create() {
                    final ProgressBar progressBar = new ProgressBar(context);
                    progressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.video_progress_drawable));
                    VideoUtils.setProgressBarOnlyIndeterminate(progressBar, false);
                    progressBar.setIndeterminate(false);
                    progressBar.setMax(100);
                    progressBar.setProgress(1);
                    progressBar.setSecondaryProgress(1);
                    return progressBar;
                }
            };
            int _1_5dp = VideoUtils.dp2px(context, 1.5f);
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, _1_5dp);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mBottomProgress.addToParent(this, params);
        }
        {
            mPlayOrPauseIcon = new LazyView<ImageView>() {
                @Override
                public ImageView create() {
                    ImageView imageView = new ImageView(context);
                    imageView.setImageResource(R.drawable.video_play_selector);
                    imageView.setOnClickListener(DefaultControllerView.this);
                    return imageView;
                }
            };
            int _45dp = VideoUtils.dp2px(context, 45);
            LayoutParams params = new LayoutParams(_45dp, _45dp);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            mPlayOrPauseIcon.addToParent(this, params);
        }
        {
            mEndViewContainer = new LazyView<EndViewContainer>() {
                @Override
                public EndViewContainer create() {
                    return new EndViewContainer(context);
                }
            };
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            mEndViewContainer.addToParent(this, params);
        }
        {
            mErrorViewContainer = new LazyView<ErrorViewContainer>() {
                @Override
                public ErrorViewContainer create() {
                    return new ErrorViewContainer(context);
                }
            };
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            mErrorViewContainer.addToParent(this, params);
        }
        {
            mMobileDataConfirmContainer = new LazyView<MobileDataConfirmContainer>() {
                @Override
                public MobileDataConfirmContainer create() {
                    return new MobileDataConfirmContainer(context);
                }
            };
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            mMobileDataConfirmContainer.addToParent(this, params);
        }
        {
            mLoadingProgress = new LazyView<ProgressBar>() {
                @Override
                public ProgressBar create() {
                    ProgressBar progressBar = new ProgressBar(context);
                    progressBar.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.video_loading));
                    return progressBar;
                }
            };
            int _45dp = VideoUtils.dp2px(context, 45);
            LayoutParams params = new LayoutParams(_45dp, _45dp);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            mLoadingProgress.addToParent(this, params);
        }
        {
            mLockCheckBox = new LazyView<CheckBox>() {
                @Override
                public CheckBox create() {
                    CheckBox checkBox = new CheckBox(context);
                    checkBox.setButtonDrawable(R.drawable.video_lock_selector);
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isShowOperateView()) {
                                showOperateView(OperateMode.ALIVE);
                            }
                        }
                    });
                    return checkBox;
                }
            };
            int _45dp = VideoUtils.dp2px(context, 45);
            LayoutParams params = new LayoutParams(_45dp, _45dp);
            params.leftMargin = VideoUtils.dp2px(context, 30);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            mLockCheckBox.addToParent(this, params);
        }
        {
            mToastView = new LazyView<ToastView>() {
                @Override
                public ToastView create() {
                    return new ToastView(context);
                }
            };
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = VideoUtils.dp2px(context, 25);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            mToastView.addToParent(this, params);
        }
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setup(VideoDelegate videoDelegate, final CharSequence title, IMediaInfo mediaInfo) {
        this.mVideoDelegate = videoDelegate;
        setVisibility(mLoadingProgress, View.GONE);
        mTopContainer.saveLazyAction("setTitle", new LazyView.LazyAction<TopContainer>() {
            @Override
            public void doAction(TopContainer topContainer) {
                topContainer.title.setText(title);
            }
        });
        mBottomContainer.saveLazyAction("reset", new LazyView.LazyAction<BottomContainer>() {
            @Override
            public void doAction(BottomContainer bottomContainer) {
                bottomContainer.seekBar.setProgress(0);
                bottomContainer.seekBar.setSecondaryProgress(0);
                bottomContainer.currentPosition.setText("00:00");
                bottomContainer.duration.setText("00:00");
            }
        });
        mBottomProgress.saveLazyAction("setProgress", new LazyView.LazyAction<ProgressBar>() {
            @Override
            public void doAction(final ProgressBar progressBar) {
                Log.d(TAG, "doAction: mBottomProgress reset");
                progressBar.setProgress(0);
                progressBar.setSecondaryProgress(0);
            }
        }, true);
        mInitialInfoContainer.saveLazyAction("reset", new LazyView.LazyAction<InitialInfoContainer>() {
            @Override
            public void doAction(InitialInfoContainer initialInfoContainer) {
                initialInfoContainer.playTimes.setVisibility(GONE);
                initialInfoContainer.duration.setVisibility(GONE);
            }
        });
        if (mediaInfo != null) {
            mediaInfo.getDuration(new IMediaInfo.Result<Long>() {
                @Override
                public void onResult(final Long duration) {
                    if (duration != null) {
                        mInitialInfoContainer.saveLazyAction("setDuration", new LazyView.LazyAction<InitialInfoContainer>() {
                            @Override
                            public void doAction(InitialInfoContainer initialInfoContainer) {
                                initialInfoContainer.duration.setVisibility(VISIBLE);
                                initialInfoContainer.duration.setText(formatTimeMillis(duration));
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void setMediaProgress(final int progress) {
        Log.d(TAG, "setMediaProgress: progress = " + progress);
        if (mBottomContainer.isCreated()) {
            mBottomContainer.get().seekBar.setProgress(progress);
        } else {
            mBottomContainer.saveLazyAction("setProgress", new LazyView.LazyAction<BottomContainer>() {
                @Override
                public void doAction(BottomContainer bottomContainer) {
                    bottomContainer.seekBar.setProgress(progress);
                }
            }, true);
        }
        if (mBottomProgress.isCreated()) {
            mBottomProgress.get().setProgress(progress);
        } else {
            mBottomProgress.saveLazyAction("setProgress", new LazyView.LazyAction<ProgressBar>() {
                @Override
                public void doAction(ProgressBar progressBar) {
                    progressBar.setProgress(progress);
                }
            }, true);
        }
    }

    @Override
    public void setBufferingProgress(final int progress) {
        if (mBottomContainer.isCreated()) {
            mBottomContainer.get().seekBar.setSecondaryProgress(progress);
        } else {
            mBottomContainer.saveLazyAction("setSecondaryProgress", new LazyView.LazyAction<BottomContainer>() {
                @Override
                public void doAction(BottomContainer bottomContainer) {
                    bottomContainer.seekBar.setSecondaryProgress(progress);
                }
            }, true);
        }
        if (mBottomProgress.isCreated()) {
            mBottomProgress.get().setSecondaryProgress(progress);
        } else {
            mBottomProgress.saveLazyAction("setSecondaryProgress", new LazyView.LazyAction<ProgressBar>() {
                @Override
                public void doAction(ProgressBar progressBar) {
                    progressBar.setSecondaryProgress(progress);
                }
            }, true);
        }
    }

    @Override
    public void setCurrentPosition(final long currentPosition) {
        if (mBottomContainer.isCreated()) {
            mBottomContainer.get().currentPosition.setText(formatTimeMillis(currentPosition));
        } else {
            mBottomContainer.saveLazyAction("setCurrentPosition", new LazyView.LazyAction<BottomContainer>() {
                @Override
                public void doAction(BottomContainer bottomContainer) {
                    bottomContainer.currentPosition.setText(formatTimeMillis(currentPosition));
                }
            }, true);
        }
    }

    @Override
    public void setDuration(final long duration) {
        mInitialInfoContainer.saveLazyAction("setDuration", new LazyView.LazyAction<InitialInfoContainer>() {
            @Override
            public void doAction(InitialInfoContainer initialInfoContainer) {
                initialInfoContainer.duration.setText(formatTimeMillis(duration));
            }
        }, true);
        mBottomContainer.saveLazyAction("setDuration", new LazyView.LazyAction<BottomContainer>() {
            @Override
            public void doAction(BottomContainer bottomContainer) {
                bottomContainer.duration.setText(formatTimeMillis(duration));
            }
        }, true);
    }

    @Override
    public void setPlayStyle() {
        if (mPlayOrPauseIcon.isCreated()) {
            mPlayOrPauseIcon.get().setImageResource(R.drawable.video_play_selector);
        } else {
            mPlayOrPauseIcon.saveLazyAction("setImageResource", new LazyView.LazyAction<ImageView>() {
                @Override
                public void doAction(ImageView imageView) {
                    imageView.setImageResource(R.drawable.video_play_selector);
                }
            }, true);
        }
    }

    @Override
    public void setPauseStyle() {
        if (mPlayOrPauseIcon.isCreated()) {
            mPlayOrPauseIcon.get().setImageResource(R.drawable.video_pause_selector);
        } else {
            mPlayOrPauseIcon.saveLazyAction("setImageResource", new LazyView.LazyAction<ImageView>() {
                @Override
                public void doAction(ImageView imageView) {
                    imageView.setImageResource(R.drawable.video_pause_selector);
                }
            }, true);
        }
    }

    @Override
    public void setControllerViewTouchListener(View.OnTouchListener listener) {
        mListenerInfo.mControllerViewTouchListener = listener;
    }

    @Override
    public void setControllerViewClickListener(View.OnClickListener listener) {
        mListenerInfo.mControllerViewClickListener = listener;
    }

    @Override
    public void setPlayOrPauseClickListener(final View.OnClickListener listener) {
        mListenerInfo.mPlayOrPauseClickListener = listener;
    }

    @Override
    public void setReplayClickListener(final View.OnClickListener listener) {
        mListenerInfo.mReplayClickListener = listener;
    }

    @Override
    public void setRetryClickListener(final View.OnClickListener listener) {
        mListenerInfo.mRetryClickListener = listener;
    }

    @Override
    public void setFullScreenToggleClickListener(final View.OnClickListener listener) {
        mListenerInfo.mFullScreenToggleClickListener = listener;
    }

    @Override
    public void setMobileDataConfirmClickListener(final View.OnClickListener listener) {
        mListenerInfo.mMobileDataConfirmClickListener = listener;
    }

    @Override
    public void setFullscreenBackClickListener(final View.OnClickListener listener) {
        mListenerInfo.mFullscreenBackClickListener = listener;
    }

    @Override
    public void setOnSeekBarChangeListener(final SeekBar.OnSeekBarChangeListener listener) {
        mListenerInfo.mOnSeekBarChangeListener = listener;
    }

    @Override
    public void showInitialView() {
        setBackgroundColor(Color.TRANSPARENT);

        setVisibility(mTopContainer, VISIBLE);
        setVisibility(mInitialInfoContainer, VISIBLE);
        setVisibility(mPlayOrPauseIcon, VISIBLE);
        mPlayOrPauseIcon.get().setImageResource(R.drawable.video_play_selector);

        setVisibility(mBottomContainer, GONE);
        setVisibility(mBottomProgress, GONE);
        setVisibility(mMobileDataConfirmContainer, GONE);
        setVisibility(mErrorViewContainer, GONE);
        setVisibility(mEndViewContainer, GONE);
        setVisibility(mLockCheckBox, GONE);
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
        setVisibility(mLockCheckBox, GONE);

        setVisibility(mBottomProgress, GONE);
        setVisibility(mBottomContainer, GONE);
        setVisibility(mErrorViewContainer, GONE);
        setVisibility(mEndViewContainer, GONE);
    }

    @Override
    public void hideMobileDataConfirm() {
        setBackgroundColor(Color.TRANSPARENT);
        setVisibility(mMobileDataConfirmContainer, GONE);
    }

    @Override
    public boolean isShowOperateView() {
        return (mTopContainer.getVisibility() == VISIBLE && mBottomContainer.getVisibility() == VISIBLE)
                || mLockCheckBox.getVisibility() == VISIBLE;
    }

    @Override
    public void showOperateView(int mode) {
        if (mode == OperateMode.IDLE) {
            setVisibility(mTopContainer, GONE);
            setVisibility(mBottomContainer, GONE);
            setVisibility(mPlayOrPauseIcon, GONE);
            setVisibility(mLockCheckBox, GONE);

            setVisibility(mBottomProgress, VISIBLE);
        } else {
            setBackgroundColor(Color.TRANSPARENT);
            boolean videoLandingSpace = isVideoLandingSpace();
            if (videoLandingSpace && isFullScreenLocked()) {
                setVisibility(mTopContainer, GONE);
                setVisibility(mBottomContainer, GONE);
                setVisibility(mPlayOrPauseIcon, GONE);
            } else {
                setVisibility(mTopContainer, VISIBLE);
                setVisibility(mBottomContainer, VISIBLE);
                setVisibility(mPlayOrPauseIcon, VISIBLE);
            }

            if (videoLandingSpace) {
                setVisibility(mLockCheckBox, VISIBLE);
            }

            setVisibility(mBottomProgress, GONE);
            setVisibility(mInitialInfoContainer, GONE);
        }
    }

    @Override
    public void showEndView() {
        setVisibility(mEndViewContainer, VISIBLE);

        setVisibility(mTopContainer, GONE);
        setVisibility(mInitialInfoContainer, GONE);
        setVisibility(mPlayOrPauseIcon, GONE);
        setVisibility(mBottomContainer, GONE);
        setVisibility(mBottomProgress, GONE);
        setVisibility(mErrorViewContainer, GONE);
        setVisibility(mLockCheckBox, GONE);
    }

    @Override
    public void hideEndView() {
        setVisibility(mEndViewContainer, GONE);
    }

    @Override
    public void showErrorView() {
        setBackgroundColor(Color.TRANSPARENT);

        setVisibility(mErrorViewContainer, VISIBLE);

        setVisibility(mTopContainer, GONE);
        setVisibility(mInitialInfoContainer, GONE);
        setVisibility(mPlayOrPauseIcon, GONE);
        setVisibility(mLockCheckBox, GONE);

        setVisibility(mBottomProgress, GONE);
        setVisibility(mBottomContainer, GONE);
        setVisibility(mEndViewContainer, GONE);
    }

    @Override
    public void hideErrorView() {
        setVisibility(mErrorViewContainer, GONE);
    }


    @Override
    public void showLoadingView() {
        mShowLoadingViewTask.remove();
        setVisibility(mLoadingProgress, VISIBLE);
    }

    @Override
    public void showLoadingViewDelayed(long delayMillis) {
        mShowLoadingViewTask.post(delayMillis);
    }

    @Override
    public void hideLoadingView() {
        mShowLoadingViewTask.remove();
        setVisibility(mLoadingProgress, GONE);
    }

    @Override
    public void onVideoSceneChanged(final FrameLayout videoContainer, final int scene) {
        if (mTopContainer.getVisibility() == View.VISIBLE) {
            if (scene == VideoDelegate.Scene.FULLSCREEN) {
                mLockCheckBox.setVisibility(VISIBLE);
            } else {
                mLockCheckBox.setVisibility(GONE);
            }
        }
        mTopContainer.saveLazyAction("onVideoSceneChanged", new LazyView.LazyAction<TopContainer>() {
            @Override
            public void doAction(TopContainer topContainer) {
                topContainer.onVideoSceneChanged(videoContainer, scene);
            }
        });
        mBottomContainer.saveLazyAction("onVideoSceneChanged", new LazyView.LazyAction<BottomContainer>() {
            @Override
            public void doAction(BottomContainer bottomContainer) {
                bottomContainer.onVideoSceneChanged(videoContainer, scene);
            }
        });
    }

    @Override
    public boolean isFullScreenLocked() {
        return mLockCheckBox.isCreated() && mLockCheckBox.get().isChecked();
    }

    @Override
    public void showToast(CharSequence text) {
        mToastView.get().showToast(text);
    }

    public void setVisibility(LazyView lazyView, int visibility) {
        lazyView.setVisibility(visibility);
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mListenerInfo.mControllerViewTouchListener != null) {
            mListenerInfo.mControllerViewTouchListener.onTouch(this, ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View v) {
        if (v == this) {
            if (mListenerInfo.mControllerViewClickListener != null) {
                mListenerInfo.mControllerViewClickListener.onClick(v);
            }
        } else if (mPlayOrPauseIcon.isCreated() && v == mPlayOrPauseIcon.get()) {
            if (mListenerInfo.mPlayOrPauseClickListener != null) {
                mListenerInfo.mPlayOrPauseClickListener.onClick(v);
            }
        } else if (mEndViewContainer.isCreated() && v == mEndViewContainer.get().replayContainer) {
            if (mListenerInfo.mReplayClickListener != null) {
                mListenerInfo.mReplayClickListener.onClick(v);
            }
        } else if (mErrorViewContainer.isCreated() && v == mErrorViewContainer.get().retryButton) {
            if (mListenerInfo.mRetryClickListener != null) {
                mListenerInfo.mRetryClickListener.onClick(v);
            }
        } else if (mBottomContainer.isCreated() && v == mBottomContainer.get().fullscreenToggle) {
            if (mListenerInfo.mFullScreenToggleClickListener != null) {
                mListenerInfo.mFullScreenToggleClickListener.onClick(v);
            }
        } else if (mMobileDataConfirmContainer.isCreated() && v == mMobileDataConfirmContainer.get().mobileDataSureButton) {
            if (mListenerInfo.mMobileDataConfirmClickListener != null) {
                mListenerInfo.mMobileDataConfirmClickListener.onClick(v);
            }
        } else if (mTopContainer.isCreated() && v == mTopContainer.get().fullscreenBackIcon) {
            if (mListenerInfo.mFullscreenBackClickListener != null) {
                mListenerInfo.mFullscreenBackClickListener.onClick(v);
            }
        }
    }

    private boolean isVideoLandingSpace() {
        int screenOrientation = VideoUtils.getScreenOrientation(getContext());
        if (screenOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                || screenOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            return true;
        } else {
            ViewParent parent = DefaultControllerView.this.getParent();
            if (parent != null && parent instanceof ViewGroup) {
                float rotation = ((ViewGroup) parent).getRotation();
                if (rotation == 90 || rotation == 270) {
                    return true;
                }
            }
        }
        return false;
    }

    public class TopContainer extends LinearLayout {

        public final ImageView fullscreenBackIcon;
        public final TextView title;
        public final LinearLayout batteryTimeContainer;
        public final ImageView batteryLevel;
        public final TextView systemTime;

        @SuppressLint("RtlHardcoded")
        public TopContainer(Context context) {
            super(context);
            setOrientation(HORIZONTAL);
            int _14dp = VideoUtils.dp2px(getContext(), 14);
            setPadding(_14dp, 0, _14dp, 0);
            setBackgroundResource(R.drawable.video_top_container_bg);
            {
                fullscreenBackIcon = new ImageView(context);
                fullscreenBackIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                int _12dp = VideoUtils.dp2px(context, 12);
                fullscreenBackIcon.setPadding(0, _12dp, 0, _12dp);
                fullscreenBackIcon.setImageResource(R.drawable.video_full_back_selector);
                fullscreenBackIcon.setVisibility(GONE);
                fullscreenBackIcon.setOnClickListener(DefaultControllerView.this);

                int _20dp = VideoUtils.dp2px(context, 20);
                LayoutParams params = new LayoutParams(_20dp, LayoutParams.MATCH_PARENT);
                params.gravity = Gravity.CENTER_VERTICAL;
                params.rightMargin = VideoUtils.dp2px(context, 14);
                addView(fullscreenBackIcon, params);
            }
            {
                title = new TextView(context);
                title.setTextSize(16);
                title.setTextColor(0xFFDEDEDE);
                title.setMaxLines(2);
                title.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    title.setTextAlignment(TEXT_ALIGNMENT_GRAVITY);
                }
                title.setEllipsize(TextUtils.TruncateAt.END);

                LayoutParams params = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
                params.weight = 1;
                addView(title, params);
            }
            {
                batteryTimeContainer = new LinearLayout(context);
                batteryTimeContainer.setOrientation(VERTICAL);
                batteryTimeContainer.setVisibility(GONE);

                LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_VERTICAL;
                layoutParams.leftMargin = VideoUtils.dp2px(context, 14);
                addView(batteryTimeContainer, layoutParams);
                {
                    batteryLevel = new ImageView(context);
                    int _24dp = VideoUtils.dp2px(context, 24);
                    int _10dp = VideoUtils.dp2px(context, 10);
                    LayoutParams params = new LayoutParams(_24dp, _10dp);
                    params.gravity = Gravity.CENTER_HORIZONTAL;
                    batteryTimeContainer.addView(batteryLevel, params);
                }
                {
                    systemTime = new TextView(context);
                    systemTime.setMaxLines(1);
                    systemTime.setGravity(Gravity.CENTER_VERTICAL);
                    systemTime.setTextSize(12);
                    systemTime.setTextColor(0xFFDEDEDE);

                    LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.CENTER_HORIZONTAL;
                    batteryTimeContainer.addView(systemTime, params);
                }
            }
        }

        public void onVideoSceneChanged(FrameLayout videoContainer, int scene) {
            if (scene == VideoDelegate.Scene.FULLSCREEN) {
                fullscreenBackIcon.setVisibility(VISIBLE);
            } else {
                fullscreenBackIcon.setVisibility(GONE);
            }
            if (isVideoLandingSpace()) {
                int _30dp = VideoUtils.dp2px(getContext(), 30);
                setPadding(_30dp, 0, _30dp, 0);
            } else {
                int _14dp = VideoUtils.dp2px(getContext(), 14);
                setPadding(_14dp, 0, _14dp, 0);
            }
        }
    }

    @SuppressWarnings("AppCompatCustomView")
    public class BottomContainer extends LinearLayout {

        public final TextView currentPosition;
        public final SeekBar seekBar;
        public final TextView duration;
        public final TextView definition;
        public final ImageView fullscreenToggle;

        public BottomContainer(Context context) {
            super(context);
            setOrientation(HORIZONTAL);
            setGravity(Gravity.BOTTOM);
            int _14dp = VideoUtils.dp2px(getContext(), 14);
            setPadding(_14dp, 0, _14dp, 0);
            setBackgroundResource(R.drawable.video_bottom_container_bg);

            {
                currentPosition = new TextView(context);
                currentPosition.setTextColor(0xFFDEDEDE);
                currentPosition.setTextSize(12);
                currentPosition.setGravity(Gravity.CENTER_VERTICAL);
                currentPosition.setText("00:00");

                LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_VERTICAL;
                addView(currentPosition, params);
            }
            {
                seekBar = new SeekBar(context);
                seekBar.setMax(100);
                seekBar.setProgress(0);
                seekBar.setSecondaryProgress(0);
                seekBar.setBackgroundColor(Color.TRANSPARENT);
                seekBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.video_seek_progress));
                seekBar.setThumb(context.getResources().getDrawable(R.drawable.video_seek_thumb));
                int _1dp = VideoUtils.dp2px(context, 1);
                seekBar.setMinimumHeight(_1dp);

                int _8dp = VideoUtils.dp2px(context, 8);
                int _12dp = VideoUtils.dp2px(context, 12);
                seekBar.setPadding(_12dp, _8dp, _12dp, _8dp);

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (mListenerInfo.mOnSeekBarChangeListener != null) {
                            mListenerInfo.mOnSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        if (mListenerInfo.mOnSeekBarChangeListener != null) {
                            mListenerInfo.mOnSeekBarChangeListener.onStartTrackingTouch(seekBar);
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        if (mListenerInfo.mOnSeekBarChangeListener != null) {
                            mListenerInfo.mOnSeekBarChangeListener.onStopTrackingTouch(seekBar);
                        }
                    }
                });

                try {
                    Field mMaxHeightField = ProgressBar.class.getDeclaredField("mMaxHeight");
                    mMaxHeightField.setAccessible(true);
                    mMaxHeightField.set(seekBar, _1dp);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                LayoutParams params = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_VERTICAL;
                params.weight = 1;
                addView(seekBar, params);
            }
            {
                duration = new TextView(context);
                duration.setTextColor(0xFFDEDEDE);
                duration.setTextSize(12);
                duration.setText("00:00");

                LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_VERTICAL;
                params.rightMargin = VideoUtils.dp2px(context, 14);
                addView(duration, params);
            }
            {
                definition = new TextView(context);
                definition.setTextColor(0xFFDEDEDE);
                definition.setTextSize(12);
                definition.setVisibility(GONE);
                definition.setGravity(Gravity.CENTER_VERTICAL);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    definition.setTextAlignment(TEXT_ALIGNMENT_CENTER);
                }

                LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_VERTICAL;
                params.rightMargin = VideoUtils.dp2px(context, 14);
                addView(definition, params);
            }
            {
                final int _12dp = VideoUtils.dp2px(context, 12);
                fullscreenToggle = new ImageView(context) {

                    private final Rect rect = new Rect();
                    private final TouchDelegate touchDelegate = new TouchDelegate(rect, this);
                    private int previousLeft = -1;
                    private int previousTop = -1;
                    private int previousRight = -1;
                    private int previousBottom = -1;

                    @Override
                    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                        super.onLayout(changed, left, top, right, bottom);
                        if (left != previousLeft || top != previousTop || right != previousRight || bottom != previousBottom) {
                            previousLeft = left;
                            previousTop = top;
                            previousRight = right;
                            previousBottom = bottom;
                            rect.set(left - _12dp, top - _12dp, right + _12dp, bottom + _12dp);
                            final View parent = (View) this.getParent();
                            if (parent != null) {
                                parent.setTouchDelegate(touchDelegate);
                            }
                        }
                    }
                };

                fullscreenToggle.setImageResource(R.drawable.video_enlarge);
                fullscreenToggle.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                int _16dp = VideoUtils.dp2px(context, 16);
                fullscreenToggle.setPadding(0, _16dp, 0, _16dp);
                fullscreenToggle.setOnClickListener(DefaultControllerView.this);


                LayoutParams params = new LayoutParams(_16dp, LayoutParams.MATCH_PARENT);
                params.gravity = Gravity.CENTER_VERTICAL;
                addView(fullscreenToggle, params);
            }
        }

        public void onVideoSceneChanged(FrameLayout videoContainer, int scene) {
            if (isVideoLandingSpace()) {
                int _30dp = VideoUtils.dp2px(getContext(), 30);
                setPadding(_30dp, 0, _30dp, 0);
                fullscreenToggle.setImageResource(R.drawable.video_shrink);
            } else {
                int _14dp = VideoUtils.dp2px(getContext(), 14);
                setPadding(_14dp, 0, _14dp, 0);
                fullscreenToggle.setImageResource(R.drawable.video_enlarge);
            }
        }
    }

    public class InitialInfoContainer extends FrameLayout {

        public final TextView playTimes;
        public final TextView duration;

        public InitialInfoContainer(Context context) {
            super(context);
            int _14dp = VideoUtils.dp2px(context, 14);
            setPadding(_14dp, 0, _14dp, 0);
            {
                playTimes = new TextView(context);
                playTimes.setTextSize(12);
                playTimes.setVisibility(GONE);
                playTimes.setTextColor(0xFFDEDEDE);

                LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                addView(playTimes, params);
            }
            {
                duration = new TextView(context);
                duration.setTextSize(12);
                duration.setTextColor(0xFFDEDEDE);
                duration.setVisibility(GONE);
                int _8dp = VideoUtils.dp2px(context, 8);
                int _3dp = VideoUtils.dp2px(context, 3);
                duration.setPadding(_8dp, _3dp, _8dp, _3dp);
                duration.setBackgroundResource(R.drawable.video_initial_info_duration_bg);

                LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
                addView(duration, params);
            }
        }
    }

    public class EndViewContainer extends FrameLayout {

        private final LinearLayout replayContainer;

        public EndViewContainer(Context context) {
            super(context);
            setBackgroundColor(0x55000000);

            replayContainer = new LinearLayout(context);
            replayContainer.setOrientation(LinearLayout.HORIZONTAL);
            replayContainer.setOnClickListener(DefaultControllerView.this);

            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.START | Gravity.BOTTOM;
            layoutParams.leftMargin = VideoUtils.dp2px(context, 14);
            layoutParams.bottomMargin = VideoUtils.dp2px(context, 12);
            addView(replayContainer, layoutParams);
            {
                ImageView replayIcon = new ImageView(context);
                replayIcon.setImageResource(R.drawable.video_replay);

                int _24dp = VideoUtils.dp2px(context, 24);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(_24dp, _24dp);
                params.gravity = Gravity.CENTER_VERTICAL;
                replayContainer.addView(replayIcon, params);
            }
            {
                TextView replayText = new TextView(context);
                replayText.setTextSize(12);
                replayText.setTextColor(0xFFDEDEDE);
                replayText.setText("重播");

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_VERTICAL;
                replayContainer.addView(replayText, params);
            }
        }
    }

    public class ErrorViewContainer extends LinearLayout {

        private final TextView errorText;
        private final TextView retryButton;

        public ErrorViewContainer(Context context) {
            super(context);
            setOrientation(VERTICAL);
            setGravity(Gravity.CENTER_HORIZONTAL);
            {
                errorText = new TextView(context);
                errorText.setTextColor(Color.WHITE);
                errorText.setTextSize(14);
                errorText.setText("视频加载失败");

                addView(errorText, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            }
            {
                retryButton = new TextView(context);
                retryButton.setText("点击重试");
                retryButton.setTextColor(Color.WHITE);
                retryButton.setTextSize(14);
                retryButton.setBackgroundResource(R.drawable.video_retry_bg);
                int _10dp = VideoUtils.dp2px(context, 10);
                int _6dp = VideoUtils.dp2px(context, 6);
                retryButton.setPadding(_10dp, _6dp, _10dp, _6dp);
                retryButton.setOnClickListener(DefaultControllerView.this);

                LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params.topMargin = VideoUtils.dp2px(context, 15);
                addView(retryButton, params);
            }
        }
    }

    public class MobileDataConfirmContainer extends LinearLayout {

        private final TextView mobileDataConfirmText;
        private final TextView mobileDataSureButton;

        public MobileDataConfirmContainer(Context context) {
            super(context);
            setOrientation(VERTICAL);
            setGravity(Gravity.CENTER_HORIZONTAL);
            {
                mobileDataConfirmText = new TextView(context);
                mobileDataConfirmText.setTextSize(14);
                mobileDataConfirmText.setTextColor(0xFFADADAD);
                mobileDataConfirmText.setText("您当前正在使用移动网络，继续播放将消耗流量");

                LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                addView(mobileDataConfirmText, params);
            }
            {
                mobileDataSureButton = new TextView(context);
                mobileDataSureButton.setTextSize(14);
                mobileDataSureButton.setTextColor(Color.WHITE);
                mobileDataSureButton.setBackgroundResource(R.drawable.video_mobile_data_sure_bg);
                mobileDataSureButton.setText("继续播放");
                int _10dp = VideoUtils.dp2px(context, 10);
                int _8dp = VideoUtils.dp2px(context, 8);
                mobileDataSureButton.setPadding(_10dp, _8dp, _10dp, _8dp);
                mobileDataSureButton.setOnClickListener(DefaultControllerView.this);

                LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params.topMargin = VideoUtils.dp2px(context, 30);
                addView(mobileDataSureButton, params);
            }
        }
    }

    @SuppressLint("AppCompatCustomView")
    public class ToastView extends TextView implements Runnable {

        public ToastView(Context context) {
            super(context);
            setTextSize(12);
            setShadowLayer(2.75f, 0, 0, 0xBB000000);
            int _12dp = VideoUtils.dp2px(context, 12);
            int _16dp = VideoUtils.dp2px(context, 16);
            setPadding(_16dp, _12dp, _16dp, _12dp);
            setTextColor(Color.WHITE);
            setGravity(Gravity.CENTER);
            setBackgroundDrawable(getBackgroundDrawable());
        }

        private Drawable getBackgroundDrawable() {
            int _10dp = VideoUtils.dp2px(getContext(), 10);
            float[] outerRadii = {_10dp, _10dp, _10dp, _10dp, _10dp, _10dp, _10dp, _10dp};
            RoundRectShape shape = new RoundRectShape(outerRadii, null, null);
            ShapeDrawable shapeDrawable = new ShapeDrawable(shape);
            shapeDrawable.getPaint().setColor(0x99000000);
            return shapeDrawable;
        }

        public void showToast(CharSequence text) {
            setText(text);
            Handler handler = getHandler();
            if (handler != null) {
                if (getVisibility() != VISIBLE) {
                    setVisibility(VISIBLE);
                    handler.postDelayed(this, 3000);
                } else {
                    handler.removeCallbacks(this);
                    handler.postDelayed(this, 3000);
                }
            }
        }

        @Override
        public void run() {
            setVisibility(GONE);
            setText(null);
        }
    }

    public static class ControllerListenerInfo {

        public View.OnTouchListener mControllerViewTouchListener;
        public View.OnClickListener mControllerViewClickListener;
        public View.OnClickListener mPlayOrPauseClickListener;
        public View.OnClickListener mReplayClickListener;
        public View.OnClickListener mRetryClickListener;
        public View.OnClickListener mFullScreenToggleClickListener;
        public View.OnClickListener mMobileDataConfirmClickListener;
        public View.OnClickListener mFullscreenBackClickListener;
        public SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener;

    }

    private static class ShowLoadingViewTask implements Runnable {

        private final WeakReference<DefaultControllerView> mControllerViewRef;

        private volatile boolean mIsRemove;

        public ShowLoadingViewTask(DefaultControllerView controllerView) {
            mControllerViewRef = new WeakReference<>(controllerView);
        }

        @Override
        public void run() {
            if (mIsRemove) return;
            DefaultControllerView controllerView = mControllerViewRef.get();
            if (controllerView == null) return;
            controllerView.showLoadingView();
        }

        void post(long delayMillis) {
            DefaultControllerView controllerView = mControllerViewRef.get();
            if (controllerView == null) return;
            mIsRemove = false;
            VideoUtils.postUiThreadDelayed(this, delayMillis);
        }

        void remove() {
            mIsRemove = true;
            DefaultControllerView controllerView = mControllerViewRef.get();
            if (controllerView == null) return;
            VideoUtils.removeUiThreadRunnable(this);
        }
    }
}