package com.hyh.video.sample;

import android.view.ViewGroup;

import com.hyh.video.lib.DataSource;
import com.hyh.video.lib.HappyVideo;
import com.hyh.video.lib.MediaEventListener;
import com.hyh.video.lib.MediaProgressListener;
import com.hyh.video.lib.SimpleMediaEventListener;

/**
 * Created by Eric_He on 2019/3/11.
 */

public class VideoData implements MediaProgressListener {

    private String videoUrl;

    private String thumbUrl;

    private Integer currentPosition;

    private static final int VIDEO_ID = 1000;

    private MediaEventListener mMediaEventListener = new MyMediaEventListener();
    private HappyVideo mVideo;

    public VideoData(String videoUrl, String thumbUrl) {
        this.videoUrl = videoUrl;
        this.thumbUrl = thumbUrl;
    }

    public void bindVideoView(ViewGroup viewGroup) {
        mVideo = viewGroup.findViewById(VIDEO_ID);
        if (mVideo == null) {
            mVideo = new HappyVideo(viewGroup.getContext());
            mVideo.setId(VIDEO_ID);
            mVideo.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            viewGroup.addView(mVideo);
        }

        Object tag = mVideo.getTag();
        if (tag != null && tag instanceof VideoData && tag != this) {
            VideoData videoData = (VideoData) tag;
            videoData.unBindVideoView(mVideo);
        }
        mVideo.setTag(this);

        mVideo.setup(new DataSource(videoUrl, DataSource.TYPE_NET), "", false);
        if (currentPosition != null) {
            mVideo.seekTimeTo(currentPosition);
        }
        /*new Handler().post(new Runnable() {
            @Override
            public void run() {
                mVideo.prepare(false);
            }
        });*/
        mVideo.prepare(false);

        //video.removeMediaProgressListeners();
        mVideo.addMediaProgressListener(this);
        //video.removeMediaEventListeners();
        mVideo.addMediaEventListener(mMediaEventListener);
    }

    private void unBindVideoView(HappyVideo video) {
        long currentPosition = video.getCurrentPosition();
        if (currentPosition > 0) {
            if (currentPosition <= Integer.MAX_VALUE) {
                this.currentPosition = (int) currentPosition;
            } else {
                this.currentPosition = Integer.MAX_VALUE;
            }
        }
        video.removeMediaProgressListener(this);
        video.removeMediaEventListener(mMediaEventListener);
    }

    @Override
    public void onMediaProgress(int progress, long currentPosition, long duration) {

    }

    private class MyMediaEventListener extends SimpleMediaEventListener {

    }
}