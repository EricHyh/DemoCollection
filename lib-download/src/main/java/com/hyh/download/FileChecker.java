package com.hyh.download;

/**
 * @author Administrator
 * @description
 * @data 2018/12/13
 */

public interface FileChecker {

    boolean isValidFile(DownloadInfo downloadInfo);

    boolean isRetryDownload();

}
