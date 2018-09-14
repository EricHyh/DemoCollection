package com.hyh.tools.download.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2017/3/14.
 */

public class TaskInfo implements Parcelable {


    private String resKey;

    private String url;

    private String packageName;

    private int versionCode;

    private String filePath;

    private int progress;

    private int rangeNum;

    private long currentSize;

    private long[] startPositions;

    private long[] endPositions;

    private long totalSize;

    private int currentStatus;

    private boolean wifiAutoRetry;

    protected int responseCode;

    private TagInfo tagInfo;

    public TaskInfo() {
    }

    protected TaskInfo(Parcel in) {
        resKey = in.readString();
        url = in.readString();
        packageName = in.readString();
        versionCode = in.readInt();
        filePath = in.readString();
        progress = in.readInt();
        rangeNum = in.readInt();
        currentSize = in.readLong();
        startPositions = in.createLongArray();
        endPositions = in.createLongArray();
        totalSize = in.readLong();
        currentStatus = in.readInt();
        wifiAutoRetry = in.readByte() != 0;
        responseCode = in.readInt();
        tagInfo = in.readParcelable(TagInfo.class.getClassLoader());
    }

    public static final Creator<TaskInfo> CREATOR = new Creator<TaskInfo>() {
        @Override
        public TaskInfo createFromParcel(Parcel in) {
            return new TaskInfo(in);
        }

        @Override
        public TaskInfo[] newArray(int size) {
            return new TaskInfo[size];
        }
    };


    @Override
    public String toString() {
        return "TaskInfo{" +
                "resKey='" + resKey + '\'' +
                ", url='" + url + '\'' +
                ", packageName='" + packageName + '\'' +
                ", versionCode=" + versionCode +
                ", filePath='" + filePath + '\'' +
                ", progress=" + progress +
                ", rangeNum=" + rangeNum +
                ", currentSize=" + currentSize +
                ", totalSize=" + totalSize +
                ", currentStatus=" + currentStatus +
                ", wifiAutoRetry=" + wifiAutoRetry +
                ", responseCode=" + responseCode +
                ", tagInfo=" + tagInfo +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskInfo that = (TaskInfo) o;
        return resKey.equals(that.resKey);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(resKey);
        dest.writeString(url);
        dest.writeString(packageName);
        dest.writeInt(versionCode);
        dest.writeString(filePath);
        dest.writeInt(progress);
        dest.writeInt(rangeNum);
        dest.writeLong(currentSize);
        dest.writeLongArray(startPositions);
        dest.writeLongArray(endPositions);
        dest.writeLong(totalSize);
        dest.writeInt(currentStatus);
        dest.writeByte((byte) (wifiAutoRetry ? 1 : 0));
        dest.writeInt(responseCode);
        dest.writeParcelable(tagInfo, flags);
    }

    public String getResKey() {
        return resKey;
    }

    public void setResKey(String resKey) {
        this.resKey = resKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getRangeNum() {
        return rangeNum;
    }

    public void setRangeNum(int rangeNum) {
        this.rangeNum = rangeNum;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public long[] getStartPositions() {
        return startPositions;
    }

    public void setStartPositions(long[] startPositions) {
        this.startPositions = startPositions;
    }

    public long[] getEndPositions() {
        return endPositions;
    }

    public void setEndPositions(long[] endPositions) {
        this.endPositions = endPositions;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public int getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(int currentStatus) {
        this.currentStatus = currentStatus;
    }

    public boolean isWifiAutoRetry() {
        return wifiAutoRetry;
    }

    public void setWifiAutoRetry(boolean wifiAutoRetry) {
        this.wifiAutoRetry = wifiAutoRetry;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public TagInfo getTagInfo() {
        return tagInfo;
    }

    public void setTagInfo(TagInfo tagInfo) {
        this.tagInfo = tagInfo;
    }

}
