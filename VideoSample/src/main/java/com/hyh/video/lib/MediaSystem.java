package com.hyh.video.lib;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Surface;

import java.lang.ref.WeakReference;


/**
 * https://blog.csdn.net/shulianghan/article/details/38487967
 */
public class MediaSystem implements IMediaPlayer, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnCompletionListener {

    private static final int PENDING_COMMAND_NONE = 0;
    private static final int PENDING_COMMAND_START = 1;
    private static final int PENDING_COMMAND_PAUSE = 2;
    private static final int PENDING_COMMAND_STOP = 3;

    private final ProgressHandler mProgressHandler = new ProgressHandler(this);

    private MediaPlayer mMediaPlayer;

    private int mCurrentState = State.IDLE;

    private String mDataSource;

    private boolean mIsLooping;

    private MediaEventListener mMediaEventListener;

    private MediaProgressListener mProgressListener;

    private boolean mIsReleased;

    private int mPendingCommand;

    private Integer mPendingSeekMilliSeconds;

    private Integer mPendingSeekProgress;

    private Surface mSurface;

    public MediaSystem(String path) {
        this.mDataSource = path;
        this.mMediaPlayer = initMediaPlayer(mDataSource);
    }

    @Override
    public void changeDataSource(String path) {
        if (TextUtils.equals(mDataSource, path)) return;
        if (mCurrentState == State.END) return;
        String oldDataSource = mDataSource;
        this.mDataSource = path;
        mMediaPlayer.reset();
        setDataSource(mMediaPlayer, path);
        mCurrentState = State.INITIALIZED;
        postChangeDataSource(oldDataSource, mDataSource);
    }

    @Override
    public void setMediaEventListener(MediaEventListener listener) {
        this.mMediaEventListener = listener;
    }

    @Override
    public void setMediaProgressListener(MediaProgressListener listener) {
        this.mProgressListener = listener;
        if (mProgressListener != null && isPlaying()) {
            startObserveProgress();
        } else {
            stopObserveProgress();
        }
    }

    @Override
    public String getDataSource() {
        return mDataSource;
    }

    @Override
    public boolean isLooping() {
        return mIsLooping;
    }

    @Override
    public void setLooping(boolean looping) {
        this.mIsLooping = looping;
        if (mMediaPlayer != null) {
            mMediaPlayer.setLooping(looping);
        }
    }

