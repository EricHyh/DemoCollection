package com.hyh.video.sample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.hyh.video.base.VideoConstant;
import com.hyh.video.lib.DataSource;
import com.hyh.video.lib.HappyVideo;
import com.hyh.video.lib.ImagePreview;
import com.hyh.video.lib.MediaProgressListener;
import com.squareup.picasso.Picasso;

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

        private Context mContext;

        public VideoListAdapter(Context context) {
            this.mContext = context;
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
            holder.onBindViewHolder(VideoConstant.videoUrlList[position], VideoConstant.videoThumbList[position]);
            return convertView;
        }
    }

    private static class VideoHolder implements MediaProgressListener {

        private final Context mContext;
        private final HappyVideo mHappyVideo;
        private final ImagePreview mImagePreview;

        VideoHolder(View itemView) {
            mContext = itemView.getContext();
            mHappyVideo = itemView.findViewById(R.id.video);
            mImagePreview = new ImagePreview(mContext);
            mHappyVideo.setVideoPreview(mImagePreview);
        }

        void onBindViewHolder(String url, String image) {
            mHappyVideo.setup(new DataSource(url, DataSource.TYPE_NET), null, false);
            if (!TextUtils.isEmpty(image)) {
                Picasso.with(mContext)
                        .load(image)
                        .fit()
                        .into(mImagePreview);
            }
            mHappyVideo.addMediaProgressListener(this);
        }

        @Override
        public void onMediaProgress(int progress, long currentPosition, long duration) {

        }
    }
}
