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

    private int versionCode;

    private int priority;

    private String filePath;

    private int currentStatus;

    private long currentSize;

    private long totalSize;

    private int progress;

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

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(int currentStatus) {
        this.currentStatus = currentStatus;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected DownloadInfo(Parcel in) {
        resKey = in.readString();
        requestUrl = in.readString();
        targetUrl = in.readString();
        versionCode = in.readInt();
        priority = in.readInt();
        filePath = in.readString();
        currentStatus = in.readInt();
        currentSize = in.readLong();
        totalSize = in.readLong();
        progress = in.readInt();
        tag = in.readString();
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(resKey);
        dest.writeString(requestUrl);
        dest.writeString(targetUrl);
        dest.writeInt(versionCode);
        dest.writeInt(priority);
        dest.writeString(filePath);
        dest.writeInt(currentStatus);
        dest.writeLong(currentSize);
        dest.writeLong(totalSize);
        dest.writeInt(progress);
        dest.writeString(tag);
    }
}
