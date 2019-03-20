package com.hyh.video.sample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.hyh.video.base.VideoConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eric_He on 2019/3/10.
 */

public class VideoListActivity extends Activity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListView listView = new ListView(this);
        listView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(listView);
        listView.setAdapter(new VideoListAdapter(this));




    }


    private static class VideoListAdapter extends BaseAdapter {

        private final Context mContext;

        private final List<VideoData> mVideoDataList;


        public VideoListAdapter(Context context) {
            this.mContext = context;
            mVideoDataList = new ArrayList<>();
            for (int index = 0; index < VideoConstant.videoUrlList.length; index++) {
                mVideoDataList.add(new VideoData(VideoConstant.videoUrlList[index], VideoConstant.videoThumbList[index]));
            }
        }

        @Override
        public int getCount() {
            return VideoConstant.videoUrlList.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            VideoHolder holder;
            if (convertView != null) {
                holder = (VideoHolder) convertView.getTag();
            } else {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_video, parent, false);
                holder = new VideoHolder(convertView);
                convertView.setTag(holder);
            }
            holder.onBindViewHolder(mVideoDataList.get(position));
            return convertView;
        }
    }

    private static class VideoHolder {

        private final FrameLayout mVideoContainer;

        VideoHolder(View itemView) {
            mVideoContainer = itemView.findViewById(R.id.video_container);
        }

        void onBindViewHolder(VideoData videoData) {
            videoData.bindVideoView(mVideoContainer);
        }
    }
}
