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

    private final String mVideoUrl1 = "http://mvvideo11.meitudata.com/5785a7e3e6a1b824.mp4?k=728dc5d4f868a569eddf82639dd8bda4&t=5c6b7f0e";
    private final String mVideoUrl2 = "http://mvvideo11.meitudata.com/5c63dbdc4f28bmhao7spvv1537_H264_1_25d3df13c76f20.mp4?k=1ead2ea555539969a4d28624b9ea6051&t=5c6d7640";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture);
        mTextureView = findViewById(R.id.TextureView);
        mMediaSystem = new MediaSystem();
        mMediaSystem.setDataSource(mVideoUrl1);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                //mTextureView.setSurfaceTexture(surface);
                mMediaSystem.setSurface(new Surface(surface));
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

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
        mMediaSystem.reStart();
    }

    public void changeUrl(View view) {
        if (TextUtils.equals(mMediaSystem.getDataSource(), mVideoUrl1)) {
            mMediaSystem.changeDataSource(mVideoUrl2);
        } else {
            mMediaSystem.changeDataSource(mVideoUrl1);
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
    public void onPrepared() {

    }

    @Override
    public void onStart(long currentPosition, long duration) {

    }

    @Override
    public void onPause(long currentPosition, long duration) {

    }

    @Override
    public void onStop(long currentPosition, long duration) {

    }

    @Override
    public void onMediaProgress(int progress, long currentPosition, long duration) {
        mSeekBar.setProgress(progress);
    }

    @Override
    public void onBufferingUpdate(int progress) {
        mSeekBar.setSecondaryProgress(progress);
    }

    @Override
    public void onSeekStart(int seekMilliSeconds, long currentPosition, long duration) {
    }

    @Override
    public void onSeekComplete(long currentPosition, long duration) {
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
    public void onRelease(long currentPosition, long duration) {
        mSeekBar.setProgress(0);
    }

}
