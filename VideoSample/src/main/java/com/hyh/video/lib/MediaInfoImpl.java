package com.hyh.video.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.webkit.URLUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Administrator
 * @description
 * @data 2019/3/8
 */

public class MediaInfoImpl implements IMediaInfo {

    private final static LruCache<String, Bitmap> FRAME_CACHE = new LruCache<>(8 * 1024 * 1024);
    private final static Map<String, Long> VIDEO_DURATION_CACHE = new HashMap<>();

    private final Object mLock = new Object();
    private final Context mContext;

    private DataSource mDataSource;
    private List<MediaMetadataTask> mRunningTaskList = new CopyOnWriteArrayList<>();


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
            for (MediaMetadataTask task : mRunningTaskList) {
                task.cancel();
            }
            mRunningTaskList.clear();
        }
    }

    @Override
    public void getDuration(Result<Long> result) {
        if (mDataSource == null) result.onResult(null);
        Long duration = VIDEO_DURATION_CACHE.get(mDataSource.getPath());
        if (duration != null) {
            result.onResult(duration);
            return;
        }
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
        Bitmap bitmap = FRAME_CACHE.get(mDataSource.getPath() + "-" + timeUs);
        if (bitmap != null) {
            result.onResult(bitmap);
            return;
        }
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

        @Override
        protected void onPostExecute(Long result) {
            super.onPostExecute(result);
            if (result != null) {
                String path = mDataSource.getPath();
                VIDEO_DURATION_CACHE.put(path, result);
            }
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

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (result != null) {
                String path = mDataSource.getPath();
                String key = path + "-" + timeUs;
                FRAME_CACHE.put(key, result);
            }
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
            long start = System.currentTimeMillis();
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
            R result = null;
            if (setDataSource) {
                result = getResult(retriever);
            }
            long end = System.currentTimeMillis();
            Log.d("doInBackground", "doInBackground: " + getClass().getSimpleName() + " use time " + (end - start));
            return result;
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