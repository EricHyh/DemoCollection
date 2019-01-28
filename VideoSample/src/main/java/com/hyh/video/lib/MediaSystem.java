package com.hyh.video.lib;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.Surface;

/**
 * https://blog.csdn.net/shulianghan/article/details/38487967
 */
public class MediaSystem implements IMediaPlayer, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnCompletionListener {

    private static final int PENDING_COMMAND_NONE = 0;
    private static final int PENDING_COMMAND_START = 1;
    private static final int PENDING_COMMAND_PAUSE = 2;

    private MediaPlayer mMediaPlayer;

    private VideoSource mVideoSource;

    private MediaListener mListener;

    private boolean mIsPreparing;

    private boolean mIsPrepared;

    private boolean mIsReleased;

    private int mPendingCommand;

    private Integer mPendingSeekMilliSeconds;

    private Surface mPendingSurface;

    @Override
    public void setVideoSource(VideoSource videoSource) {
        this.mVideoSource = videoSource;
    }

    @Override
    public boolean isPrepared() {
        return mIsPrepared;
    }

    @Override
    public void prepare(boolean autoStart) {
        mIsReleased = false;
        if (autoStart) {
            mPendingCommand = PENDING_COMMAND_START;
        }
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setLooping(mVideoSource.isLooping());
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setDataSource(mVideoSource.getUrl());
            mediaPlayer.prepareAsync();
            mIsPreparing = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        if (isPrepared()) {
            mMediaPlayer.start();
        } else {
            mPendingCommand = PENDING_COMMAND_START;
        }
    }

    @Override
    public void pause() {
        if (isPrepared()) {
            mMediaPlayer.pause();
        } else {
            mPendingCommand = PENDING_COMMAND_START;
        }
    }

    @Override
    public boolean isPlaying() {
        return isPrepared() && mMediaPlayer.isPlaying();
    }

    @Override
    public void seekTo(int milliSeconds) {
        boolean seekSuccess = false;
        if (isPrepared()) {
            try {
                mMediaPlayer.seekTo(milliSeconds);
                seekSuccess = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!seekSuccess) {
            mPendingSeekMilliSeconds = milliSeconds;
        }
    }

    @Override
    public void release() {
        mIsReleased = true;
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                mIsPrepared = false;
            }
        } catch (Exception e) {
            //
        }
    }

    @Override
    public long getCurrentPosition() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    @Override
    public long getDuration() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    @Override
    public void setSurface(Surface surface) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setSurface(surface);
        } else {
            mPendingSurface = surface;
        }
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        mMediaPlayer.setVolume(leftVolume, rightVolume);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void setSpeed(float speed) {
        PlaybackParams pp = mMediaPlayer.getPlaybackParams();
        pp.setSpeed(speed);
        mMediaPlayer.setPlaybackParams(pp);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (mIsReleased) {
            mediaPlayer.release();
            return;
        }
        this.mMediaPlayer = mediaPlayer;
        this.mIsPrepared = true;
        this.mIsPreparing = false;
        if (mPendingCommand == PENDING_COMMAND_START) {
            mMediaPlayer.start();
            mPendingCommand = PENDING_COMMAND_NONE;
        }
        if (mPendingSeekMilliSeconds != null) {
            mMediaPlayer.seekTo(mPendingSeekMilliSeconds);
            mPendingSeekMilliSeconds = null;
        }
        if (mPendingSurface != null) {
            mMediaPlayer.setSurface(mPendingSurface);
            mPendingSurface = null;
        }
        if (mListener != null) {
            mListener.onPrepared();
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, final int percent) {
        if (mListener != null) {
            mListener.onBufferingUpdate(percent);
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        if (mListener != null) {
            mListener.onSeekComplete();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        if (mListener != null) {
            mListener.onError(what, extra);
        }
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, final int what, final int extra) {
        return false;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
        if (mListener != null) {
            mListener.onVideoSizeChanged(width, height);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mListener != null) {
            mListener.onCompletion();
        }
    }
}
