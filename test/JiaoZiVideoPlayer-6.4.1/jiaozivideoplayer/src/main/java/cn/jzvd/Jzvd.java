package com.yly.mob.ssp.video;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Nathen on 16/7/30.
 */
public abstract class Jzvd extends FrameLayout implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, JzvdTouchHelper.OnJzvdFullViewGestureCallback, View.OnTouchListener {

    public static final String TAG = "JZVD";
    public static final int THRESHOLD = 80;
    public static final int FULL_SCREEN_NORMAL_DELAY = 300;

    public static final int SCREEN_WINDOW_NORMAL = 0;
    public static final int SCREEN_WINDOW_LIST = 1;
    public static final int SCREEN_WINDOW_FULLSCREEN = 2;

    public static final int CURRENT_STATE_IDLE = -1;
    public static final int CURRENT_STATE_NORMAL = 0;
    public static final int CURRENT_STATE_PREPARING = 1;
    public static final int CURRENT_STATE_PREPARING_CHANGING_URL = 2;
    public static final int CURRENT_STATE_PLAYING = 3;
    public static final int CURRENT_STATE_PAUSE = 5;
    public static final int CURRENT_STATE_AUTO_COMPLETE = 6;
    public static final int CURRENT_STATE_ERROR = 7;

    public static final int SCALE_TYPE_ADAPTER = 0;//default
    public static final int SCALE_TYPE_FILL_PARENT = 1;
    public static final int SCALE_TYPE_FILL_CROP = 2;
    public static final int SCALE_TYPE_ORIGINAL = 3;


    public static boolean SAVE_PROGRESS = true;
    public static boolean WIFI_TIP_DIALOG_SHOWED = false;
    public static int VIDEO_IMAGE_DISPLAY_TYPE = 0;
    public static long CLICK_QUIT_FULLSCREEN_TIME = 0;
    public static long lastAutoFullscreenTime = 0;
    public static AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {//是否新建个class，代码更规矩，并且变量的位置也很尴尬
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    releaseAllVideos();
                    Log.d(TAG, "AUDIOFOCUS_LOSS [" + this.hashCode() + "]");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    try {
                        Jzvd player = JzvdMgr.getCurrentJzvd();
                        if (player != null && player.currentState == Jzvd.CURRENT_STATE_PLAYING) {
                            player.mStartButton.performClick();
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT [" + this.hashCode() + "]");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
            }
        }
    };
    protected static JZUserAction JZ_USER_EVENT;
    protected Timer UPDATE_PROGRESS_TIMER;
    protected IWindowController mWindowController;
    protected IFullScreenView mFullScreenView;

    protected int currentState = -1;
    public int currentScreen = -1;
    public long seekToInAdvance = 0;

    protected ImageView mStartButton;
    public SeekBar progressBar;
    public ImageView fullscreenButton;
    public TextView currentTimeTextView, totalTimeTextView;
    public ViewGroup mTextureViewContainer;
    public ViewGroup topContainer, bottomContainer;
    public ImageView thumbImageView;



    public int widthRatio = 0;
    public int heightRatio = 0;
    public JZDataSource jzDataSource;
    protected int mScreenWidth;
    protected int mScreenHeight;
    protected AudioManager mAudioManager;
    protected ProgressTimerTask mProgressTimerTask;
    protected boolean mTouchingProgressBar;

    protected int mGestureType;
    protected long mSeekTimePosition;


    protected JzvdRuntime mJzvdRuntime;
    protected JZTextureView mTextureView;
    protected int mOrientation;
    protected int mScaleType = SCALE_TYPE_ADAPTER;
    protected int mVideoRotation;
    private JzvdTouchHelper mJzvdTouchHelper;


    public Jzvd(Context context) {
        super(context);
        init(context);
    }

