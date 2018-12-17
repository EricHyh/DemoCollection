package com.hyh.download.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2017/3/14.
 */

public class TaskInfo implements Parcelable {

    private String resKey;

    private String requestUrl;

    private String cacheRequestUrl;

    private String cacheTargetUrl;

    private int versionCode;

    private int priority;

    private String fileDir;

    private String filePath;

    private int progress;

    private boolean byMultiThread;

    private int rangeNum;

    private long currentSize;

    private long[] startPositions;

    private long[] endPositions;

    private long totalSize;

    private int currentStatus;

    private boolean wifiAutoRetry;

    private boolean permitMobileDataRetry;

    private int responseCode;

    private String eTag;

    private String tag;

    public TaskInfo() {
    }

    protected TaskInfo(Parcel in) {
        resKey = in.readString();
        requestUrl = in.readString();
        versionCode = in.readInt();
        fileDir = in.readString();
        filePath = in.readString();
        progress = in.readInt();
        byMultiThread = in.readByte() != 0;
        rangeNum = in.readInt();
        currentSize = in.readLong();
        startPositions = in.createLongArray();
        endPositions = in.createLongArray();
        totalSize = in.readLong();
        currentStatus = in.readInt();
        wifiAutoRetry = in.readByte() != 0;
        permitMobileDataRetry = in.readByte() != 0;
        responseCode = in.readInt();
        tag = in.readString();
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
                ", requestUrl='" + requestUrl + '\'' +
                ", versionCode=" + versionCode +
                ", fileDir='" + fileDir + '\'' +
                ", filePath='" + filePath + '\'' +
                ", progress=" + progress +
                ", byMultiThread=" + byMultiThread +
                ", rangeNum=" + rangeNum +
                ", currentSize=" + currentSize +
                ", totalSize=" + totalSize +
                ", currentStatus=" + currentStatus +
                ", wifiAutoRetry=" + wifiAutoRetry +
                ", permitMobileDataRetry=" + permitMobileDataRetry +
                ", responseCode=" + responseCode +
                ", tag=" + tag +
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
        dest.writeString(requestUrl);
        dest.writeInt(versionCode);
        dest.writeString(filePath);
        dest.writeInt(progress);
        dest.writeByte((byte) (byMultiThread ? 1 : 0));
        dest.writeInt(rangeNum);
        dest.writeLong(currentSize);
        dest.writeLongArray(startPositions);
        dest.writeLongArray(endPositions);
        dest.writeLong(totalSize);
        dest.writeInt(currentStatus);
        dest.writeByte((byte) (wifiAutoRetry ? 1 : 0));
        dest.writeByte((byte) (permitMobileDataRetry ? 1 : 0));
        dest.writeInt(responseCode);
        dest.writeString(tag);
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

    public String getCacheRequestUrl() {
        return cacheRequestUrl;
    }

    public void setCacheRequestUrl(String cacheRequestUrl) {
        this.cacheRequestUrl = cacheRequestUrl;
    }

    public String getCacheTargetUrl() {
        return cacheTargetUrl;
    }

    public void setCacheTargetUrl(String cacheTargetUrl) {
        this.cacheTargetUrl = cacheTargetUrl;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getFileDir() {
        return fileDir;
    }

    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
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

    public boolean isPermitMobileDataRetry() {
        return permitMobileDataRetry;
    }

    public void setPermitMobileDataRetry(boolean permitMobileDataRetry) {
        this.permitMobileDataRetry = permitMobileDataRetry;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public DownloadInfo toDownloadInfo() {
        return new DownloadInfo();
    }
}
