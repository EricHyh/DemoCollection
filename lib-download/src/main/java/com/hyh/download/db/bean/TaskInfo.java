package com.hyh.download.db.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.hyh.download.DownloadInfo;
import com.hyh.download.FileRequest;
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

    @NotNull
    @Column(nameInDb = "requestUrl")
    private String requestUrl;

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

    @Column(nameInDb = "progress")
    private int progress;

    @Column(nameInDb = "byMultiThread")
    private boolean byMultiThread;

    @Column(nameInDb = "rangeNum")
    private int rangeNum;

    @Column(nameInDb = "currentSize")
    private long currentSize;

    @Column(nameInDb = "totalSize")
    private long totalSize;

    @Column(nameInDb = "currentStatus")
    private int currentStatus;

    @Column(nameInDb = "wifiAutoRetry")
    private boolean wifiAutoRetry;

    @Column(nameInDb = "permitMobileDataRetry")
    private boolean permitMobileDataRetry;

    @Column(nameInDb = "permitRetryIfInterrupt")
    private boolean permitRetryIfInterrupt;

    @Column(nameInDb = "responseCode")
    private int responseCode;

    @Column(nameInDb = "eTag")
    private String eTag;

    @Column(nameInDb = "tag")
    private String tag;

    public TaskInfo() {
    }


    protected TaskInfo(Parcel in) {
        id = in.readLong();
        resKey = in.readString();
        requestUrl = in.readString();
        cacheRequestUrl = in.readString();
        cacheTargetUrl = in.readString();
        versionCode = in.readInt();
        priority = in.readInt();
        fileDir = in.readString();
        filePath = in.readString();
        progress = in.readInt();
        byMultiThread = in.readByte() != 0;
        rangeNum = in.readInt();
        currentSize = in.readLong();
        totalSize = in.readLong();
        currentStatus = in.readInt();
        wifiAutoRetry = in.readByte() != 0;
        permitMobileDataRetry = in.readByte() != 0;
        permitRetryIfInterrupt = in.readByte() != 0;
        responseCode = in.readInt();
        eTag = in.readString();
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
        dest.writeString(cacheRequestUrl);
        dest.writeString(cacheTargetUrl);
        dest.writeInt(versionCode);
        dest.writeInt(priority);
        dest.writeString(fileDir);
        dest.writeString(filePath);
        dest.writeInt(progress);
        dest.writeByte((byte) (byMultiThread ? 1 : 0));
        dest.writeInt(rangeNum);
        dest.writeLong(currentSize);
        dest.writeLong(totalSize);
        dest.writeInt(currentStatus);
        dest.writeByte((byte) (wifiAutoRetry ? 1 : 0));
        dest.writeByte((byte) (permitMobileDataRetry ? 1 : 0));
        dest.writeByte((byte) (permitRetryIfInterrupt ? 1 : 0));
        dest.writeInt(responseCode);
        dest.writeString(eTag);
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

    public boolean isPermitRetryIfInterrupt() {
        return permitRetryIfInterrupt;
    }

    public void setPermitRetryIfInterrupt(boolean permitRetryIfInterrupt) {
        this.permitRetryIfInterrupt = permitRetryIfInterrupt;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getETag() {
        return eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
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

    public FileRequest toFileRequest() {
        return new FileRequest.Builder().build();
    }
}
