package com.hyh.tools.download.db.bean;

import com.hyh.tools.download.bean.State;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.
/**
 * Entity mapped to table "TASK_DBINFO".
 */
public class TaskDBInfo {

    private Long id;
    private String resKey;
    private String url;
    private Integer currentStatus;
    private Integer progress;
    private Integer versionCode;
    private Integer responseCode;
    private Integer rangeNum;
    private Long totalSize;
    private Long currentSize;
    private Long timeMillis;
    private String packageName;
    private String filePath;
    private Boolean wifiAutoRetry;
    private String tagStr;
    private String tagClassName;

    public TaskDBInfo() {
    }

    public TaskDBInfo(Long id) {
        this.id = id;
    }

    public TaskDBInfo(Long id, String resKey, String url, Integer currentStatus, Integer progress, Integer versionCode, Integer responseCode, Integer rangeNum, Long totalSize, Long currentSize, Long timeMillis, String packageName, String filePath , Boolean wifiAutoRetry, String tagStr, String tagClassName) {
        this.id = id;
        this.resKey = resKey;
        this.url = url;
        this.currentStatus = currentStatus;
        this.progress = progress;
        this.versionCode = versionCode;
        this.responseCode = responseCode;
        this.rangeNum = rangeNum;
        this.totalSize = totalSize;
        this.currentSize = currentSize;
        this.timeMillis = timeMillis;
        this.packageName = packageName;
        this.filePath = filePath;
        this.wifiAutoRetry = wifiAutoRetry;
        this.tagStr = tagStr;
        this.tagClassName = tagClassName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(Integer currentStatus) {
        this.currentStatus = currentStatus;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public Integer getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(Integer versionCode) {
        this.versionCode = versionCode;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public Integer getRangeNum() {
        return rangeNum;
    }

    public void setRangeNum(Integer rangeNum) {
        this.rangeNum = rangeNum;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public Long getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(Long currentSize) {
        this.currentSize = currentSize;
    }

    public Long getTimeMillis() {
        return timeMillis;
    }

    public void setTimeMillis(Long timeMillis) {
        this.timeMillis = timeMillis;
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

    public Boolean getWifiAutoRetry() {
        return wifiAutoRetry;
    }

    public void setWifiAutoRetry(Boolean wifiAutoRetry) {
        this.wifiAutoRetry = wifiAutoRetry;
    }

    public String getTagStr() {
        return tagStr;
    }

    public void setTagStr(String tagStr) {
        this.tagStr = tagStr;
    }

    public String getTagClassName() {
        return tagClassName;
    }

    public void setTagClassName(String tagClassName) {
        this.tagClassName = tagClassName;
    }

    public void clear() {
        id = null;
        resKey = null;
        url = null;
        currentStatus = State.NONE;
        progress = 0;
        rangeNum = 0;
        versionCode = 0;
        responseCode = 0;
        totalSize = 0L;
        currentSize = 0L;
        timeMillis = 0L;
        packageName = null;
        filePath = null;
        wifiAutoRetry = null;
        tagStr = null;
        tagClassName = null;
    }
}