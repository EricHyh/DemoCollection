package com.hyh.download.db.bean;

import com.hyh.download.DownloadInfo;
import com.hyh.download.db.annotation.Column;
import com.hyh.download.db.annotation.Id;
import com.hyh.download.db.annotation.NotNull;
import com.hyh.download.db.annotation.Unique;
import com.hyh.download.utils.RangeUtil;

/**
 * Created by Administrator on 2017/3/14.
 */

public class TaskInfo {

    @Id
    @Column(nameInDb = "_id", indexInDb = 0)
    private long id = -1;

    @NotNull
    @Unique
    @Column(nameInDb = "resKey", indexInDb = 1)
    private String resKey;

    @Column(nameInDb = "requestUrl", indexInDb = 2)
    private String requestUrl;

    @Column(nameInDb = "targetUrl", indexInDb = 3)
    private String targetUrl;

    @Column(nameInDb = "priority", indexInDb = 4)
    private int priority;

    @NotNull
    @Column(nameInDb = "fileDir", indexInDb = 5)
    private String fileDir;

    @Column(nameInDb = "requestFileName", indexInDb = 6)
    private String requestFileName;

    @Column(nameInDb = "realFileName", indexInDb = 7)
    private String realFileName;

    @Column(nameInDb = "byMultiThread", indexInDb = 8)
    private boolean byMultiThread;

    @Column(nameInDb = "rangeNum", indexInDb = 9)
    private int rangeNum;

    @Column(nameInDb = "totalSize", indexInDb = 10)
    private volatile long totalSize;

    @Column(nameInDb = "currentSize", indexInDb = 11)
    private volatile long currentSize;

    @Column(nameInDb = "currentStatus", indexInDb = 12)
    private volatile int currentStatus;

    @Column(nameInDb = "onlyWifiDownload", indexInDb = 13)
    private boolean onlyWifiDownload;

    @Column(nameInDb = "wifiAutoRetry", indexInDb = 14)
    private boolean wifiAutoRetry;

    @Column(nameInDb = "permitRetryInMobileData", indexInDb = 15)
    private boolean permitRetryInMobileData;

    @Column(nameInDb = "permitRetryInvalidFileTask", indexInDb = 16)
    private boolean permitRetryInvalidFileTask;

    @Column(nameInDb = "permitRecoverTask", indexInDb = 17)
    private boolean permitRecoverTask;

    @Column(nameInDb = "responseCode", indexInDb = 18)
    private int responseCode;

    @Column(nameInDb = "failureCode", indexInDb = 19)
    private int failureCode;

    @Column(nameInDb = "contentMD5", indexInDb = 20)
    private String contentMD5;

    @Column(nameInDb = "contentType", indexInDb = 21)
    private String contentType;

    @Column(nameInDb = "eTag", indexInDb = 22)
    private String eTag;

    @Column(nameInDb = "lastModified", indexInDb = 23)
    private String lastModified;

    @Column(nameInDb = "updateTimeMillis", indexInDb = 24)
    private long updateTimeMillis;

    @Column(nameInDb = "tag", indexInDb = 25)
    private String tag;

    public TaskInfo() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskInfo that = (TaskInfo) o;
        return resKey.equals(that.resKey);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getResKey() {
        return resKey;
    }

    public void setResKey(String resKey) {
        this.resKey = resKey;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getFileDir() {
        return fileDir;
    }

    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

    public String getRequestFileName() {
        return requestFileName;
    }

    public void setRequestFileName(String requestFileName) {
        this.requestFileName = requestFileName;
    }

    public String getRealFileName() {
        return realFileName;
    }

    public void setRealFileName(String realFileName) {
        this.realFileName = realFileName;
    }

    public boolean isByMultiThread() {
        return byMultiThread;
    }

    public void setByMultiThread(boolean byMultiThread) {
        this.byMultiThread = byMultiThread;
    }

    public int getRangeNum() {
        return rangeNum;
    }

    public void setRangeNum(int rangeNum) {
        this.rangeNum = rangeNum;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public int getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(int currentStatus) {
        this.currentStatus = currentStatus;
    }

    public boolean isOnlyWifiDownload() {
        return onlyWifiDownload;
    }

    public void setOnlyWifiDownload(boolean onlyWifiDownload) {
        this.onlyWifiDownload = onlyWifiDownload;
    }

    public boolean isWifiAutoRetry() {
        return wifiAutoRetry;
    }

    public void setWifiAutoRetry(boolean wifiAutoRetry) {
        this.wifiAutoRetry = wifiAutoRetry;
    }

    public boolean isPermitRetryInMobileData() {
        return permitRetryInMobileData;
    }

    public void setPermitRetryInMobileData(boolean permitRetryInMobileData) {
        this.permitRetryInMobileData = permitRetryInMobileData;
    }

    public boolean isPermitRetryInvalidFileTask() {
        return permitRetryInvalidFileTask;
    }

    public void setPermitRetryInvalidFileTask(boolean permitRetryInvalidFileTask) {
        this.permitRetryInvalidFileTask = permitRetryInvalidFileTask;
    }

    public boolean isPermitRecoverTask() {
        return permitRecoverTask;
    }

    public void setPermitRecoverTask(boolean permitRecoverTask) {
        this.permitRecoverTask = permitRecoverTask;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getFailureCode() {
        return failureCode;
    }

    public void setFailureCode(int failureCode) {
        this.failureCode = failureCode;
    }

    public String getContentMD5() {
        return contentMD5;
    }

    public void setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getETag() {
        return eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public long getUpdateTimeMillis() {
        return updateTimeMillis;
    }

    public void setUpdateTimeMillis(long updateTimeMillis) {
        this.updateTimeMillis = updateTimeMillis;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public DownloadInfo toDownloadInfo() {
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.setResKey(this.resKey);
        downloadInfo.setRequestUrl(this.requestUrl);
        downloadInfo.setTargetUrl(this.targetUrl);
        downloadInfo.setPriority(this.priority);
        downloadInfo.setFileDir(this.fileDir);
        downloadInfo.setFileName(this.realFileName);

        downloadInfo.setByMultiThread(this.byMultiThread);
        downloadInfo.setOnlyWifiDownload(this.onlyWifiDownload);
        downloadInfo.setWifiAutoRetry(this.wifiAutoRetry);
        downloadInfo.setPermitRetryInMobileData(this.permitRetryInMobileData);
        downloadInfo.setPermitRetryInvalidFileTask(this.permitRetryInvalidFileTask);
        downloadInfo.setPermitRecoverTask(this.permitRecoverTask);

        downloadInfo.setTotalSize(this.totalSize);
        downloadInfo.setCurrentSize(this.currentSize);
        downloadInfo.setProgress(RangeUtil.computeProgress(this.currentSize, this.totalSize));

        downloadInfo.setCurrentStatus(this.currentStatus);

        downloadInfo.setResponseCode(this.responseCode);
        downloadInfo.setFailureCode(this.failureCode);
        downloadInfo.setContentType(this.contentType);
        downloadInfo.setTag(this.tag);
        return downloadInfo;
    }
}
