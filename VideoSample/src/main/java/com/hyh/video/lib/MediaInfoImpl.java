package com.hyh.video.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.LruCache;
import android.webkit.URLUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.KITKAT;

/**
 * @author Administrator
 * @description
 * @data 2019/3/8
 */

public class MediaInfoImpl implements IMediaInfo {

    private final static LruCache<String, Bitmap> FRAME_CACHE = new LruCache<String, Bitmap>(8 * 1024 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            int result = SDK_INT >= KITKAT ? value.getAllocationByteCount() : value.getByteCount();
            if (result < 0) {
                throw new IllegalStateException("Negative size: " + value);
            }
            return result;
        }
    };
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
        getDuration(null, result);
    }

    @Override
    public void getDuration(BeforeResult<Long> beforeResult, Result<Long> result) {
        if (mDataSource == null) {
            result.onResult(null);
            return;
        }
        Long duration = VIDEO_DURATION_CACHE.get(mDataSource.getPath());
        if (duration != null && beforeResult == null) {
            result.onResult(duration);
            return;
        }
        synchronized (mLock) {
            DurationTask task = new DurationTask(mContext, mDataSource, mRunningTaskList, beforeResult, result);
            task.execute();
        }
    }

    @Override
    public void getVideoSize(Result<int[]> result) {
        getVideoSize(null, result);
    }

    @Override
    public void getVideoSize(BeforeResult<int[]> beforeResult, Result<int[]> result) {
        if (mDataSource == null) {
            result.onResult(null);
            return;
        }
        synchronized (mLock) {
            VideoSizeTask task = new VideoSizeTask(mContext, mDataSource, mRunningTaskList, beforeResult, result);
            task.execute();
        }
    }

    @Override
    public void getFrameAtTime(long timeUs, Result<Bitmap> result) {
        getFrameAtTime(timeUs, null, result);
    }

    @Override
    public void getFrameAtTime(long timeUs, BeforeResult<Bitmap> beforeResult, Result<Bitmap> result) {
        if (mDataSource == null) {
            result.onResult(null);
            return;
        }
        Bitmap bitmap = FRAME_CACHE.get(mDataSource.getPath() + "-" + timeUs);
        if (bitmap != null && beforeResult == null) {
            result.onResult(bitmap);
            return;
        }
        synchronized (mLock) {
            FrameTask task = new FrameTask(mContext, mDataSource, mRunningTaskList, timeUs, beforeResult, result);
            task.execute();
        }
    }

    private static class DurationTask extends MediaMetadataTask<Long> {


        DurationTask(Context context, DataSource dataSource, List<MediaMetadataTask> runningTaskList, BeforeResult<Long> beforeResult, Result<Long> result) {
            super(context, dataSource, runningTaskList, beforeResult, result);
        }

        @Override
        Long getCache() {
            return VIDEO_DURATION_CACHE.get(mDataSource.getPath());
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


        VideoSizeTask(Context context, DataSource dataSource, List<MediaMetadataTask> runningTaskList, BeforeResult<int[]> beforeResult, Result<int[]> result) {
            super(context, dataSource, runningTaskList, beforeResult, result);
        }

        @Override
        int[] getCache() {
            return null;
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

        public FrameTask(Context context, DataSource dataSource, List<MediaMetadataTask> runningTaskList, long timeUs, BeforeResult<Bitmap> beforeResult, Result<Bitmap> result) {
            super(context, dataSource, runningTaskList, beforeResult, result);
            this.timeUs = timeUs;
        }

        @Override
        Bitmap getCache() {
            return FRAME_CACHE.get(mDataSource.getPath() + "-" + timeUs);
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
        BeforeResult<R> mBeforeResult;
        Result<R> mResult;
        volatile boolean mIsCancel;

        MediaMetadataTask(Context context,
                          DataSource dataSource,
                          List<MediaMetadataTask> runningTaskList,
                          BeforeResult<R> beforeResult,
                          Result<R> result) {
            this.mContext = context;
            this.mDataSource = dataSource;
            this.mRunningTaskList = runningTaskList;
            this.mBeforeResult = beforeResult;
            this.mResult = result;
            mRunningTaskList.add(this);
        }

        @Override
        protected R doInBackground(Void... voids) {
            R result = getCache();
            if (result == null) {
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
                if (setDataSource) {
                    result = getResult(retriever);
                }
            }
            if (mBeforeResult != null) {
                result = mBeforeResult.onBefore(result);
            }
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

        abstract R getCache();

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