    private MediaPlayer initMediaPlayer(String path) {
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setLooping(mIsLooping);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setDataSource(path);
            mCurrentState = State.INITIALIZED;
            return mediaPlayer;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void prepare(boolean autoStart) {
        if (mCurrentState == State.INITIALIZED || mCurrentState == State.STOPPED) {
            mMediaPlayer.prepareAsync();
            postPreparing();
        } else if (mCurrentState == State.ERROR) {
            mMediaPlayer.reset();
            setDataSource(mMediaPlayer, mDataSource);
            mMediaPlayer.prepareAsync();
            postPreparing();
        }
        if (autoStart) {
            mPendingCommand = PENDING_COMMAND_START;
        }
    }

    private boolean setDataSource(MediaPlayer mediaPlayer, String source) {
        try {
            mediaPlayer.setDataSource(source);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void start() {
        if (mCurrentState == State.PREPARED
                || mCurrentState == State.STARTED
                || mCurrentState == State.PAUSED
                || mCurrentState == State.COMPLETED) {
            if (!isPlaying()) {
                mMediaPlayer.start();
                mCurrentState = State.STARTED;
                postStart();
                if (mProgressListener != null) {
                    startObserveProgress();
                } else {
                    stopObserveProgress();
                }
            }
        } else {
            prepare(true);
        }
    }

    @Override
    public void reStart() {
        if (mCurrentState == State.PREPARED
                || mCurrentState == State.STARTED
                || mCurrentState == State.PAUSED
                || mCurrentState == State.COMPLETED) {
            seekTimeTo(0);
            if (!isPlaying()) {
                mMediaPlayer.start();
                mCurrentState = State.STARTED;
                postStart();
                if (mProgressListener != null) {
                    startObserveProgress();
                } else {
                    stopObserveProgress();
                }
            }
        } else {
            mPendingSeekMilliSeconds = null;
            mPendingSeekProgress = null;
            prepare(true);
        }
    }

    @Override
    public void pause() {
        stopObserveProgress();
        if (mCurrentState == State.PAUSED) return;
        if (mCurrentState == State.STARTED) {
            mMediaPlayer.pause();
            postPause();
        } else {
            if (mPendingCommand == PENDING_COMMAND_START) {
                mPendingCommand = PENDING_COMMAND_PAUSE;
            }
        }
    }

    @Override
    public void stop() {
        stopObserveProgress();
        if (mCurrentState == State.STOPPED) return;
        if (mCurrentState == State.PREPARED
                || mCurrentState == State.STARTED
                || mCurrentState == State.PAUSED
                || mCurrentState == State.COMPLETED) {
            mMediaPlayer.stop();
            postStop();
        } else {
            mPendingCommand = PENDING_COMMAND_STOP;
        }
    }

    @Override
    public boolean isPlaying() {
        try {
            return mMediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void seekTimeTo(int milliSeconds) {
        if (mCurrentState == State.PREPARED
                || mCurrentState == State.STARTED
                || mCurrentState == State.PAUSED
                || mCurrentState == State.COMPLETED) {
            mMediaPlayer.seekTo(milliSeconds);
            postSeekStart(milliSeconds);
        } else {
            mPendingSeekMilliSeconds = milliSeconds;
            mPendingSeekProgress = null;
        }
    }

    @Override
    public void seekProgressTo(int progress) {
        boolean seekSuccess = false;
        if (mCurrentState == State.PREPARED
                || mCurrentState == State.STARTED
                || mCurrentState == State.PAUSED
                || mCurrentState == State.COMPLETED) {
            int duration = getDuration();
            if (duration > 0) {
                int milliSeconds = Math.round(duration * 1.0f * progress / 100);
                mMediaPlayer.seekTo(milliSeconds);
                postSeekStart(milliSeconds);
                seekSuccess = true;
            }
        }
        if (!seekSuccess) {
            mPendingSeekProgress = progress;
            mPendingSeekMilliSeconds = null;
        }
    }

    @Override
    public void release() {
        stopObserveProgress();
        mIsReleased = true;
        if (mMediaPlayer != null) {
            postRelease();
            mMediaPlayer.release();
            mCurrentState = State.END;
            mMediaPlayer = null;
            mMediaEventListener = null;
            mProgressListener = null;
        }
    }

    @Override
    public int getCurrentPosition() {
        if (mCurrentState == State.PREPARED
                || mCurrentState == State.STARTED
                || mCurrentState == State.PAUSED
                || mCurrentState == State.STOPPED
                || mCurrentState == State.COMPLETED) {
            return mMediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    @Override
    public int getDuration() {
        if (mCurrentState == State.PREPARED
                || mCurrentState == State.STARTED
                || mCurrentState == State.PAUSED
                || mCurrentState == State.STOPPED
                || mCurrentState == State.COMPLETED) {
            return mMediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    @Override
    public void setSurface(Surface surface) {
        this.mSurface = surface;
        if (mMediaPlayer != null) {
            mMediaPlayer.setSurface(surface);
        }
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(leftVolume, rightVolume);
        }
    }

    @Override
    public void setSpeed(float speed) {
        if (mMediaPlayer != null) {
            PlaybackParams pp = mMediaPlayer.getPlaybackParams();
            pp.setSpeed(speed);
            mMediaPlayer.setPlaybackParams(pp);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (mIsReleased) {
            mediaPlayer.release();
            postRelease();
            return;
        }
        postPrepared();
        if (mPendingCommand == PENDING_COMMAND_NONE) {
            handlePendingSeek();
        } else if (mPendingCommand == PENDING_COMMAND_START) {
            handlePendingSeek();
            mMediaPlayer.start();
            mPendingCommand = PENDING_COMMAND_NONE;
            postStart();
        } else if (mPendingCommand == PENDING_COMMAND_PAUSE) {
            handlePendingSeek();
            mPendingCommand = PENDING_COMMAND_NONE;
            mMediaPlayer.pause();
            postPause();
        } else if (mPendingCommand == PENDING_COMMAND_STOP) {
            mPendingCommand = PENDING_COMMAND_NONE;
            mMediaPlayer.stop();
            postStop();
        }
        if (mProgressListener != null && isPlaying()) {
            startObserveProgress();
        } else {
            stopObserveProgress();
        }
    }

    private void handlePendingSeek() {
        if (mPendingSeekMilliSeconds != null) {
            mMediaPlayer.seekTo(mPendingSeekMilliSeconds);
            postSeekStart(mPendingSeekMilliSeconds);
            mPendingSeekMilliSeconds = null;
        }
        if (mPendingSeekProgress != null) {
            int duration = getDuration();
            if (duration > 0) {
                int milliSeconds = Math.round(duration * 1.0f * mPendingSeekProgress / 100);
                mMediaPlayer.seekTo(milliSeconds);
                postSeekStart(milliSeconds);
            }
            mPendingSeekProgress = null;
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, final int percent) {
        postBufferingUpdate(percent);
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        postSeekComplete();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        postError(what, extra);
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, final int what, final int extra) {
        return false;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
        postVideoSizeChanged(width, height);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        postComplete();
        stopObserveProgress();
    }

    private void startObserveProgress() {
        mProgressHandler.start();
    }

    private void stopObserveProgress() {
        mProgressHandler.stop();
    }


    private void postChangeDataSource(String oldDataSource, String curDataSource) {
        if (mMediaEventListener != null) {
            mMediaEventListener.onDataSourceChanged(oldDataSource, curDataSource);
        }
    }

    private void postPreparing() {
        mCurrentState = State.PREPARING;
        if (mMediaEventListener != null) {
            mMediaEventListener.onPreparing();
        }
    }

    private void postPrepared() {
        mCurrentState = State.PREPARED;
        if (mMediaEventListener != null) {
            mMediaEventListener.onPrepared();
        }
    }

    private void postStart() {
        mCurrentState = State.STARTED;
        if (mMediaEventListener != null) {
            mMediaEventListener.onStart(getCurrentPosition(), getDuration());
        }
    }

    private void postPause() {
        mCurrentState = State.PAUSED;
        if (mMediaEventListener != null) {
            mMediaEventListener.onPause(getCurrentPosition(), getDuration());
        }
    }

    private void postStop() {
        mCurrentState = State.STOPPED;
        if (mMediaEventListener != null) {
            mMediaEventListener.onStop(getCurrentPosition(), getDuration());
        }
    }

    private void postSeekStart(int seekMilliSeconds) {
        if (mMediaEventListener != null) {
            mMediaEventListener.onSeekStart(seekMilliSeconds, getCurrentPosition(), getDuration());
        }
    }

    private void postSeekComplete() {
        if (mMediaEventListener != null) {
            mMediaEventListener.onSeekComplete(getCurrentPosition(), getDuration());
        }
    }

    private void postProgress() {
        int duration = getDuration();
        int currentPosition = getCurrentPosition();
        int progress = Math.round(currentPosition * 1.0f / duration * 100);
        if (mProgressListener != null) {
            mProgressListener.onMediaProgress(progress, currentPosition, duration);
        }
    }

    private void postBufferingUpdate(int percent) {
        if (mMediaEventListener != null) {
            mMediaEventListener.onBufferingUpdate(percent);
        }
    }

    private void postVideoSizeChanged(int width, int height) {
        if (mMediaEventListener != null) {
            mMediaEventListener.onVideoSizeChanged(width, height);
        }
    }

    private void postError(int what, int extra) {
        mCurrentState = State.ERROR;
        if (mMediaEventListener != null) {
            mMediaEventListener.onError(what, extra);
        }
    }


    private void postComplete() {
        mCurrentState = State.COMPLETED;
        if (mMediaEventListener != null) {
            mMediaEventListener.onCompletion();
        }
    }

    private void postRelease() {
        mCurrentState = State.END;
        if (mMediaEventListener != null) {
            mMediaEventListener.onRelease(getCurrentPosition(), getDuration());
        }
    }

    private static class ProgressHandler extends Handler {

        private final WeakReference<MediaSystem> mMediaSystemRef;

        private boolean mIsStart;

        ProgressHandler(MediaSystem mediaSystem) {
            super(Looper.getMainLooper());
            mMediaSystemRef = new WeakReference<>(mediaSystem);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MediaSystem mediaSystem = mMediaSystemRef.get();
            if (!mIsStart) return;
            mediaSystem.postProgress();
            sendEmptyMessageDelayed(0, 1000);
        }

        void start() {
            mIsStart = true;
            sendEmptyMessage(0);
        }

        void stop() {
            mIsStart = false;
        }
    }


    private static class State {

        private static final int IDLE = 0;

        private static final int INITIALIZED = 1;

        private static final int PREPARING = 2;

        private static final int PREPARED = 3;

        private static final int STARTED = 4;

        private static final int PAUSED = 5;

        private static final int STOPPED = 6;

        private static final int COMPLETED = 7;

        private static final int ERROR = 8;

        private static final int END = 9;

    }
}
