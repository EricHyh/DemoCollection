package com.hyh.video.sample;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hyh.video.base.VideoConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eric_He on 2019/3/17.
 */

public class MultiListActivity extends Activity {

    private static final String TAG = "Video_Test";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListView listView = new ListView(this);
        listView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(listView);
        listView.setAdapter(new MultiListAdapter(this));
    }


    private static class MultiListAdapter extends BaseAdapter {

        private final Context mContext;
        private final List<VideoData> mVideoDataList;

        MultiListAdapter(Context context) {
            this.mContext = context;
            mVideoDataList = new ArrayList<>();
            for (int index = 0; index < VideoConstant.videoUrlList.length; index++) {
                mVideoDataList.add(new VideoData(VideoConstant.videoUrlList[index], VideoConstant.videoThumbList[index]));
            }
        }

        @Override
        public int getCount() {
            return mVideoDataList.size() * 8 + 7;
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
            if (getItemViewType(position) == 1) {
                boolean useConvertView = false;
                if (convertView != null) {
                    Object tag = convertView.getTag();
                    if (tag != null && tag instanceof VideoHolder) {
                        VideoHolder holder = (VideoHolder) tag;
                        holder.onBindViewHolder(mVideoDataList.get(position / 8));
                        useConvertView = true;
                    }
                }
                if (!useConvertView) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_video, parent, false);
                    VideoHolder holder = new VideoHolder(convertView);
                    holder.onBindViewHolder(mVideoDataList.get(position / 8));
                }
            } else {
                if (convertView != null & convertView instanceof TextView) {
                    TextView textView = (TextView) convertView;
                    textView.setText("条目：" + position);
                } else {
                    TextView textView = new TextView(mContext);
                    textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    textView.setText("条目：" + position);
                    textView.setTextSize(20);
                    textView.setPadding(0, 250, 0, 250);
                    textView.setGravity(Gravity.CENTER);
                    textView.setTextColor(Color.BLACK);
                    convertView = textView;
                }
            }
            return convertView;
        }

        @Override
        public int getItemViewType(int position) {
            if (position % 8 == 7) {
                return 1;
            }
            return super.getItemViewType(position);
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }
    }

    private static class VideoHolder {

        private final FrameLayout mVideoContainer;

        VideoHolder(View itemView) {
            mVideoContainer = itemView.findViewById(R.id.video_container);
            itemView.setTag(this);
        }

        void onBindViewHolder(VideoData videoData) {
            videoData.bindVideoView(mVideoContainer);
        }
    }
}
