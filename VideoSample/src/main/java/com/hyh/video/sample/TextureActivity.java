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
import android.view.ViewGroup;
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

public class TextureActivity extends Activity implements SeekBar.OnSeekBarChangeListener, MediaEventListener, MediaProgressListener {

    private static final String TAG = "TextureActivity";

    private MediaSystem mMediaSystem = new MediaSystem();
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

        mSeekBar = findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mVideoImage = findViewById(R.id.video_image);
        mMediaSystem.setDataSource(new DataSource(mVideoUrl1, DataSource.TYPE_NET));

        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mMediaSystem.setSurface(new Surface(surface));
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                Log.d(TAG, "onSurfaceTextureDestroyed: ");
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

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
        mMediaSystem.prepare(false);
    }

    public void release(View view) {
        mMediaSystem.seekTimeTo(0);
        mMediaSystem.release();
    }

    public void fullscreen(View view) {
        ViewGroup parent = (ViewGroup) mTextureView.getParent();
        parent.removeView(mTextureView);
        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        ViewGroup.LayoutParams params = mTextureView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        decorView.addView(mTextureView);
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
