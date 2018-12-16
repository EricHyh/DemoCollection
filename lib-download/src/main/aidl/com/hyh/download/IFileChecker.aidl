// IFileChecker.aidl
package com.hyh.download;

// Declare any non-default types here with import statements
import  com.hyh.download.bean.TaskInfo;

interface IFileChecker {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    boolean isValidFile(in TaskInfo taskInfo);

    boolean isRetryDownload();
}