    public Jzvd(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public static void releaseAllVideos() {
        if ((System.currentTimeMillis() - CLICK_QUIT_FULLSCREEN_TIME) > FULL_SCREEN_NORMAL_DELAY) {
            Log.d(TAG, "releaseAllVideos");
            JzvdMgr.completeAll();
            JZMediaManager.instance().releaseMediaPlayer();
        }
    }

    public JZDataSource getJzDataSource() {
        return jzDataSource;
    }

    public int getCurrentState() {
        return currentState;
    }

    public void setWindowController(IWindowController windowController) {
        mWindowController = windowController;
    }

    public IWindowController getWindowController() {
        return mWindowController;
    }

    public int getSecondaryProgress() {
        return progressBar.getSecondaryProgress();
    }

    public static boolean backPress() {
        Jzvd firstFloor = JzvdMgr.getFirstFloor();
        if (firstFloor == null || firstFloor.mFullScreenView == null) return false;
        return firstFloor.mFullScreenView.close();
    }

    public static void clearSavedProgress(Context context, String url) {
        JZUtils.clearSavedProgress(context, url);
    }

    public static void setJzUserAction(JZUserAction jzUserEvent) {
        JZ_USER_EVENT = jzUserEvent;
    }

    public static void goOnPlayOnResume() {
        if (JzvdMgr.getCurrentJzvd() != null) {
            Jzvd jzvd = JzvdMgr.getCurrentJzvd();
            if (jzvd.currentState == Jzvd.CURRENT_STATE_PAUSE) {
                if (ON_PLAY_PAUSE_TMP_STATE == CURRENT_STATE_PAUSE) {
                    jzvd.onStatePause();
                    JZMediaManager.pause();
                } else {
                    jzvd.onStatePlaying();
                    JZMediaManager.start();
                }
                ON_PLAY_PAUSE_TMP_STATE = 0;
            }
        }
    }

    public static int ON_PLAY_PAUSE_TMP_STATE = 0;

    public static void goOnPlayOnPause() {
        if (JzvdMgr.getCurrentJzvd() != null) {
            Jzvd jzvd = JzvdMgr.getCurrentJzvd();
            if (jzvd.currentState == Jzvd.CURRENT_STATE_AUTO_COMPLETE ||
                    jzvd.currentState == Jzvd.CURRENT_STATE_NORMAL ||
                    jzvd.currentState == Jzvd.CURRENT_STATE_ERROR) {
                //JZVideoPlayer.releaseAllVideos();
            } else {
                ON_PLAY_PAUSE_TMP_STATE = jzvd.currentState;
                jzvd.onStatePause();
                JZMediaManager.pause();
            }
        }
    }

    public void setVideoRotation(int rotation) {
        this.mVideoRotation = rotation;
        if (mTextureView != null) {
            mTextureView.setRotation(rotation);
        }
    }

    public void setVideoScaleType(int type) {
        this.mScaleType = type;
        if (mTextureView != null) {
            mTextureView.setScaleType(mScaleType);
        }
    }

    public Object getCurrentUrl() {
        return jzDataSource.getCurrentUrl();
    }

    public abstract int getLayoutId();

    private void init(Context context) {
        mWindowController = new DefaultWindowController(this);
        View.inflate(context, getLayoutId(), this);
        mStartButton = findViewById(R.id.start);
        fullscreenButton = findViewById(R.id.fullscreen);
        progressBar = findViewById(R.id.bottom_seek_progress);
        currentTimeTextView = findViewById(R.id.current);
        totalTimeTextView = findViewById(R.id.total);
        bottomContainer = findViewById(R.id.layout_bottom);
        mTextureViewContainer = findViewById(R.id.surface_container);
        topContainer = findViewById(R.id.layout_top);


        mStartButton.setOnClickListener(this);
        fullscreenButton.setOnClickListener(this);
        progressBar.setOnSeekBarChangeListener(this);
        bottomContainer.setOnClickListener(this);
        mTextureViewContainer.setOnClickListener(this);
        mTextureViewContainer.setOnTouchListener(this);
        mJzvdTouchHelper = new JzvdTouchHelper(this, this);

        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);

        mOrientation = getResources().getConfiguration().orientation;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mOrientation = getResources().getConfiguration().orientation;
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (currentScreen != SCREEN_WINDOW_FULLSCREEN) {
            JzvdRuntime jzvdRuntime = JZMediaManager.instance().getCurrentJzvdRuntime();
            if (jzvdRuntime != null && jzvdRuntime == mJzvdRuntime) {
                mJzvdRuntime.release();
            }
        }
    }

    public void setUp(String url, String title, int screen) {
        setUp(new JZDataSource(url, title), screen);
    }

