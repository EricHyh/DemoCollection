package com.hyh.download.sample.presenter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.hyh.download.DownloadInfo;
import com.hyh.download.FileDownloader;
import com.hyh.download.FileRequest;
import com.hyh.download.State;
import com.hyh.download.TaskListener;
import com.hyh.download.sample.bean.DownloadBean;
import com.hyh.download.sample.utils.ReferenceUtil;
import com.hyh.download.sample.view.DownloadItemView;
import com.hyh.download.sample.view.IDownloadItemView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

/**
 * Created by Eric_He on 2019/1/8.
 */

public class DownloadItemPresenter implements IDownloadItemPresenter, View.OnClickListener, TaskListener {

    private Context mContext;

    private DownloadBean mDownloadBean;

    private WeakReference<IDownloadItemView> mDownloadItemViewRef;

    private int mDownloadStatus = State.NONE;

    private final FileRequest mFileRequest;

    public DownloadItemPresenter(Context context, DownloadBean downloadBean) {
        mContext = context;
        mDownloadBean = downloadBean;
        mFileRequest = new FileRequest
                .Builder()
                .url(mDownloadBean.url)
                .fileName(mDownloadBean.fileName)
                .build();
        FileDownloader.getInstance().addDownloadListener(mDownloadBean.url, this);
    }

    @Override
    public void bindDownloadItemView(IDownloadItemView downloadItemView) {
        IDownloadItemView oldDownloadItemView = getDownloadItemView();
        if (oldDownloadItemView != null) {
            oldDownloadItemView.unBindDownloadItemPresenter(this);
        }

        mDownloadItemViewRef = new WeakReference<>(downloadItemView);
        downloadItemView.bindDownloadItemPresenter(this);

        downloadItemView.setOnClickListener(this);
        downloadItemView.setTitle(mDownloadBean.title);

        DownloadInfo downloadInfo = FileDownloader.getInstance().getDownloadInfo(mDownloadBean.url);
        if (downloadInfo != null) {
            mDownloadStatus = downloadInfo.getCurrentStatus();
            downloadItemView.setDownloadStatus(mDownloadStatus);
            downloadItemView.setProgress(downloadInfo.getProgress());
            downloadItemView.setSpeed(0.0f);
            String filePath = downloadInfo.getFileName();
            if (TextUtils.isEmpty(filePath)) {
                downloadItemView.setFileName(null);
            } else {
                downloadItemView.setFileName(new File(filePath).getName());
            }
            downloadItemView.setTotalSize(downloadInfo.getTotalSize());
        } else {
            mDownloadStatus = State.NONE;
            downloadItemView.setDownloadStatus(mDownloadStatus);
            downloadItemView.setProgress(0);
            downloadItemView.setSpeed(0.0f);
            downloadItemView.setFileName(null);
            downloadItemView.setTotalSize(0L);
        }
    }

    @Override
    public void unBindDownloadItemView(DownloadItemView downloadItemView) {
        IDownloadItemView oldDownloadItemView = getDownloadItemView();
        if (oldDownloadItemView == downloadItemView) {
            mDownloadItemViewRef = null;
        }
    }

    private IDownloadItemView getDownloadItemView() {
        if (mDownloadItemViewRef == null) {
            return null;
        }
        return mDownloadItemViewRef.get();
    }

    @Override
    public void onClick(View v) {
        switch (mDownloadStatus) {
            case State.NONE: {
                FileDownloader.getInstance().startTask(mFileRequest);
                break;
            }
            case State.PREPARE: {
                FileDownloader.getInstance().pauseTask(mDownloadBean.url);
                break;
            }
            case State.WAITING_START: {
                FileDownloader.getInstance().pauseTask(mDownloadBean.url);
                break;
            }
            case State.WAITING_END: {
                FileDownloader.getInstance().pauseTask(mDownloadBean.url);
                break;
            }
            case State.CONNECTED: {
                FileDownloader.getInstance().pauseTask(mDownloadBean.url);
                break;
            }
            case State.DOWNLOADING: {
                FileDownloader.getInstance().pauseTask(mDownloadBean.url);
                break;
            }
            case State.RETRYING: {
                FileDownloader.getInstance().pauseTask(mDownloadBean.url);
                break;
            }
            case State.DELETE: {
                FileDownloader.getInstance().startTask(mFileRequest);
                break;
            }
            case State.PAUSE: {
                FileDownloader.getInstance().startTask(mFileRequest);
                break;
            }
            case State.SUCCESS: {
                Toast.makeText(mContext, "已下载完成", Toast.LENGTH_SHORT).show();
                break;
            }
            case State.FAILURE: {
                FileDownloader.getInstance().startTask(mFileRequest);
                break;
            }
        }
    }

    @Override
    public void onPrepare(final DownloadInfo downloadInfo) {
        mDownloadStatus = downloadInfo.getCurrentStatus();
        ReferenceUtil.checkReference(mDownloadItemViewRef, new ReferenceUtil.ReferenceListener<IDownloadItemView>() {
            @Override
            public void nonNul(@NonNull IDownloadItemView downloadItemView) {
                downloadItemView.setDownloadStatus(mDownloadStatus);
                downloadItemView.setProgress(downloadInfo.getProgress());
                downloadItemView.setSpeed(0.0f);
            }
        });
    }

