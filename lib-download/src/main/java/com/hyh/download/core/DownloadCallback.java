package com.hyh.download.core;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2018/12/17
 */
public interface DownloadCallback {

    void onConnected(Map<String, List<String>> responseHeaderFields);

    void onDownloading(long currentSize);

    void onRetrying(int failureCode, boolean deleteFile);

    void onSuccess();

    void onFailure(int failureCode);

}
