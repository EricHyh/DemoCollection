package com.eric.hyh.tools.download.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.eric.hyh.tools.download.internal.db.bean.TaskDBInfo;
import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Created by Administrator on 2017/3/14.
 */

public class TaskInfo<T> implements Parcelable {


    protected String resKey;

    protected String url;

    protected String packageName;

    protected int versionCode;

    protected String filePath;

    protected int progress;

    protected long currentSize;

    protected long totalSize;

    protected int currentStatus;

    protected boolean byService;

    protected boolean wifiAutoRetry;

    protected String expand;

    protected String tagJson;

    protected String tagClassName;

    protected int code;

    private T tag;

    private Type tagType;

    public T getTag() {
        return tag;
    }

    public void setTag(T tag) {
        this.tag = tag;
    }

    public Type getTagType() {
        return tagType;
    }

    public void setTagType(Type tagType) {
        this.tagType = tagType;
    }

    public TaskInfo() {
    }

    protected TaskInfo(Parcel in) {
        resKey = in.readString();
        url = in.readString();
        packageName = in.readString();
        versionCode = in.readInt();
        filePath = in.readString();
        progress = in.readInt();
        currentSize = in.readLong();
        totalSize = in.readLong();
        currentStatus = in.readInt();
        byService = in.readByte() != 0;
        wifiAutoRetry = in.readByte() != 0;
        expand = in.readString();
        tagJson = in.readString();
        tagClassName = in.readString();
        code = in.readInt();
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
                ", currentSize=" + currentSize +
                ", totalSize=" + totalSize +
                ", currentStatus=" + currentStatus +
                ", byService=" + byService +
                ", wifiAutoRetry=" + wifiAutoRetry +
                ", expand='" + expand + '\'' +
                ", tagJson='" + tagJson + '\'' +
                ", tagClassName='" + tagClassName + '\'' +
                ", code=" + code +
                ", tag=" + tag +
                ", tagType=" + tagType +
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
        dest.writeLong(currentSize);
        dest.writeLong(totalSize);
        dest.writeInt(currentStatus);
        dest.writeByte((byte) (byService ? 1 : 0));
        dest.writeByte((byte) (wifiAutoRetry ? 1 : 0));
        dest.writeString(expand);
        dest.writeString(tagJson);
        dest.writeString(tagClassName);
        dest.writeInt(code);
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

    public boolean isByService() {
        return byService;
    }

    public void setByService(boolean byService) {
        this.byService = byService;
    }

    public boolean isWifiAutoRetry() {
        return wifiAutoRetry;
    }

    public void setWifiAutoRetry(boolean wifiAutoRetry) {
        this.wifiAutoRetry = wifiAutoRetry;
    }

    public String getExpand() {
        return expand;
    }

    public void setExpand(String expand) {
        this.expand = expand;
    }

    public String getTagJson() {
        return tagJson;
    }

    public void setTagJson(String tagJson) {
        this.tagJson = tagJson;
    }

    public String getTagClassName() {
        return tagClassName;
    }

    public void setTagClassName(String tagClassName) {
        this.tagClassName = tagClassName;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public static <T> TaskInfo<T> taskDBInfo2TaskInfo(TaskDBInfo taskDBInfo, Type type, Gson gson) {
        TaskInfo<T> taskInfo = new TaskInfo<>();
        taskInfo.setResKey(taskDBInfo.getResKey());
        taskInfo.setUrl(taskDBInfo.getUrl());
        taskInfo.setPackageName(taskDBInfo.getPackageName());
        taskInfo.setFilePath(taskDBInfo.getFilePath());
        taskInfo.setVersionCode(taskDBInfo.getVersionCode() == null ? -1 : taskDBInfo.getVersionCode());
        taskInfo.setProgress(taskDBInfo.getProgress() == null ? 0 : taskDBInfo.getProgress());
        taskInfo.setTotalSize(taskDBInfo.getTotalSize() == null ? 0 : taskDBInfo.getTotalSize());
        taskInfo.setCurrentSize(taskDBInfo.getCurrentSize() == null ? 0 : taskDBInfo.getCurrentSize());
        taskInfo.setCurrentStatus(taskDBInfo.getCurrentStatus() == null ? State.NONE : taskDBInfo.getCurrentStatus());
        taskInfo.setByService(taskDBInfo.getByService() == null ? true : taskDBInfo.getByService());
        taskInfo.setWifiAutoRetry(taskDBInfo.getWifiAutoRetry() == null ? true : taskDBInfo.getWifiAutoRetry());
        taskInfo.setCode(taskDBInfo.getResponseCode() == null ? 0 : taskDBInfo.getResponseCode());
        taskInfo.setExpand(taskDBInfo.getExpand());
        taskInfo.setTagClassName(taskDBInfo.getTagClassName());
        String tagJson = taskDBInfo.getTagJson();
        taskInfo.setTagJson(tagJson);
        taskInfo.setTagType(type);
        if (!TextUtils.isEmpty(tagJson)) {
            try {
                T tag = gson.fromJson(tagJson, type);
                taskInfo.setTag(tag);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return taskInfo;
    }

    public static TaskInfo taskDBInfo2TaskInfo(TaskDBInfo taskDBInfo, Gson gson) {
        TaskInfo<Object> taskInfo = new TaskInfo<>();
        taskInfo.setResKey(taskDBInfo.getResKey());
        taskInfo.setUrl(taskDBInfo.getUrl());
        taskInfo.setPackageName(taskDBInfo.getPackageName());
        taskInfo.setFilePath(taskDBInfo.getFilePath());
        taskInfo.setVersionCode(taskDBInfo.getVersionCode() == null ? -1 : taskDBInfo.getVersionCode());
        taskInfo.setProgress(taskDBInfo.getProgress() == null ? 0 : taskDBInfo.getProgress());
        taskInfo.setTotalSize(taskDBInfo.getTotalSize() == null ? 0 : taskDBInfo.getTotalSize());
        taskInfo.setCurrentSize(taskDBInfo.getCurrentSize() == null ? 0 : taskDBInfo.getCurrentSize());
        taskInfo.setCurrentStatus(taskDBInfo.getCurrentStatus() == null ? State.NONE : taskDBInfo.getCurrentStatus());
        taskInfo.setByService(taskDBInfo.getByService() == null ? true : taskDBInfo.getByService());
        taskInfo.setWifiAutoRetry(taskDBInfo.getWifiAutoRetry() == null ? true : taskDBInfo.getWifiAutoRetry());
        taskInfo.setCode(taskDBInfo.getResponseCode() == null ? 0 : taskDBInfo.getResponseCode());
        taskInfo.setExpand(taskDBInfo.getExpand());
        String tagJson = taskDBInfo.getTagJson();
        String tagClassName = taskDBInfo.getTagClassName();
        taskInfo.setTagJson(tagJson);
        taskInfo.setTagClassName(tagClassName);
        if (!TextUtils.isEmpty(tagJson) && !TextUtils.isEmpty(tagClassName)) {
            try {
                Class<?> clazz = Class.forName(tagClassName);
                taskInfo.setTagType(clazz);
                Object fromJson = gson.fromJson(tagJson, clazz);
                taskInfo.setTag(fromJson);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return taskInfo;
    }


    public static TaskDBInfo taskInfo2TaskDBInfo(TaskInfo taskInfo) {
        TaskDBInfo taskDBInfo = new TaskDBInfo();
        taskDBInfo.setResKey(taskInfo.getResKey());
        taskDBInfo.setUrl(taskInfo.getUrl());
        taskDBInfo.setFilePath(taskInfo.getFilePath());
        taskDBInfo.setExpand(taskInfo.getExpand());
        taskDBInfo.setCurrentSize(taskInfo.getCurrentSize());
        taskDBInfo.setCurrentStatus(taskInfo.getCurrentStatus());
        taskDBInfo.setTotalSize(taskInfo.getTotalSize());
        taskDBInfo.setByService(taskInfo.isByService());
        taskDBInfo.setPackageName(taskInfo.getPackageName());
        taskDBInfo.setVersionCode(taskInfo.getVersionCode());
        taskDBInfo.setTagClassName(taskInfo.getTagClassName());
        taskDBInfo.setProgress(taskInfo.getProgress());
        taskDBInfo.setWifiAutoRetry(taskInfo.isWifiAutoRetry());
        taskDBInfo.setTagJson(taskInfo.getTagJson());
        taskDBInfo.setTime(System.currentTimeMillis());
        return taskDBInfo;
    }

}
