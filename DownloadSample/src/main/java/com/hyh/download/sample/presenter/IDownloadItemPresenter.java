package com.hyh.download.sample.presenter;

import com.hyh.download.sample.view.DownloadItemView;
import com.hyh.download.sample.view.IDownloadItemView;

/**
 * Created by Eric_He on 2019/1/8.
 */

public interface IDownloadItemPresenter {

    void bindDownloadItemView(IDownloadItemView downloadItemView);

    void unBindDownloadItemView(DownloadItemView downloadItemView);
}
