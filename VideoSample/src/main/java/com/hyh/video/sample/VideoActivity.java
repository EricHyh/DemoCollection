package com.hyh.video.sample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.hyh.video.lib.MediaEventListener;
import com.hyh.video.lib.MediaProgressListener;
import com.hyh.video.lib.MediaSystem;

import java.util.HashMap;

/**
 * @author Administrator
 * @description
 * @data 2019/2/11
 */

public class VideoActivity extends Activity implements SeekBar.OnSeekBarChangeListener, MediaEventListener, MediaProgressListener {

    private static final String TAG = "VideoActivity";

    private MediaSystem mMediaSystem;
    private TextureView mTextureView;
    private SeekBar mSeekBar;
    private ImageView mVideoImage;

    private final String mVideoUrl1 = "http://vod.cntv.lxdns.com/flash/mp4video62/TMS/2019/02/20/a5c722ca046345608b92e8defa84f70d_h264418000nero_aac32-1.mp4";
    private final String mVideoUrl2 = "http://mvvideo11.meitudata.com/5c63dbdc4f28bmhao7spvv1537_H264_1_25d3df13c76f20.mp4?k=1ead2ea555539969a4d28624b9ea6051&t=5c6d7640";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture);
        mTextureView = findViewById(R.id.TextureView);
        mMediaSystem = new MediaSystem();
        mMediaSystem.setDataSource(mVideoUrl1);
        mMediaSystem.setVolume(0, 0);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                //mTextureView.setSurfaceTexture(surface);

                mMediaSystem.setSurface(new Surface(surface));
                /*if (mSurfaceTexture == null) {
                    mSurfaceTexture = surface;
                    mMediaSystem.setSurface(new Surface(surface));
                } else {
                    mTextureView.setSurfaceTexture(mSurfaceTexture);
                }*/

                Log.d(TAG, "onSurfaceTextureAvailable: surface = " + surface);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                Log.d(TAG, "onSurfaceTextureSizeChanged: ");
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                Log.d(TAG, "onSurfaceTextureDestroyed: ");
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                Log.d(TAG, "onSurfaceTextureUpdated: ");
            }
        });

        mMediaSystem.setMediaEventListener(this);
        mMediaSystem.setMediaProgressListener(this);

        mSeekBar = findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(this);

        mVideoImage = findViewById(R.id.video_image);
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
        if (TextUtils.equals(mMediaSystem.getDataSource(), mVideoUrl1)) {
            mMediaSystem.setDataSource(mVideoUrl2);
        } else {
            mMediaSystem.setDataSource(mVideoUrl1);
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
        int currentPosition = mMediaSystem.getCurrentPosition();
        Toast.makeText(this, "currentPosition:" + currentPosition, Toast.LENGTH_SHORT).show();
    }

    public void getDuration(View view) {
        int duration = mMediaSystem.getDuration();
        Toast.makeText(this, "duration:" + duration, Toast.LENGTH_SHORT).show();
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
    public void onPreparing() {

    }

    @Override
    public void onPrepared(int duration) {

    }

    @Override
    public void onStart(int currentPosition, int duration) {

    }

    @Override
    public void onPlaying(int currentPosition, int duration) {

    }

    @Override
    public void onPause(int currentPosition, int duration) {

    }

    @Override
    public void onStop(int currentPosition, int duration) {

    }

    @Override
    public void onBufferingStart() {

    }

    @Override
    public void onBufferingEnd() {

    }

    @Override
    public void onMediaProgress(int progress, int currentPosition) {
        mSeekBar.setProgress(progress);
    }

    @Override
    public void onBufferingUpdate(int progress) {
        mSeekBar.setSecondaryProgress(progress);
    }

    @Override
    public void onSeekStart(int seekMilliSeconds, int seekProgress) {
    }

    @Override
    public void onSeekEnd() {
    }

    @Override
    public void onError(int what, int extra) {
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        Log.d(TAG, "onVideoSizeChanged: video width=" + width + ", video height=" + height);
        Log.d(TAG, "onVideoSizeChanged: view width=" + mTextureView.getMeasuredWidth() + ", view height=" + mTextureView.getMeasuredHeight());
    }

    @Override
    public void onCompletion() {
    }

    @Override
    public void onRelease(int currentPosition, int duration) {
        mSeekBar.setProgress(0);
    }

}
