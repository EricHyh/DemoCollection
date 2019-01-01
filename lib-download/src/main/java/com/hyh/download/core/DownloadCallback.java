package com.hyh.download.core;

import com.hyh.download.db.bean.TaskInfo;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2018/12/17
 */

public interface DownloadCallback {

    void onConnected(TaskInfo taskInfo, Map<String, List<String>> responseHeaderFields);

    void onDownloading(TaskInfo taskInfo);

    void onFailure(TaskInfo taskInfo);

    void onSuccess(TaskInfo taskInfo);

}