    public void setUp(JZDataSource jzDataSource, int screen) {
        if (this.jzDataSource != null && jzDataSource.getCurrentUrl() != null &&
                this.jzDataSource.containsTheUrl(jzDataSource.getCurrentUrl())) {
            return;
        }
        if (isCurrentJZVD() && jzDataSource.containsTheUrl(JZMediaManager.getCurrentUrl())) {
            long position = 0;
            try {
                position = JZMediaManager.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            if (position != 0) {
                JZUtils.saveProgress(getContext(), JZMediaManager.getCurrentUrl(), position);
            }
            JZMediaManager.instance().releaseMediaPlayer();
        }
        this.jzDataSource = jzDataSource;
        this.currentScreen = screen;
        onStateNormal();

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.start) {
            Log.i(TAG, "onClick start [" + this.hashCode() + "] ");
            if (jzDataSource == null || jzDataSource.urlsMap.isEmpty() || jzDataSource.getCurrentUrl() == null) {
                Toast.makeText(getContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentState == CURRENT_STATE_NORMAL) {
                if (!jzDataSource.getCurrentUrl().toString().startsWith("file") && !
                        jzDataSource.getCurrentUrl().toString().startsWith("/") &&
                        !JZUtils.isWifiConnected(getContext()) && !WIFI_TIP_DIALOG_SHOWED) {
                    showWifiDialog();
                    return;
                }
                startVideo();
                onEvent(JZUserAction.ON_CLICK_START_ICON);//开始的事件应该在播放之后，此处特殊
            } else if (currentState == CURRENT_STATE_PLAYING) {
                onEvent(JZUserAction.ON_CLICK_PAUSE);
                Log.d(TAG, "pauseVideo [" + this.hashCode() + "] ");
                JZMediaManager.pause();
                onStatePause();
            } else if (currentState == CURRENT_STATE_PAUSE) {
                onEvent(JZUserAction.ON_CLICK_RESUME);
                JZMediaManager.start();
                onStatePlaying();
            } else if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
                onEvent(JZUserAction.ON_CLICK_START_AUTO_COMPLETE);
                startVideo();
            }
        } else if (i == R.id.fullscreen) {
            Log.i(TAG, "onClick fullscreen [" + this.hashCode() + "] ");
            if (currentState == CURRENT_STATE_AUTO_COMPLETE) return;
            if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
                //quit fullscreen
                backPress();
            } else {
                Log.d(TAG, "toFullscreenActivity [" + this.hashCode() + "] ");
                onEvent(JZUserAction.ON_ENTER_FULLSCREEN);
                startWindowFullscreen();
            }
        }
    }


    @Override
    public void onActionDown() {
        mTouchingProgressBar = true;
    }

    @Override
    public void onStartControl(int gestureType) {
        cancelProgressTimer();
        mGestureType = gestureType;
    }

    @Override
    public void onControlPosition(float deltaX, String seekTime, long seekTimePosition, String totalTime, long totalTimeDuration) {
        mSeekTimePosition = seekTimePosition;
        showProgressDialog(deltaX, seekTime, seekTimePosition, totalTime, totalTimeDuration);
    }

    @Override
    public void onControlVolume(float deltaY, int volumePercent) {
        showVolumeDialog(deltaY, volumePercent);
    }

    @Override
    public void onControlBrightness(float deltaY, int brightnessPercent) {
        showBrightnessDialog(brightnessPercent);
    }

    @Override
    public void onEndControl(int gestureType) {
        Log.i(TAG, "onTouch surfaceContainer actionUp [" + this.hashCode() + "] ");
        dismissProgressDialog();
        dismissVolumeDialog();
        dismissBrightnessDialog();
        if (gestureType == JzvdTouchHelper.TYPE_POSITION) {
            onEvent(JZUserAction.ON_TOUCH_SCREEN_SEEK_POSITION);
            JZMediaManager.seekTo(mSeekTimePosition);
            long duration = getDuration();
            int progress = (int) (mSeekTimePosition * 100 / (duration == 0 ? 1 : duration));
            progressBar.setProgress(progress);
        }
        if (gestureType == JzvdTouchHelper.TYPE_VOLUME) {
            onEvent(JZUserAction.ON_TOUCH_SCREEN_SEEK_VOLUME);
        }
        startProgressTimer();

        mTouchingProgressBar = false;
        mGestureType = JzvdTouchHelper.TYPE_NONE;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == mTextureViewContainer && currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            return mJzvdTouchHelper.onTouch(v, event);
        }
        return false;
    }

    public void startVideo() {
        if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            AudioManager mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            if (mAudioManager != null) {
                mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            }
            mWindowController.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            onStatePreparing();

            JZMediaManager.instance().prepare();
        } else {
            JZMediaManager.instance().releaseCurrentJzvd();
            JzvdMgr.completeAll();

            mJzvdRuntime = JZMediaManager.instance().createJZTextureView(this, jzDataSource);
            mTextureView = mJzvdRuntime.mTextureView;
            addTextureView();

            AudioManager mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            if (mAudioManager != null) {
                mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            }

            mWindowController.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            onStatePreparing();

            JzvdMgr.setFirstFloor(this);
            JzvdMgr.setSecondFloor(null);
        }
    }

    public void onPrepared() {
        Log.i(TAG, "onPrepared " + " [" + this.hashCode() + "] ");
        onStatePrepared();
        onStatePlaying();
    }

    public void setState(int state) {
        setState(state, 0, 0);
    }

    public void setState(int state, int urlMapIndex, int seekToInAdvance) {
        switch (state) {
            case CURRENT_STATE_NORMAL:
                onStateNormal();
                break;
            case CURRENT_STATE_PREPARING:
                onStatePreparing();
                break;
            case CURRENT_STATE_PREPARING_CHANGING_URL:
                changeUrl(urlMapIndex, seekToInAdvance);
                break;
            case CURRENT_STATE_PLAYING:
                onStatePlaying();
                break;
            case CURRENT_STATE_PAUSE:
                onStatePause();
                break;
            case CURRENT_STATE_ERROR:
                onStateError();
                break;
            case CURRENT_STATE_AUTO_COMPLETE:
                onStateAutoComplete();
                break;
        }
    }

    public void onStateNormal() {
        Log.i(TAG, "onStateNormal " + " [" + this.hashCode() + "] ");
        currentState = CURRENT_STATE_NORMAL;
        cancelProgressTimer();
    }

    public void onStatePreparing() {
        Log.i(TAG, "onStatePreparing " + " [" + this.hashCode() + "] ");
        currentState = CURRENT_STATE_PREPARING;
        resetProgressAndTime();
    }

    public void changeUrl(int urlMapIndex, long seekToInAdvance) {
        currentState = CURRENT_STATE_PREPARING_CHANGING_URL;
        this.seekToInAdvance = seekToInAdvance;
        jzDataSource.currentUrlIndex = urlMapIndex;
        JZMediaManager.setDataSource(jzDataSource);
        JZMediaManager.instance().prepare();
    }

    public void changeUrl(JZDataSource jzDataSource, long seekToInAdvance) {
        currentState = CURRENT_STATE_PREPARING_CHANGING_URL;
        this.seekToInAdvance = seekToInAdvance;
        this.jzDataSource = jzDataSource;
        if (JzvdMgr.getSecondFloor() != null && JzvdMgr.getFirstFloor() != null) {
            JzvdMgr.getFirstFloor().jzDataSource = jzDataSource;
        }
        JZMediaManager.setDataSource(jzDataSource);
        JZMediaManager.instance().prepare();
    }

    public void changeUrl(String url, String title, long seekToInAdvance) {
        changeUrl(new JZDataSource(url, title), seekToInAdvance);
    }

    public void onStatePrepared() {//因为这个紧接着就会进入播放状态，所以不设置state
        if (seekToInAdvance != 0) {
            JZMediaManager.seekTo(seekToInAdvance);
            seekToInAdvance = 0;
        } else {
            long position = JZUtils.getSavedProgress(getContext(), jzDataSource.getCurrentUrl());
            if (position != 0) {
                JZMediaManager.seekTo(position);
            }
        }
    }

    public void onStatePlaying() {
        Log.i(TAG, "onStatePlaying " + " [" + this.hashCode() + "] ");
        currentState = CURRENT_STATE_PLAYING;
        startProgressTimer();
    }

    public void onStatePause() {
        Log.i(TAG, "onStatePause " + " [" + this.hashCode() + "] ");
        currentState = CURRENT_STATE_PAUSE;
        startProgressTimer();
    }

    public void onStateError() {
        Log.i(TAG, "onStateError " + " [" + this.hashCode() + "] ");
        currentState = CURRENT_STATE_ERROR;
        cancelProgressTimer();
    }

    public void onStateAutoComplete() {
        Log.i(TAG, "onStateAutoComplete " + " [" + this.hashCode() + "] ");
        currentState = CURRENT_STATE_AUTO_COMPLETE;
        cancelProgressTimer();
        progressBar.setProgress(100);
        currentTimeTextView.setText(totalTimeTextView.getText());
    }

    public void onInfo(int what, int extra) {
        Log.d(TAG, "onInfo what - " + what + " extra - " + extra);
    }

    public void onError(int what, int extra) {
        Log.e(TAG, "onError " + what + " - " + extra + " [" + this.hashCode() + "] ");
        if (what != 38 && extra != -38 && what != -38 && extra != 38 && extra != -19) {
            onStateError();
            if (isCurrentPlay()) {
                JZMediaManager.instance().releaseMediaPlayer();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        if (widthRatio != 0 && heightRatio != 0) {
            int specWidth = MeasureSpec.getSize(widthMeasureSpec);
            int specHeight = (int) ((specWidth * (float) heightRatio) / widthRatio);
            setMeasuredDimension(specWidth, specHeight);

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(specHeight, MeasureSpec.EXACTLY);
            getChildAt(0).measure(childWidthMeasureSpec, childHeightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    }

    public void onAutoCompletion() {
        Runtime.getRuntime().gc();
        Log.i(TAG, "onAutoCompletion " + " [" + this.hashCode() + "] ");
        onEvent(JZUserAction.ON_AUTO_COMPLETE);
        dismissVolumeDialog();
        dismissProgressDialog();
        dismissBrightnessDialog();
        onStateAutoComplete();

        if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            backPress();
        }
        JZMediaManager.instance().releaseMediaPlayer();
        mWindowController.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        JZUtils.saveProgress(getContext(), jzDataSource.getCurrentUrl(), 0);
    }

    public void onCompletion() {
        Log.i(TAG, "onCompletion " + " [" + this.hashCode() + "] ");
        if (currentState == CURRENT_STATE_PLAYING || currentState == CURRENT_STATE_PAUSE) {
            long position = getCurrentPositionWhenPlaying();
            JZUtils.saveProgress(getContext(), jzDataSource.getCurrentUrl(), position);
        }
        cancelProgressTimer();
        dismissBrightnessDialog();
        dismissProgressDialog();
        dismissVolumeDialog();
        onStateNormal();

        if (mTextureView != null && mTextureView.getParent() == mTextureViewContainer) {
            mTextureViewContainer.removeView(mTextureView);
        }

        JZMediaManager.instance().setVideoWidth(0);
        JZMediaManager.instance().setVideoHeight(0);

        AudioManager mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        }

        mWindowController.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (mFullScreenView != null && mFullScreenView.isShow()) {
            mFullScreenView.destroy();
        }

        JZMediaManager.instance().releaseCurrentJzvd();
    }

    public void release() {
        if (jzDataSource.getCurrentUrl().equals(JZMediaManager.getCurrentUrl()) &&
                (System.currentTimeMillis() - CLICK_QUIT_FULLSCREEN_TIME) > FULL_SCREEN_NORMAL_DELAY) {
            //在非全屏的情况下只能backPress()
            if (JzvdMgr.getSecondFloor() != null &&
                    JzvdMgr.getSecondFloor().currentScreen == SCREEN_WINDOW_FULLSCREEN) {//点击全屏
            } else if (JzvdMgr.getSecondFloor() == null && JzvdMgr.getFirstFloor() != null &&
                    JzvdMgr.getFirstFloor().currentScreen == SCREEN_WINDOW_FULLSCREEN) {//直接全屏
            } else {
                Log.d(TAG, "releaseMediaPlayer [" + this.hashCode() + "]");
                releaseAllVideos();
            }
        }
    }

    public void addTextureView() {
        Log.d(TAG, "addTextureView [" + this.hashCode() + "] ");
        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER);
        mTextureViewContainer.addView(mTextureView, layoutParams);
    }

    public void onVideoSizeChanged() {
        Log.i(TAG, "onVideoSizeChanged " + " [" + this.hashCode() + "] ");
        if (mTextureView != null) {
            if (mVideoRotation != 0) {
                mTextureView.setRotation(mVideoRotation);
            }
            mTextureView.setVideoSize(JZMediaManager.instance().getVideoWidth(), JZMediaManager.instance().getVideoHeight());
        }
    }

    public void startProgressTimer() {
        Log.i(TAG, "startProgressTimer: " + " [" + this.hashCode() + "] ");
        cancelProgressTimer();
        UPDATE_PROGRESS_TIMER = new Timer();
        mProgressTimerTask = new ProgressTimerTask();
        UPDATE_PROGRESS_TIMER.schedule(mProgressTimerTask, 0, 300);
    }

    public void cancelProgressTimer() {
        if (UPDATE_PROGRESS_TIMER != null) {
            UPDATE_PROGRESS_TIMER.cancel();
        }
        if (mProgressTimerTask != null) {
            mProgressTimerTask.cancel();
        }
    }

    public void onProgress(int progress, long position, long duration) {
        if (!mTouchingProgressBar) {
            if (seekToManulPosition != -1) {
                if (seekToManulPosition > progress) {
                    return;
                } else {
                    seekToManulPosition = -1;
                }
            } else {
                if (progress != 0) progressBar.setProgress(progress);
            }
        }
        if (position != 0) currentTimeTextView.setText(JZUtils.stringForTime(position));
        totalTimeTextView.setText(JZUtils.stringForTime(duration));
    }

    public void setBufferProgress(int bufferProgress) {
        if (bufferProgress != 0) progressBar.setSecondaryProgress(bufferProgress);
    }

    public void resetProgressAndTime() {
        progressBar.setProgress(0);
        progressBar.setSecondaryProgress(0);
        currentTimeTextView.setText(JZUtils.stringForTime(0));
        totalTimeTextView.setText(JZUtils.stringForTime(0));
    }

    public long getCurrentPositionWhenPlaying() {
        long position = 0;
        //TODO 这块的判断应该根据MediaPlayer来
        if (currentState == CURRENT_STATE_PLAYING ||
                currentState == CURRENT_STATE_PAUSE) {
            try {
                position = JZMediaManager.getCurrentPosition();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return position;
    }

    public long getDuration() {
        long duration = 0;
        //TODO MediaPlayer 判空的问题
        try {
            duration = JZMediaManager.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
            return duration;
        }
        return duration;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.i(TAG, "bottomProgress onStartTrackingTouch [" + this.hashCode() + "] ");
        cancelProgressTimer();
        ViewParent vpdown = getParent();
        while (vpdown != null) {
            vpdown.requestDisallowInterceptTouchEvent(true);
            vpdown = vpdown.getParent();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.i(TAG, "bottomProgress onStopTrackingTouch [" + this.hashCode() + "] ");
        onEvent(JZUserAction.ON_SEEK_POSITION);
        startProgressTimer();
        ViewParent vpup = getParent();
        while (vpup != null) {
            vpup.requestDisallowInterceptTouchEvent(false);
            vpup = vpup.getParent();
        }
        if (currentState != CURRENT_STATE_PLAYING &&
                currentState != CURRENT_STATE_PAUSE) return;
        long time = seekBar.getProgress() * getDuration() / 100;
        seekToManulPosition = seekBar.getProgress();
        JZMediaManager.seekTo(time);
        Log.i(TAG, "seekTo " + time + " [" + this.hashCode() + "] ");
    }

    public int seekToManulPosition = -1;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            //设置这个progres对应的时间，给textview
            long duration = getDuration();
            currentTimeTextView.setText(JZUtils.stringForTime(progress * duration / 100));
        }
    }

    public void startWindowFullscreen() {
        if (mFullScreenView != null && mFullScreenView.isShow()) {
            return;
        }
        mFullScreenView = newFullScreenView();
        if (mFullScreenView == null) {
            return;
        }
        mTextureViewContainer.removeView(mTextureView);
        mFullScreenView.setUp(this);
        mFullScreenView.show();
        onStateNormal();
    }

    protected IFullScreenView newFullScreenView() {
        return new JzvdFullScreenView();
    }

    public boolean isCurrentPlay() {
        return isCurrentJZVD()
                && jzDataSource.containsTheUrl(JZMediaManager.getCurrentUrl());//不仅正在播放的url不能一样，并且各个清晰度也不能一样
    }

    public boolean isCurrentJZVD() {
        return JzvdMgr.getCurrentJzvd() != null
                && JzvdMgr.getCurrentJzvd() == this;
    }


    public void onCloseFullScreen(Jzvd full) {
        mFullScreenView = null;
        setState(full.getCurrentState());
        progressBar.setSecondaryProgress(full.getSecondaryProgress());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        if (mTextureView != null && mTextureView.getParent() != null) {
            ((ViewGroup) mTextureView.getParent()).removeView(mTextureView);
        }
        mTextureViewContainer.addView(full.getTextureView(), layoutParams);

    }

    //重力感应的时候调用的函数，
    public void autoFullscreen(float x) {
        if (isCurrentPlay()
                && (currentState == CURRENT_STATE_PLAYING || currentState == CURRENT_STATE_PAUSE)
                && currentScreen != SCREEN_WINDOW_FULLSCREEN) {
            if (x > 0) {
                //JZUtils.setRequestedOrientation(getContext(), ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                mWindowController.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                //JZUtils.setRequestedOrientation(getContext(), ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                mWindowController.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            }
            onEvent(JZUserAction.ON_ENTER_FULLSCREEN);
            startWindowFullscreen();
        }
    }

    public void autoQuitFullscreen() {
        if ((System.currentTimeMillis() - lastAutoFullscreenTime) > 2000
                && isCurrentPlay()
                && currentState == CURRENT_STATE_PLAYING
                && currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            lastAutoFullscreenTime = System.currentTimeMillis();
            backPress();
        }
    }

    public void onEvent(int type) {
        if (JZ_USER_EVENT != null && isCurrentPlay() && !jzDataSource.urlsMap.isEmpty()) {
            JZ_USER_EVENT.onEvent(type, jzDataSource.getCurrentUrl(), currentScreen);
        }
    }

    public static void setMediaInterface(JZMediaInterface mediaInterface) {
        JZMediaManager.instance().jzMediaInterface = mediaInterface;
    }

    //TODO 是否有用
    public void onSeekComplete() {

    }

    public void showWifiDialog() {
    }

    public void showProgressDialog(float deltaX,
                                   String seekTime, long seekTimePosition,
                                   String totalTime, long totalTimeDuration) {
    }

    public void dismissProgressDialog() {

    }

    public void showVolumeDialog(float deltaY, int volumePercent) {

    }

    public void dismissVolumeDialog() {

    }

    public void showBrightnessDialog(int brightnessPercent) {

    }

    public void dismissBrightnessDialog() {

    }

    public int getOrientation() {
        return mOrientation;
    }

    public JZTextureView getTextureView() {
        return mTextureView;
    }

    public void setTextureView(JZTextureView textureView) {
        mTextureView = textureView;
    }

    public void play() {
        if (JZMediaManager.isPlaying()) return;
        if (JZMediaManager.isPrepared()) {
            JZMediaManager.start();
            onStatePlaying();
        } else {
            startVideo();
        }
    }

    public void pause() {
        if (!JZMediaManager.isPlaying()) return;
        JZMediaManager.pause();
        onStatePause();
    }

    public static class JZAutoFullscreenListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {//可以得到传感器实时测量出来的变化值
            final float x = event.values[SensorManager.DATA_X];
            float y = event.values[SensorManager.DATA_Y];
            float z = event.values[SensorManager.DATA_Z];
            //过滤掉用力过猛会有一个反向的大数值
            if (x < -12 || x > 12) {
                if ((System.currentTimeMillis() - lastAutoFullscreenTime) > 2000) {
                    if (JzvdMgr.getCurrentJzvd() != null) {
                        JzvdMgr.getCurrentJzvd().autoFullscreen(x);
                    }
                    lastAutoFullscreenTime = System.currentTimeMillis();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    public class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            if (currentState == CURRENT_STATE_PLAYING || currentState == CURRENT_STATE_PAUSE) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        long position = getCurrentPositionWhenPlaying();
                        long duration = getDuration();
                        int progress = (int) (position * 100 / (duration == 0 ? 1 : duration));

                        onProgress(progress, position, duration);
                    }
                });
            }
        }
    }

    public Context getApplicationContext() {
        Context context = getContext();
        if (context != null) {
            Context applicationContext = context.getApplicationContext();
            if (applicationContext != null) {
                return applicationContext;
            }
        }
        return context;
    }
}
