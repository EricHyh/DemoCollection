package com.hyh.download;


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

    private int versionCode;

    private String fileDir;

    private String filePath;

    private FileChecker fileChecker;

    private String tag;

    private FileRequest() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileRequest that = (FileRequest) o;
        return resKey.equals(that.resKey);
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

    public int versionCode() {
        return versionCode;
    }

    public String fileDir() {
        return fileDir;
    }

    public String filePath() {
        return filePath;
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

        private int versionCode = -1;

        private String fileDir;

        private String filePath;

        private String tag;

        private FileChecker fileChecker;

        public Builder() {

        }

        public FileRequest build() {
            FileRequest fileRequest = new FileRequest();
            fileRequest.resKey = this.resKey;
            fileRequest.url = this.url;
            fileRequest.needVerifyUrl = this.needVerifyUrl;
            fileRequest.byMultiThread = this.byMultiThread;
            fileRequest.wifiAutoRetry = this.wifiAutoRetry;
            fileRequest.permitRetryInMobileData = this.permitRetryInMobileData;
            fileRequest.permitRetryInvalidFileTask = this.permitRetryInvalidFileTask;
            fileRequest.permitRecoverTask = this.permitRecoverTask;
            fileRequest.forceDownload = this.forceDownload;
            fileRequest.versionCode = this.versionCode;
            fileRequest.fileDir = fileDir;
            fileRequest.filePath = filePath;
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

        public Builder versionCode(int versionCode) {
            this.versionCode = versionCode;
            return this;
        }

        public Builder fileDir(String fileDir) {
            this.fileDir = fileDir;
            return this;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
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
