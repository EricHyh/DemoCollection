package com.hyh.video.sample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.hyh.video.lib.DataSource;
import com.hyh.video.lib.MediaEventListener;
import com.hyh.video.lib.MediaProgressListener;
import com.hyh.video.lib.MediaSystem;

import java.util.HashMap;

/**
 * @author Administrator
 * @description
 * @data 2019/2/11
 */

public class SurfaceActivity extends Activity implements SeekBar.OnSeekBarChangeListener, MediaEventListener, MediaProgressListener {

    private static final String TAG = "TextureActivity";

    private MediaSystem mMediaSystem = new MediaSystem();
    private FrameLayout mVideoContainer;
    private SurfaceView mSurfaceView;
    private SeekBar mSeekBar;
    private ImageView mVideoImage;

    private boolean isTestFullScreen;

    private final String mVideoUrl1 = "http://vod.cntv.lxdns.com/flash/mp4video62/TMS/2019/02/20/a5c722ca046345608b92e8defa84f70d_h264418000nero_aac32-1.mp4";
    private final String mVideoUrl2 = "http://jzvd.nathen.cn/c494b340ff704015bb6682ffde3cd302/64929c369124497593205a4190d7d128-5287d2089db37e62345123a1be272f8b.mp4";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface);
        mVideoContainer = findViewById(R.id.video_container);
        mSurfaceView = findViewById(R.id.SurfaceView);

        mSeekBar = findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(this);

        mVideoImage = findViewById(R.id.video_image);

        mMediaSystem.setDataSource(new DataSource(mVideoUrl1, DataSource.TYPE_NET));


        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback2() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Surface surface = holder.getSurface();
                mMediaSystem.setSurface(surface);
                Log.d(TAG, "surfaceCreated: ");

                if (isTestFullScreen) {
                    mMediaSystem.start();
                    isTestFullScreen = false;
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "surfaceChanged: ");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "surfaceDestroyed: ");
                mMediaSystem.pause();
            }

            @Override
            public void surfaceRedrawNeeded(SurfaceHolder holder) {
                Log.d(TAG, "surfaceRedrawNeeded: ");
            }
        });
    }

    public void prepare(View view) {
        mMediaSystem.prepare(false);
    }

    public void play(View view) {
        mMediaSystem.start();
    }

    public void pause(View view) {
        mMediaSystem.pause();
    }

    public void stop(View view) {
        mMediaSystem.stop();
    }

    public void replay(View view) {
        mMediaSystem.restart();
    }

    public void changeUrl(View view) {
        if (mMediaSystem.getDataSource() != null && TextUtils.equals(mMediaSystem.getDataSource().getPath(), mVideoUrl1)) {
            mMediaSystem.setDataSource(new DataSource(mVideoUrl2, DataSource.TYPE_NET));
        } else {
            mMediaSystem.setDataSource(new DataSource(mVideoUrl1, DataSource.TYPE_NET));
        }
    }

    public void release(View view) {
        mMediaSystem.seekTimeTo(0);
        mMediaSystem.release();
    }

    public void getFistImage(View view) {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(mVideoUrl1, new HashMap<String, String>());
        Bitmap bitmap = metadataRetriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST);
        mVideoImage.setImageBitmap(bitmap);
    }

    public void getLastImage(View view) {

    }

    public void getCurrentPosition(View view) {
        long currentPosition = mMediaSystem.getCurrentPosition();
        Toast.makeText(this, "currentPosition:" + currentPosition, Toast.LENGTH_SHORT).show();
    }

    public void getDuration(View view) {
        long duration = mMediaSystem.getDuration();
        Toast.makeText(this, "duration:" + duration, Toast.LENGTH_SHORT).show();
    }

    public void testFullScreen(View view) {
        isTestFullScreen = true;
        mMediaSystem.pause();
        mVideoContainer.removeAllViews();
        Log.d(TAG, "testFullScreen: removeAllViews");
        mVideoContainer.postDelayed(new Runnable() {
            @Override
            public void run() {
                mVideoContainer.addView(mSurfaceView);
                Log.d(TAG, "run: addView");
            }
        }, 20000);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mMediaSystem.seekProgressTo(seekBar.getProgress());
    }

    @Override
    public void onInitialized() {

    }

    @Override
    public void onPreparing(boolean autoStart) {

    }

    @Override
    public void onPrepared(long duration) {

    }

    @Override
    public void onExecuteStart() {

    }

    @Override
    public void onStart(long currentPosition, long duration, int bufferingPercent) {

    }

    @Override
    public void onPlaying(long currentPosition, long duration) {

    }

    @Override
    public void onPause(long currentPosition, long duration) {

    }

    @Override
    public void onStop(long currentPosition, long duration) {

    }

    @Override
    public void onBufferingStart() {

    }

    @Override
    public void onBufferingEnd() {

    }

    @Override
    public void onMediaProgress(int progress, long currentPosition, long duration) {
        mSeekBar.setProgress(progress);
    }

    @Override
    public void onBufferingUpdate(int percent) {
        mSeekBar.setSecondaryProgress(percent);
    }

    @Override
    public void onSeekStart(long seekMilliSeconds, int seekProgress) {
    }

    @Override
    public void onSeekEnd() {
    }

    @Override
    public void onError(int what, int extra) {
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
    }

    @Override
    public void onCompletion() {
    }

    @Override
    public void onRelease(long currentPosition, long duration) {
        mSeekBar.setProgress(0);
    }

    public void isStart(View view) {
        Toast.makeText(this, "" + mMediaSystem.isExecuteStart(), Toast.LENGTH_SHORT).show();
    }

}
