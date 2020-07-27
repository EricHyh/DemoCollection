package com.hyh.video.lib.cache;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.webkit.URLUtil;

import com.hyh.video.lib.DataSource;
import com.hyh.video.lib.IMediaInfo;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2020/7/27
 */
public class DiskCacheMediaInfo implements IMediaInfo {

    private final Object mLock = new Object();
    private DataSource mDataSource;
    private MediaMetadataRetriever mRetriever;


    @Override
    public void setup(DataSource source) {
        synchronized (mLock) {
            if (mDataSource == null) {
                this.mDataSource = source;
            } else {
                if (!mDataSource.equals(source)) {
                    this.mDataSource = source;
                    releaseMediaMetadataRetriever(mRetriever);
                    mRetriever = null;
                    stopRunningTask();
                }
            }
        }
    }

    private void stopRunningTask() {

    }

    @Override
    public void getDuration(Result<Long> result) {

    }

    @Override
    public void getDuration(BeforeResult<Long> beforeResult, Result<Long> result) {

    }

    @Override
    public void getVideoSize(Result<int[]> result) {

    }

    @Override
    public void getVideoSize(BeforeResult<int[]> beforeResult, Result<int[]> result) {

    }

    @Override
    public void getFrameAtTime(long timeUs, Result<Bitmap> result) {

    }

    @Override
    public void getFrameAtTime(long timeUs, BeforeResult<Bitmap> beforeResult, Result<Bitmap> result) {

    }

    @Override
    public void release() {
        releaseMediaMetadataRetriever(mRetriever);
    }

    private void releaseMediaMetadataRetriever(MediaMetadataRetriever retriever) {
        if (retriever == null) return;
        try {
            retriever.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private abstract static class MediaMetadataTask<R> extends AsyncTask<Void, Void, R> {

        @SuppressLint("StaticFieldLeak")
        final Context mContext;
        final DataSource mDataSource;
        final List<MediaMetadataTask> mRunningTaskList;
        BeforeResult<R> mBeforeResult;
        Result<R> mResult;
        volatile boolean mCanceled;

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
                try {
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
                } catch (Exception e) {
                    e.printStackTrace();
                    setDataSource = false;
                }
                if (setDataSource) {
                    result = getResult(retriever);
                }
            }
            if (result != null) {
                cacheResult(result);
            }
            if (mBeforeResult != null) {
                result = mBeforeResult.onBefore(result);
            }
            return result;
        }

        void cancel() {
            mRunningTaskList.remove(this);
            this.mCanceled = true;
            this.mResult = null;
            try {
                cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        abstract R getCache();

        abstract void cacheResult(R result);

        abstract R getResult(MediaMetadataRetriever retriever);

        @Override
        protected void onPostExecute(R result) {
            super.onPostExecute(result);
            mRunningTaskList.remove(this);
            if (mCanceled || mResult == null) return;
            mResult.onResult(result);
            mResult = null;
        }
    }

    private static class MediaMetadata {


    }
}
