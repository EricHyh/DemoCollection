package com.hyh.video.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.webkit.URLUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2019/3/8
 */

public class MediaInfoImpl implements IMediaInfo {

    private final Object mLock = new Object();
    private Context mContext;
    private DataSource mDataSource;
    private List<MediaMetadataTask> mRunningTaskList = new ArrayList<>();

    public MediaInfoImpl(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void setup(DataSource source) {
        synchronized (mLock) {
            if (mDataSource == null) {
                this.mDataSource = source;
            } else {
                if (!mDataSource.equals(source)) {
                    this.mDataSource = source;
                    stopRunningTask();
                }
            }
        }
    }

    private void stopRunningTask() {
        if (!mRunningTaskList.isEmpty()) {
            Iterator<MediaMetadataTask> iterator = mRunningTaskList.iterator();
            while (iterator.hasNext()) {
                iterator.next().cancel();
                iterator.remove();
            }
        }
    }

    @Override
    public void getDuration(Result<Long> result) {
        if (mDataSource == null) result.onResult(null);
        synchronized (mLock) {
            DurationTask task = new DurationTask(mContext, mDataSource, mRunningTaskList, result);
            task.execute();
        }
    }

    @Override
    public void getVideoSize(Result<int[]> result) {
        if (mDataSource == null) result.onResult(null);
        synchronized (mLock) {
            VideoSizeTask task = new VideoSizeTask(mContext, mDataSource, mRunningTaskList, result);
            task.execute();
        }
    }

    @Override
    public void getFrameAtTime(long timeUs, Result<Bitmap> result) {
        if (mDataSource == null) result.onResult(null);
        synchronized (mLock) {
            FrameTask task = new FrameTask(mContext, mDataSource, mRunningTaskList, timeUs, result);
            task.execute();
        }
    }

    private static class DurationTask extends MediaMetadataTask<Long> {


        DurationTask(Context context, DataSource dataSource, List<MediaMetadataTask> runningTaskList, Result<Long> result) {
            super(context, dataSource, runningTaskList, result);
        }

        @Override
        Long getResult(MediaMetadataRetriever retriever) {
            Long duration = null;
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (!TextUtils.isEmpty(durationStr) && TextUtils.isDigitsOnly(durationStr)) {
                duration = Long.parseLong(durationStr);
            }
            return duration;
        }
    }

    private static class VideoSizeTask extends MediaMetadataTask<int[]> {


        VideoSizeTask(Context context, DataSource dataSource, List<MediaMetadataTask> runningTaskList, Result<int[]> result) {
            super(context, dataSource, runningTaskList, result);
        }

        @Override
        int[] getResult(MediaMetadataRetriever retriever) {
            int[] size = null;
            String videoWidthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String videoHeightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            if (!TextUtils.isEmpty(videoWidthStr) && TextUtils.isDigitsOnly(videoWidthStr)
                    && !TextUtils.isEmpty(videoHeightStr) && TextUtils.isDigitsOnly(videoHeightStr)) {
                size = new int[2];
                size[0] = Integer.parseInt(videoWidthStr);
                size[1] = Integer.parseInt(videoHeightStr);
            }
            return size;
        }
    }


    private static class FrameTask extends MediaMetadataTask<Bitmap> {

        private long timeUs;

        public FrameTask(Context context, DataSource dataSource, List<MediaMetadataTask> runningTaskList, long timeUs, Result<Bitmap> result) {
            super(context, dataSource, runningTaskList, result);
            this.timeUs = timeUs;
        }

        @Override
        Bitmap getResult(MediaMetadataRetriever retriever) {
            return retriever.getFrameAtTime(timeUs);
        }
    }

    private abstract static class MediaMetadataTask<R> extends AsyncTask<Void, Void, R> {

        @SuppressLint("StaticFieldLeak")
        final Context mContext;
        final DataSource mDataSource;
        final List<MediaMetadataTask> mRunningTaskList;
        Result<R> mResult;
        volatile boolean mIsCancel;

        MediaMetadataTask(Context context, DataSource dataSource, List<MediaMetadataTask> runningTaskList, Result<R> result) {
            this.mContext = context;
            this.mDataSource = dataSource;
            this.mRunningTaskList = runningTaskList;
            this.mResult = result;
            mRunningTaskList.add(this);
        }

        @Override
        protected R doInBackground(Void... voids) {
            if (mIsCancel) return null;
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            int pathType = mDataSource.getPathType();
            String path = mDataSource.getPath();
            boolean setDataSource = true;
            switch (pathType) {
                case DataSource.TYPE_NET: {
                    retriever.setDataSource(path, new HashMap<String, String>());
                    break;
                }
                case DataSource.TYPE_FILE: {
                    retriever.setDataSource(path);
                    break;
                }
                case DataSource.TYPE_URI: {
                    retriever.setDataSource(mContext, Uri.parse(path));
                    break;
                }
                default: {
                    if (URLUtil.isNetworkUrl(path)) {
                        retriever.setDataSource(path, new HashMap<String, String>());
                    } else if (new File(path).isFile()) {
                        retriever.setDataSource(path);
                    } else if (URLUtil.isValidUrl(path)) {
                        retriever.setDataSource(mContext, Uri.parse(path));
                    } else {
                        setDataSource = false;
                    }
                }
            }
            if (!mIsCancel && setDataSource) {
                return getResult(retriever);
            }
            return null;
        }

        void cancel() {
            mRunningTaskList.remove(this);
            this.mIsCancel = true;
            this.mResult = null;
            try {
                cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        abstract R getResult(MediaMetadataRetriever retriever);

        @Override
        protected void onPostExecute(R result) {
            super.onPostExecute(result);
            mRunningTaskList.remove(this);
            if (mIsCancel || mResult == null) return;
            mResult.onResult(result);
            mResult = null;
        }
    }
}