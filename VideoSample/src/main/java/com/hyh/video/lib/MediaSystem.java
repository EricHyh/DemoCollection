package com.hyh.video.lib;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
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

    private final EventHandler mEventHandler = new EventHandler(this);

    private MediaPlayer mMediaPlayer;

    private int mCurrentState = State.IDLE;

    private DataSource mDataSource;

    private long mDuration;

    private int mBufferingPercent;

    private int mErrorPosition;

    private WeakReference<MediaEventListener> mMediaEventListenerRef;

    private WeakReference<MediaProgressListener> mProgressListenerRef;

    private int mPendingCommand;

    private Integer mPendingSeekMilliSeconds;

    private Integer mPendingSeekProgress;

    private Surface mSurface;

    private Boolean mIsLooping;

    private float[] mVolume = {1.0f, 1.0f};

    private Float mSpeed;

    private long mInitTimeMillis;

    @Override
    public boolean setDataSource(DataSource source) {
        if (source == null) return false;
        if (isReleased()) {
            mMediaPlayer = newMediaPlayer();
        } else {
            if (mDataSource != null && mDataSource.equals(source)) return false;
        }
        mEventHandler.cancelPrepareTask();
        if (mDataSource != null) {
            mMediaPlayer.reset();
            this.mErrorPosition = 0;
        }
        boolean init = initMediaPlayer(source);
        if (init) {
            this.mDataSource = source;
            this.mDuration = 0;
            this.mErrorPosition = 0;
            this.mPendingSeekMilliSeconds = null;
            this.mPendingSeekProgress = null;
            postInitialized();
        }
        return init;
    }

    @Override
    public void setMediaEventListener(MediaEventListener listener) {
        this.mMediaEventListenerRef = new WeakReference<>(listener);
    }

    @Override
    public void setMediaProgressListener(MediaProgressListener listener) {
        this.mProgressListenerRef = new WeakReference<>(listener);
        if (getMediaProgressListener() != null && isPlaying()) {
            startObserveProgress();
        } else {
            stopObserveProgress();
        }
    }

    private MediaEventListener getMediaEventListener() {
        final WeakReference<MediaEventListener> mediaEventListenerRef = mMediaEventListenerRef;
        return mediaEventListenerRef != null ? mediaEventListenerRef.get() : null;
    }

    private MediaProgressListener getMediaProgressListener() {
        final WeakReference<MediaProgressListener> progressListenerRef = mProgressListenerRef;
        return progressListenerRef != null ? progressListenerRef.get() : null;
    }

    @Override
    public DataSource getDataSource() {
        return mDataSource;
    }

    @Override
    public boolean isLooping() {
        return mIsLooping != null && mIsLooping;
    }

    @Override
    public void setLooping(boolean looping) {
        this.mIsLooping = looping;
        if (isReleased()) return;
        mMediaPlayer.setLooping(looping);
    }

    @Override
    public int getMediaState() {
        return mCurrentState;
    }

    private MediaPlayer newMediaPlayer() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setScreenOnWhilePlaying(true);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnVideoSizeChangedListener(this);
        mediaPlayer.setOnCompletionListener(this);

        final Boolean isLooping = mIsLooping;
        if (isLooping != null) {
            mediaPlayer.setLooping(isLooping);
        }

        final float[] volume = mVolume;
        if (volume != null) {
            mediaPlayer.setVolume(volume[0], volume[1]);
        }

        final Float speed = mSpeed;
        if (speed != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PlaybackParams pp = mediaPlayer.getPlaybackParams();
                pp.setSpeed(speed);
                mediaPlayer.setPlaybackParams(pp);
            }
        }

        return mediaPlayer;
    }

    private boolean initMediaPlayer(DataSource source) {
        try {
            String path = source.getPath();
            mMediaPlayer.setDataSource(path);
            mInitTimeMillis = SystemClock.elapsedRealtime();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void prepare(boolean autoStart) {
        if (checkReleased()) return;
        boolean preparing = false;
        if (mCurrentState == State.INITIALIZED || mCurrentState == State.STOPPED) {
            try {
                long currentTimeMillis = SystemClock.elapsedRealtime();
                long timeInterval = Math.abs(currentTimeMillis - mInitTimeMillis);
                if (timeInterval > 1000) {
                    mMediaPlayer.prepareAsync();
                    postPreparing(autoStart);
                    preparing = true;
                    if (mSurface != null) {
                        mMediaPlayer.setSurface(mSurface);
                    }
                } else {
                    mEventHandler.startPrepareTask(1000 - timeInterval);
                    postPreparing(autoStart);
                    preparing = true;
                    if (mSurface != null) {
                        mMediaPlayer.setSurface(mSurface);
                    }
                }
            } catch (Exception e) {
                postError(0, 0);
            }
        } else if (mCurrentState == State.ERROR) {
            mMediaPlayer.reset();
            if (initMediaPlayer(mDataSource)) {
                postInitialized();
                try {
                    long currentTimeMillis = SystemClock.elapsedRealtime();
                    long timeInterval = Math.abs(currentTimeMillis - mInitTimeMillis);
                    if (timeInterval > 1000) {
                        mMediaPlayer.prepareAsync();
                        postPreparing(autoStart);
                        preparing = true;
                        if (mSurface != null) {
                            mMediaPlayer.setSurface(mSurface);
                        }
                    } else {
                        mEventHandler.startPrepareTask(1000 - timeInterval);
                        postPreparing(autoStart);
                        preparing = true;
                        if (mSurface != null) {
                            mMediaPlayer.setSurface(mSurface);
                        }
                    }
                } catch (Exception e) {
                    postError(0, 0);
                }
            } else {
                postError(0, 0);
            }
        } else if (mCurrentState == State.IDLE) {
            postError(0, 0);
        }
        if (autoStart && (preparing || mCurrentState == State.PREPARING)) {
            mPendingCommand = PENDING_COMMAND_START;
        }
    }

    @Override
    public void start() {
        if (checkReleased()) return;
        if (mCurrentState != State.STARTED) {
            postExecuteStart();
        }
        if (mCurrentState == State.PREPARED
                || mCurrentState == State.STARTED
                || mCurrentState == State.PAUSED
                || mCurrentState == State.COMPLETED) {
            if (!isPlaying()) {
                mMediaPlayer.start();
                postStart();
                if (getMediaProgressListener() != null) {
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
    public void restart() {
        if (checkReleased()) return;
        if (mCurrentState != State.STARTED) {
            postExecuteStart();
        }
        if (mCurrentState == State.PREPARED
                || mCurrentState == State.STARTED
                || mCurrentState == State.PAUSED
                || mCurrentState == State.COMPLETED) {
            seekTimeTo(0);
            if (!isPlaying()) {
                mMediaPlayer.start();
                postStart();
                if (getMediaProgressListener() != null) {
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
    public void retry() {
        if (checkReleased()) return;
        if (mCurrentState == State.ERROR) {
            mMediaPlayer.reset();
            if (initMediaPlayer(mDataSource)) {
                postInitialized();
                mMediaPlayer.prepareAsync();
                postPreparing(true);
                mPendingCommand = PENDING_COMMAND_START;
                mPendingSeekMilliSeconds = mErrorPosition;
            } else {
                postError(0, 0);
            }
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
                postPendingPause();
            }
        }
    }

    @Override
    public void stop() {
        this.mErrorPosition = 0;
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
            postPendingStop();
        }
    }

    @Override
    public boolean isExecuteStart() {
        return !isReleased() && (isPlaying() || mCurrentState == State.STARTED || mPendingCommand == PENDING_COMMAND_START);
    }

    @Override
    public boolean isPlaying() {
        if (isReleased()) return false;
        try {
            return mMediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void seekTimeTo(int millis) {
        if (mCurrentState == State.PREPARED
                || mCurrentState == State.STARTED
                || mCurrentState == State.PAUSED
                || mCurrentState == State.COMPLETED) {
            long duration = getDuration();
            if (duration > 0) {
                int progress = Math.round(millis * 1.0f / duration * 100);
                mMediaPlayer.seekTo(millis);
                postSeekStart(millis, progress);
            }
        } else {
            mPendingSeekMilliSeconds = millis;
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
            long duration = getDuration();
            if (duration > 0) {
                int milliSeconds = Math.round(duration * 1.0f * progress / 100);
                mMediaPlayer.seekTo(milliSeconds);
                postSeekStart(milliSeconds, progress);
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
        if (isReleased()) return;
        this.mErrorPosition = 0;
        stopObserveProgress();
        postRelease();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    @Override
    public long getCurrentPosition() {
        if (mCurrentState == State.PREPARED
                || mCurrentState == State.STARTED
                || mCurrentState == State.PAUSED
                || mCurrentState == State.STOPPED
                || mCurrentState == State.ERROR
                || mCurrentState == State.COMPLETED) {
            try {
                return mMediaPlayer.getCurrentPosition();
            } catch (Exception e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public long getDuration() {
        return mDuration;
    }

    @Override
    public void setSurface(Surface surface) {
        if (isReleased() || surface == null) return;
        Log.d("", "setSurface: surface = " + surface + ", mSurface = " + mSurface);
        if (surface != mSurface) {
            try {
                mSurface.release();
            } catch (Exception e) {
                //
            }
            this.mSurface = surface;
        }
        mMediaPlayer.setSurface(mSurface);
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        mVolume[0] = leftVolume;
        mVolume[1] = rightVolume;
        if (isReleased()) return;
        mMediaPlayer.setVolume(leftVolume, rightVolume);
    }

    @Override
    public boolean isSupportSpeed() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    @Override
    public void setSpeed(float speed) {
        mSpeed = speed;
        if (isReleased()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PlaybackParams pp = mMediaPlayer.getPlaybackParams();
            pp.setSpeed(speed);
            mMediaPlayer.setPlaybackParams(pp);
        }
    }

    @Override
    public boolean isReleased() {
        return mMediaPlayer == null || mCurrentState == State.END;
    }

    private boolean checkReleased() {
        if (isReleased()) {
            boolean setDataSource = setDataSource(mDataSource);
            if (!setDataSource) {
                postError(0, 0);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (isReleased()) {
            mediaPlayer.release();
            postRelease();
            return;
        }
        mDuration = mMediaPlayer.getDuration();
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
            //postPause();
        } else if (mPendingCommand == PENDING_COMMAND_STOP) {
            mPendingCommand = PENDING_COMMAND_NONE;
            mMediaPlayer.stop();
            //postStop();
        }
        if (getMediaProgressListener() != null && isPlaying()) {
            startObserveProgress();
        } else {
            stopObserveProgress();
        }
    }

    private void handlePendingSeek() {
        if (mPendingSeekMilliSeconds != null) {
            long duration = getDuration();
            if (duration > 0) {
                int progress = Math.round(mPendingSeekMilliSeconds * 1.0f / duration * 100);
                mMediaPlayer.seekTo(mPendingSeekMilliSeconds);
                postSeekStart(mPendingSeekMilliSeconds, progress);
            }
            mPendingSeekMilliSeconds = null;
        }
        if (mPendingSeekProgress != null) {
            long duration = getDuration();
            if (duration > 0) {
                int milliSeconds = Math.round(duration * 1.0f * mPendingSeekProgress / 100);
                mMediaPlayer.seekTo(milliSeconds);
                postSeekStart(milliSeconds, mPendingSeekProgress);
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
        postSeekEnd();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        postError(what, extra);
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, final int what, final int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START: {
                postPlaying();
                break;
            }
            case MediaPlayer.MEDIA_INFO_BUFFERING_START: {
                postBufferingStart();
                break;
            }
            case MediaPlayer.MEDIA_INFO_BUFFERING_END: {
                postBufferingEnd();
                break;
            }
        }
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
        mEventHandler.startObserveProgress();
    }

    private void stopObserveProgress() {
        mEventHandler.stopObserveProgress();
    }

    private void postInitialized() {
        this.mCurrentState = State.INITIALIZED;
        final MediaEventListener mediaEventListener = getMediaEventListener();
        if (mediaEventListener != null) {
            mediaEventListener.onInitialized();
        }
    }

    private void postPreparing(boolean autoStart) {
        mCurrentState = State.PREPARING;
        final MediaEventListener mediaEventListener = getMediaEventListener();
        if (mediaEventListener != null) {
            mediaEventListener.onPreparing(autoStart);
        }
    }

    private void postPrepared() {
        mCurrentState = State.PREPARED;
        final MediaEventListener mediaEventListener = getMediaEventListener();
        if (mediaEventListener != null) {
            mediaEventListener.onPrepared(getDuration());
        }
    }

    private void postExecuteStart() {
        final MediaEventListener mediaEventListener = getMediaEventListener();
        if (mediaEventListener != null) {
            mediaEventListener.onExecuteStart();
        }
    }

    private void postStart() {
        mCurrentState = State.STARTED;
        final MediaEventListener mediaEventListener = getMediaEventListener();
        if (mediaEventListener != null) {
            mediaEventListener.onStart(getCurrentPosition(), getDuration(), mBufferingPercent);
        }
    }

    private void postPlaying() {
        final MediaEventListener mediaEventListener = getMediaEventListener();
        if (mediaEventListener != null) {
            mediaEventListener.onPlaying(getCurrentPosition(), getDuration());
        }
    }

    private void postPause() {
        mCurrentState = State.PAUSED;
        final MediaEventListener mediaEventListener = getMediaEventListener();
        if (mediaEventListener != null) {
            mediaEventListener.onPause(getCurrentPosition(), getDuration());
        }
    }

    private void postPendingPause() {
        final MediaEventListener mediaEventListener = getMediaEventListener();
        if (mediaEventListener != null) {
            mediaEventListener.onPause(getCurrentPosition(), getDuration());
        }
    }

    private void postStop() {
        mCurrentState = State.STOPPED;
        final MediaEventListener mediaEventListener = getMediaEventListener();
        if (mediaEventListener != null) {
            mediaEventListener.onStop(getCurrentPosition(), getDuration());
        }
    }

    private void postPendingStop() {
        final MediaEventListener mediaEventListener = getMediaEventListener();
        if (mediaEventListener != null) {
            mediaEventListener.onStop(getCurrentPosition(), getDuration());
        }
    }

    private void postSeekStart(int seekMilliSeconds, int progress) {
        final MediaEventListener mediaEventListener = getMediaEventListener();
        if (mediaEventListener != null) {
            mediaEventListener.onSeekStart(seekMilliSeconds, progress);
        }
    }

    private void postSeekEnd() {
        final MediaEventListener mediaEventListener = getMediaEventListener();
        if (mediaEventListener != null) {
            mediaEventListener.onSeekEnd();
        }
    }

    private void postProgress() {
        long duration = getDuration();
        long currentPosition = getCurrentPosition();
        int progress = 0;
        if (duration != 0 && currentPosition != 0) {
            progress = Math.round(currentPosition * 1.0f / duration * 100);
        }
        final MediaProgressListener mediaProgressListener = getMediaProgressListener();
        if (mediaProgressListener != null) {
            mediaProgressListener.onMediaProgress(progress, currentPosition, duration);
        }
    }


    private void postBufferingStart() {
        final MediaEventListener mediaEventListener = getMediaEventListener();
        if (mediaEventListener != null) {
            mediaEventListener.onBufferingStart();
        }
    }

    private void postBufferingEnd() {
        final MediaEventListener mediaEventListener = getMediaEventListener();
        if (mediaEventListener != null) {
            mediaEventListener.onBufferingEnd();
        }
    }

    private void postBufferingUpdate(int percent) {
        if (percent == 0 || mBufferingPercent != percent) {
            mBufferingPercent = percent;
            final MediaEventListener mediaEventListener = getMediaEventListener();
            if (mediaEventListener != null) {
                mediaEventListener.onBufferingUpdate(percent);
            }
        }
    }

    private void postVideoSizeChanged(int width, int height) {
        final MediaEventListener mediaEventListener = getMediaEventListener();
        if (mediaEventListener != null) {
            mediaEventListener.onVideoSizeChanged(width, height);
        }
    }

    private void postError(int what, int extra) {
        Log.d("MediaSystem", "postError: what = " + what + ", extra = " + extra);
        try {
            mMediaPlayer.pause();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mBufferingPercent = 0;
        //if (what == 38 || what == -38 || extra == 38 || extra == -38 || extra == -19) return;
        long currentPosition = getCurrentPosition();
        if (currentPosition >= Integer.MAX_VALUE) {
            mErrorPosition = Integer.MAX_VALUE;
        } else {
            mErrorPosition = (int) currentPosition;
        }
        if (mPendingCommand == PENDING_COMMAND_START) {
            mPendingCommand = PENDING_COMMAND_NONE;
        }
        mCurrentState = State.ERROR;
        final MediaEventListener mediaEventListener = getMediaEventListener();
        if (mediaEventListener != null) {
            mediaEventListener.onError(what, extra);
        }
    }


    private void postComplete() {
        mBufferingPercent = 0;
        mCurrentState = State.COMPLETED;
        final MediaEventListener mediaEventListener = getMediaEventListener();
        if (mediaEventListener != null) {
            mediaEventListener.onCompletion();
        }
    }

    private void postRelease() {
        long currentPosition = getCurrentPosition();
        mBufferingPercent = 0;
        mCurrentState = State.END;
        final MediaEventListener mediaEventListener = getMediaEventListener();
        if (mediaEventListener != null) {
            mediaEventListener.onRelease(currentPosition, getDuration());
        }
    }

    private static class EventHandler extends Handler {

        private static final int MESSAGE_PROGRESS = 0;
        private static final int MESSAGE_PREPARE = 1;

        private final WeakReference<MediaSystem> mMediaSystemRef;

        private volatile boolean mObserveProgressStarted;

        private volatile boolean mIsCancelPrepareTask;

        EventHandler(MediaSystem mediaSystem) {
            super(Looper.getMainLooper());
            mMediaSystemRef = new WeakReference<>(mediaSystem);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MediaSystem mediaSystem = mMediaSystemRef.get();
            if (mediaSystem == null || mediaSystem.isReleased()) return;
            switch (msg.what) {
                case MESSAGE_PROGRESS: {
                    if (!mObserveProgressStarted) return;
                    mediaSystem.postProgress();
                    sendEmptyMessageDelayed(0, 1000);
                    break;
                }
                case MESSAGE_PREPARE: {
                    if (mIsCancelPrepareTask) return;
                    try {
                        mediaSystem.mMediaPlayer.prepareAsync();
                    } catch (Exception e) {
                        mediaSystem.postError(0, 0);
                    }
                    break;
                }
            }
        }

        void startObserveProgress() {
            mObserveProgressStarted = true;
            sendEmptyMessage(0);
        }

        void stopObserveProgress() {
            mObserveProgressStarted = false;
        }

        boolean hasPrepareMessage() {
            return hasMessages(MESSAGE_PREPARE);
        }

        void startPrepareTask(long delayMillis) {
            mIsCancelPrepareTask = false;
            sendEmptyMessageDelayed(MESSAGE_PREPARE, delayMillis);
        }

        void cancelPrepareTask() {
            mIsCancelPrepareTask = true;
            removeMessages(MESSAGE_PREPARE);
        }
    }
}