package com.hyh.download;


import android.text.TextUtils;

/**
 * Created by Administrator on 2017/3/9.
 */

public class FileRequest {

    private String resKey;

    private String url;

    private boolean needVerifyUrl;

    private boolean byMultiThread;

    private boolean wifiAutoRetry;

    private boolean permitRetryInMobileData;

    private boolean permitRetryInvalidFileTask;

    private boolean permitRecoverTask;

    private boolean forceDownload;

    private String fileDir;

    private String fileName;

    private FileChecker fileChecker;

    private String tag;

    private FileRequest() {
    }

    public String key() {
        return resKey;
    }

    public String url() {
        return url;
    }

    public boolean needVerifyUrl() {
        return needVerifyUrl;
    }

    public boolean byMultiThread() {
        return byMultiThread;
    }

    public boolean wifiAutoRetry() {
        return wifiAutoRetry;
    }

    public boolean permitRetryInMobileData() {
        return permitRetryInMobileData;
    }

    public boolean permitRetryInvalidFileTask() {
        return permitRetryInvalidFileTask;
    }

    public boolean permitRecoverTask() {
        return permitRecoverTask;
    }

    public boolean forceDownload() {
        return forceDownload;
    }

    public String fileDir() {
        return fileDir;
    }

    public String fileName() {
        return fileName;
    }

    public FileChecker fileChecker() {
        return fileChecker;
    }

    public String tag() {
        return tag;
    }

    public static class Builder {

        private String resKey;

        private String url;

        private boolean needVerifyUrl = false;

        private boolean byMultiThread = false;

        private boolean wifiAutoRetry = false;

        private boolean permitRetryInMobileData = false;

        private boolean permitRetryInvalidFileTask = false;

        private boolean permitRecoverTask = false;

        private boolean forceDownload = false;

        private String fileDir;

        private String fileName;

        private String tag;

        private FileChecker fileChecker;

        public FileRequest build() {
            FileRequest fileRequest = new FileRequest();
            if (TextUtils.isEmpty(this.resKey)) {
                fileRequest.resKey = this.url;
            } else {
                fileRequest.resKey = this.resKey;
            }
            fileRequest.url = this.url;
            fileRequest.needVerifyUrl = this.needVerifyUrl;
            fileRequest.byMultiThread = this.byMultiThread;
            fileRequest.wifiAutoRetry = this.wifiAutoRetry;
            fileRequest.permitRetryInMobileData = this.permitRetryInMobileData;
            fileRequest.permitRetryInvalidFileTask = this.permitRetryInvalidFileTask;
            fileRequest.permitRecoverTask = this.permitRecoverTask;
            fileRequest.forceDownload = this.forceDownload;
            fileRequest.fileDir = fileDir;
            fileRequest.fileName = fileName;
            fileRequest.tag = this.tag;
            fileRequest.fileChecker = this.fileChecker;
            return fileRequest;
        }

        public Builder key(String resKey) {
            this.resKey = resKey;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder needVerifyUrl(boolean needVerifyUrl) {
            this.needVerifyUrl = needVerifyUrl;
            return this;
        }

        public Builder byMultiThread(boolean byMultiThread) {
            this.byMultiThread = byMultiThread;
            return this;
        }

        public Builder wifiAutoRetry(boolean wifiAutoRetry) {
            this.wifiAutoRetry = wifiAutoRetry;
            return this;
        }

        public Builder permitRetryInMobileData(boolean permitRetryInMobileData) {
            this.permitRetryInMobileData = permitRetryInMobileData;
            return this;
        }

        public Builder permitRetryInvalidFileTask(boolean permitRetryInvalidFileTask) {
            this.permitRetryInvalidFileTask = permitRetryInvalidFileTask;
            return this;
        }

        public Builder permitRecoverTask(boolean permitRecoverTask) {
            this.permitRecoverTask = permitRecoverTask;
            return this;
        }

        public Builder forceDownload(boolean isForceDownload) {
            this.forceDownload = isForceDownload;
            return this;
        }

        public Builder fileDir(String fileDir) {
            this.fileDir = fileDir;
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder fileChecker(FileChecker fileChecker) {
            this.fileChecker = fileChecker;
            return this;
        }
    }
}
