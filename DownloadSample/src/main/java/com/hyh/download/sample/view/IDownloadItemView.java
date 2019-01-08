package com.hyh.download.sample.view;

import android.view.View;

import com.hyh.download.sample.presenter.IDownloadItemPresenter;

/**
 * Created by Eric_He on 2019/1/8.
 */

public interface IDownloadItemView {

    void unBindDownloadItemPresenter(IDownloadItemPresenter downloadItemPresenter);

    void bindDownloadItemPresenter(IDownloadItemPresenter downloadItemPresenter);

    void setOnClickListener(View.OnClickListener listener);

    void setTitle(String title);

    void setDownloadStatus(int downloadStatus);

    void setProgress(int progress);

    void setSpeed(float speed);

    void setFileName(String fileName);

    void setTotalSize(long totalSize);


}
