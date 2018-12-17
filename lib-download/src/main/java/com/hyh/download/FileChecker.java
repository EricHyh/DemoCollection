package com.hyh.download;

import com.hyh.download.bean.DownloadInfo;

/**
 * @author Administrator
 * @description
 * @data 2018/12/13
 */

public interface FileChecker {

    boolean isValidFile(DownloadInfo downloadInfo);

    boolean isRetryDownload();

}
