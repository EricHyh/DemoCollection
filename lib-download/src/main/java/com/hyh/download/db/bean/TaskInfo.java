package com.hyh.download.db.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.hyh.download.DownloadInfo;
import com.hyh.download.db.annotation.Column;
import com.hyh.download.db.annotation.Id;
import com.hyh.download.db.annotation.NotNull;
import com.hyh.download.db.annotation.Unique;

/**
 * Created by Administrator on 2017/3/14.
 */

public class TaskInfo implements Parcelable {

    @Id
    @Column(nameInDb = "_id")
    private long id = -1;

    @NotNull
    @Unique
    @Column(nameInDb = "resKey")
    private String resKey;

    private String requestUrl;

    private String targetUrl;

    @Column(nameInDb = "cacheRequestUrl")
    private String cacheRequestUrl;

    @Column(nameInDb = "cacheTargetUrl")
    private String cacheTargetUrl;

    @Column(nameInDb = "versionCode")
    private int versionCode;

    @Column(nameInDb = "priority")
    private int priority;

    @NotNull
    @Column(nameInDb = "fileDir")
    private String fileDir;

    @Column(nameInDb = "filePath")
    private String filePath;

    @Column(nameInDb = "byMultiThread")
    private boolean byMultiThread;

    @Column(nameInDb = "rangeNum")
    private int rangeNum;

    @Column(nameInDb = "totalSize")
    private long totalSize;

    @Column(nameInDb = "currentSize")
    private volatile long currentSize;

    @Column(nameInDb = "progress")
    private volatile int progress;

    @Column(nameInDb = "currentStatus")
    private volatile int currentStatus;

    @Column(nameInDb = "wifiAutoRetry")
    private boolean wifiAutoRetry;

    @Column(nameInDb = "permitRetryInMobileData")
    private boolean permitRetryInMobileData;

    @Column(nameInDb = "permitRetryInvalidFileTask")
    private boolean permitRetryInvalidFileTask;

    @Column(nameInDb = "permitRecoverTask")
    private boolean permitRecoverTask;

    @Column(nameInDb = "responseCode")
    private int responseCode;

    @Column(nameInDb = "failureCode")
    private int failureCode;

    @Column(nameInDb = "eTag")
    private String eTag;

    @Column(nameInDb = "lastModified")
    private String lastModified;

    @Column(nameInDb = "updateTimeMillis")
    private long updateTimeMillis;

    @Column(nameInDb = "tag")
    private String tag;

    public TaskInfo() {
    }


    protected TaskInfo(Parcel in) {
        id = in.readLong();
        resKey = in.readString();
        requestUrl = in.readString();
        targetUrl = in.readString();
        cacheRequestUrl = in.readString();
        cacheTargetUrl = in.readString();
        versionCode = in.readInt();
        priority = in.readInt();
        fileDir = in.readString();
        filePath = in.readString();
        byMultiThread = in.readByte() != 0;
        rangeNum = in.readInt();
        totalSize = in.readLong();
        currentSize = in.readLong();
        progress = in.readInt();
        currentStatus = in.readInt();
        wifiAutoRetry = in.readByte() != 0;
        permitRetryInMobileData = in.readByte() != 0;
        permitRetryInvalidFileTask = in.readByte() != 0;
        permitRecoverTask = in.readByte() != 0;
        responseCode = in.readInt();
        failureCode = in.readInt();
        eTag = in.readString();
        lastModified = in.readString();
        updateTimeMillis = in.readLong();
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
        dest.writeLong(id);
        dest.writeString(resKey);
        dest.writeString(requestUrl);
        dest.writeString(targetUrl);
        dest.writeString(cacheRequestUrl);
        dest.writeString(cacheTargetUrl);
        dest.writeInt(versionCode);
        dest.writeInt(priority);
        dest.writeString(fileDir);
        dest.writeString(filePath);
        dest.writeByte((byte) (byMultiThread ? 1 : 0));
        dest.writeInt(rangeNum);
        dest.writeLong(totalSize);
        dest.writeLong(currentSize);
        dest.writeInt(progress);
        dest.writeInt(currentStatus);
        dest.writeByte((byte) (wifiAutoRetry ? 1 : 0));
        dest.writeByte((byte) (permitRetryInMobileData ? 1 : 0));
        dest.writeByte((byte) (permitRetryInvalidFileTask ? 1 : 0));
        dest.writeByte((byte) (permitRecoverTask ? 1 : 0));
        dest.writeInt(responseCode);
        dest.writeInt(failureCode);
        dest.writeString(eTag);
        dest.writeString(lastModified);
        dest.writeLong(updateTimeMillis);
        dest.writeString(tag);
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
        downloadInfo.setVersionCode(this.versionCode);
        downloadInfo.setPriority(this.priority);
        downloadInfo.setFilePath(this.filePath);
        downloadInfo.setCurrentStatus(this.currentStatus);
        downloadInfo.setTotalSize(this.totalSize);
        downloadInfo.setCurrentSize(this.currentSize);
        downloadInfo.setProgress(this.progress);
        downloadInfo.setSpeed(0);
        downloadInfo.setTag(this.tag);
        return downloadInfo;
    }
}
