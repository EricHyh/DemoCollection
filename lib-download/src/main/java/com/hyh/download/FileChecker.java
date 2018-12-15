package com.hyh.download;

import com.hyh.download.bean.TaskInfo;

/**
 * @author Administrator
 * @description
 * @data 2018/12/13
 */

public interface FileChecker {

    boolean isValidFile(TaskInfo taskInfo);

    boolean isRetryDownload();

}