    @Override
    public void onWaitingStart(final DownloadInfo downloadInfo) {
        mDownloadStatus = downloadInfo.getCurrentStatus();
        ReferenceUtil.checkReference(mDownloadItemViewRef, new ReferenceUtil.ReferenceListener<IDownloadItemView>() {
            @Override
            public void nonNul(@NonNull IDownloadItemView downloadItemView) {
                downloadItemView.setDownloadStatus(mDownloadStatus);
                downloadItemView.setProgress(downloadInfo.getProgress());
                downloadItemView.setSpeed(0.0f);
            }
        });
    }

    @Override
    public void onWaitingEnd(final DownloadInfo downloadInfo) {
        mDownloadStatus = downloadInfo.getCurrentStatus();
        ReferenceUtil.checkReference(mDownloadItemViewRef, new ReferenceUtil.ReferenceListener<IDownloadItemView>() {
            @Override
            public void nonNul(@NonNull IDownloadItemView downloadItemView) {
                downloadItemView.setDownloadStatus(mDownloadStatus);
                downloadItemView.setProgress(downloadInfo.getProgress());
                downloadItemView.setSpeed(0.0f);
            }
        });
    }

    @Override
    public void onConnected(final DownloadInfo downloadInfo, Map<String, List<String>> responseHeaderFields) {
        mDownloadStatus = downloadInfo.getCurrentStatus();
        ReferenceUtil.checkReference(mDownloadItemViewRef, new ReferenceUtil.ReferenceListener<IDownloadItemView>() {
            @Override
            public void nonNul(@NonNull IDownloadItemView downloadItemView) {
                downloadItemView.setDownloadStatus(mDownloadStatus);
                downloadItemView.setProgress(downloadInfo.getProgress());
                downloadItemView.setFileName(downloadInfo.getFileName());
                downloadItemView.setSpeed(0.0f);
                downloadItemView.setTotalSize(downloadInfo.getTotalSize());
            }
        });
    }

    @Override
    public void onDownloading(String resKey, long totalSize, long currentSize, final int progress, final float speed) {
        mDownloadStatus = State.DOWNLOADING;
        ReferenceUtil.checkReference(mDownloadItemViewRef, new ReferenceUtil.ReferenceListener<IDownloadItemView>() {
            @Override
            public void nonNul(@NonNull IDownloadItemView downloadItemView) {
                downloadItemView.setDownloadStatus(mDownloadStatus);
                downloadItemView.setProgress(progress);
                downloadItemView.setSpeed(speed);
            }
        });
    }

    @Override
    public void onRetrying(final DownloadInfo downloadInfo, boolean deleteFile) {
        mDownloadStatus = downloadInfo.getCurrentStatus();
        ReferenceUtil.checkReference(mDownloadItemViewRef, new ReferenceUtil.ReferenceListener<IDownloadItemView>() {
            @Override
            public void nonNul(@NonNull IDownloadItemView downloadItemView) {
                downloadItemView.setDownloadStatus(mDownloadStatus);
                downloadItemView.setProgress(downloadInfo.getProgress());
                downloadItemView.setSpeed(0.0f);
            }
        });
    }

    @Override
    public void onPause(final DownloadInfo downloadInfo) {
        mDownloadStatus = downloadInfo.getCurrentStatus();
        ReferenceUtil.checkReference(mDownloadItemViewRef, new ReferenceUtil.ReferenceListener<IDownloadItemView>() {
            @Override
            public void nonNul(@NonNull IDownloadItemView downloadItemView) {
                downloadItemView.setDownloadStatus(mDownloadStatus);
                downloadItemView.setProgress(downloadInfo.getProgress());
                downloadItemView.setSpeed(0.0f);
            }
        });
    }

    @Override
    public void onDelete(final DownloadInfo downloadInfo) {
        mDownloadStatus = downloadInfo.getCurrentStatus();
        ReferenceUtil.checkReference(mDownloadItemViewRef, new ReferenceUtil.ReferenceListener<IDownloadItemView>() {
            @Override
            public void nonNul(@NonNull IDownloadItemView downloadItemView) {
                downloadItemView.setDownloadStatus(mDownloadStatus);
                downloadItemView.setProgress(downloadInfo.getProgress());
                downloadItemView.setSpeed(0.0f);
            }
        });
    }

    @Override
    public void onSuccess(final DownloadInfo downloadInfo) {
        mDownloadStatus = downloadInfo.getCurrentStatus();
        ReferenceUtil.checkReference(mDownloadItemViewRef, new ReferenceUtil.ReferenceListener<IDownloadItemView>() {
            @Override
            public void nonNul(@NonNull IDownloadItemView downloadItemView) {
                downloadItemView.setDownloadStatus(mDownloadStatus);
                downloadItemView.setProgress(downloadInfo.getProgress());
                downloadItemView.setSpeed(0.0f);
            }
        });
    }

    @Override
    public void onFailure(final DownloadInfo downloadInfo) {
        mDownloadStatus = downloadInfo.getCurrentStatus();
        ReferenceUtil.checkReference(mDownloadItemViewRef, new ReferenceUtil.ReferenceListener<IDownloadItemView>() {
            @Override
            public void nonNul(@NonNull IDownloadItemView downloadItemView) {
                downloadItemView.setDownloadStatus(mDownloadStatus);
                downloadItemView.setProgress(downloadInfo.getProgress());
                downloadItemView.setSpeed(0.0f);
            }
        });
    }

}
