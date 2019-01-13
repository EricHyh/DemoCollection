package com.hyh.download;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Administrator
 * @description
 * @data 2018/12/17
 */

public class DownloadInfo implements Parcelable {

    private String resKey;

    private String requestUrl;

    private String targetUrl;

    private int priority;

    private boolean byMultiThread;

    private boolean onlyWifiDownload;

    private boolean wifiAutoRetry;

    private boolean permitRetryInMobileData;

    private boolean permitRetryInvalidFileTask;

    private boolean permitRecoverTask;

    private String fileDir;

    private String fileName;

    private long totalSize;

    private long currentSize;

    private int progress;

    private int currentStatus;

    private int responseCode;

    private int failureCode;

    private String contentType;

    private String tag;

    public DownloadInfo() {
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

    public boolean isByMultiThread() {
        return byMultiThread;
    }

    public void setByMultiThread(boolean byMultiThread) {
        this.byMultiThread = byMultiThread;
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

    public String getFileDir() {
        return fileDir;
    }

    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }


    public int getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(int currentStatus) {
        this.currentStatus = currentStatus;
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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    protected DownloadInfo(Parcel in) {
        resKey = in.readString();
        requestUrl = in.readString();
        targetUrl = in.readString();
        priority = in.readInt();
        byMultiThread = in.readByte() != 0;
        onlyWifiDownload = in.readByte() != 0;
        wifiAutoRetry = in.readByte() != 0;
        permitRetryInMobileData = in.readByte() != 0;
        permitRetryInvalidFileTask = in.readByte() != 0;
        permitRecoverTask = in.readByte() != 0;
        fileDir = in.readString();
        fileName = in.readString();

        totalSize = in.readLong();
        currentSize = in.readLong();
        progress = in.readInt();
        currentStatus = in.readInt();

        responseCode = in.readInt();
        failureCode = in.readInt();
        contentType = in.readString();
        tag = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(resKey);
        dest.writeString(requestUrl);
        dest.writeString(targetUrl);
        dest.writeInt(priority);
        dest.writeByte((byte) (byMultiThread ? 1 : 0));
        dest.writeByte((byte) (onlyWifiDownload ? 1 : 0));
        dest.writeByte((byte) (wifiAutoRetry ? 1 : 0));
        dest.writeByte((byte) (permitRetryInMobileData ? 1 : 0));
        dest.writeByte((byte) (permitRetryInvalidFileTask ? 1 : 0));
        dest.writeByte((byte) (permitRecoverTask ? 1 : 0));
        dest.writeString(fileDir);
        dest.writeString(fileName);

        dest.writeLong(totalSize);
        dest.writeLong(currentSize);
        dest.writeInt(progress);
        dest.writeInt(currentStatus);

        dest.writeInt(responseCode);
        dest.writeInt(failureCode);
        dest.writeString(contentType);
        dest.writeString(tag);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DownloadInfo> CREATOR = new Creator<DownloadInfo>() {
        @Override
        public DownloadInfo createFromParcel(Parcel in) {
            return new DownloadInfo(in);
        }

        @Override
        public DownloadInfo[] newArray(int size) {
            return new DownloadInfo[size];
        }
    };
}
