package com.hyh.download.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Administrator
 * @description
 * @data 2018/12/17
 */

public class DownloadInfo implements Parcelable{

    private String resKey;

    private String requestUrl;

    private String locationUrl;

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

    protected DownloadInfo(Parcel in) {
        resKey = in.readString();
        requestUrl = in.readString();
        locationUrl = in.readString();
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

    public String getResKey() {
        return resKey;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public String getLocationUrl() {
        return locationUrl;
    }

    public int getVersionCode() {
        return versionCode;
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

    public int getCurrentStatus() {
        return currentStatus;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public int getProgress() {
        return progress;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(resKey);
        dest.writeString(requestUrl);
        dest.writeString(locationUrl);
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
