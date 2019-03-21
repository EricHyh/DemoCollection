package com.hyh.video.sample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.hyh.video.lib.DataSource;
import com.hyh.video.lib.HappyVideo;
import com.hyh.video.lib.MediaEventListener;
import com.hyh.video.lib.MediaProgressListener;

import java.util.HashMap;

/**
 * @author Administrator
 * @description
 * @data 2019/2/11
 */

public class VideoActivity extends Activity implements SeekBar.OnSeekBarChangeListener, MediaEventListener, MediaProgressListener {

    private static final String TAG = "VideoActivity";

    private HappyVideo mHappyVideo;
    private SeekBar mSeekBar;
    private ImageView mVideoImage;

    private final String mVideoUrl1 = "http://vod.cntv.lxdns.com/flash/mp4video62/TMS/2019/02/20/a5c722ca046345608b92e8defa84f70d_h264418000nero_aac32-1.mp4";
    private final String mVideoUrl2 = "http://mvvideo11.meitudata.com/5c63dbdc4f28bmhao7spvv1537_H264_1_25d3df13c76f20.mp4?k=1ead2ea555539969a4d28624b9ea6051&t=5c6d7640";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_happy_video);
        mHappyVideo = findViewById(R.id.HappyVideo);
        mHappyVideo.setup(new DataSource(mVideoUrl1, DataSource.TYPE_NET), "厉害了，我的哥！", false);

        mSeekBar = findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(this);

        mVideoImage = findViewById(R.id.video_image);



    }

    public void prepare(View view) {
        mHappyVideo.prepare(false);
    }

    public void play(View view) {
        mHappyVideo.start();
    }

    public void pause(View view) {
        mHappyVideo.pause();
    }

    public void stop(View view) {
        mHappyVideo.stop();
    }

    public void replay(View view) {
        mHappyVideo.restart();
    }

    public void changeUrl(View view) {
        if (mHappyVideo.getDataSource() != null && TextUtils.equals(mHappyVideo.getDataSource().getPath(), mVideoUrl1)) {
            mHappyVideo.setup(new DataSource(mVideoUrl2, DataSource.TYPE_NET), null, true);
        } else {
            mHappyVideo.setup(new DataSource(mVideoUrl1, DataSource.TYPE_NET), null, false);
        }
    }

    public void release(View view) {
        mHappyVideo.seekTimeTo(0);
        mHappyVideo.release();
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
        long currentPosition = mHappyVideo.getCurrentPosition();
        Toast.makeText(this, "currentPosition:" + currentPosition, Toast.LENGTH_SHORT).show();
    }

    public void getDuration(View view) {
        long duration = mHappyVideo.getDuration();
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
        mHappyVideo.seekProgressTo(seekBar.getProgress());
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
        Log.d(TAG, "onVideoSizeChanged: video width=" + width + ", video height=" + height);
        Log.d(TAG, "onVideoSizeChanged: view width=" + mHappyVideo.getMeasuredWidth() + ", view height=" + mHappyVideo.getMeasuredHeight());
    }

    @Override
    public void onCompletion() {
    }

    @Override
    public void onRelease(long currentPosition, long duration) {
        mSeekBar.setProgress(0);
    }

    public void isStart(View view) {
        Toast.makeText(this, "" + mHappyVideo.isExecuteStart(), Toast.LENGTH_SHORT).show();
    }
}
