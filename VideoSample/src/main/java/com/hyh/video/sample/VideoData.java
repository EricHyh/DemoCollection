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

    public VideoData(String videoUrl, String thumbUrl) {
        this.videoUrl = videoUrl;
        this.thumbUrl = thumbUrl;
    }

    public void bindVideoView(ViewGroup viewGroup) {
        HappyVideo video = viewGroup.findViewById(VIDEO_ID);
        if (video == null) {
            video = new HappyVideo(viewGroup.getContext());
            video.setId(VIDEO_ID);
            video.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            viewGroup.addView(video);
        }

        Object tag = video.getTag();
        if (tag != null && tag instanceof VideoData && tag != this) {
            VideoData videoData = (VideoData) tag;
            videoData.unBindVideoView(video);
        }
        video.setTag(this);

        video.setup(new DataSource(videoUrl, DataSource.TYPE_NET), "", false);
        if (currentPosition != null) {
            video.seekTimeTo(currentPosition);
        }
        video.prepare(false);

        //video.removeMediaProgressListeners();
        video.addMediaProgressListener(this);
        //video.removeMediaEventListeners();
        video.addMediaEventListener(mMediaEventListener);
    }

    private void unBindVideoView(HappyVideo video) {
        long currentPosition = video.getCurrentPosition();
        if (currentPosition > 0 && currentPosition <= Integer.MAX_VALUE) {
            this.currentPosition = (int) currentPosition;
        } else {
            this.currentPosition = Integer.MAX_VALUE;
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