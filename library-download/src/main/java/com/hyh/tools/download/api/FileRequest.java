package com.hyh.tools.download.api;


import com.hyh.tools.download.bean.Command;

/**
 * Created by Administrator on 2017/3/9.
 */

public class FileRequest {

    private String resKey;

    private String url;

    private boolean byMultiThread;

    private boolean wifiAutoRetry;

    private String packageName;

    private long fileSize;

    private int versionCode;

    private int command;

    private Object tag;

    private String tagClassName;

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

    public Object tag() {
        return tag;
    }

    public boolean byMultiThread() {
        return byMultiThread;
    }

    public boolean wifiAutoRetry() {
        return wifiAutoRetry;
    }

    public String packageName() {
        return packageName;
    }

    public int command() {
        return command;
    }

    public String tagClassName() {
        return tagClassName;
    }

    public long fileSize() {
        return fileSize;
    }

    public int versionCode() {
        return versionCode;
    }

    void changeCommand(int command) {
        this.command = command;
    }

    public void setByMultiThread(boolean byMultiThread) {
        this.byMultiThread = byMultiThread;
    }


    public static class Builder {

        private String resKey;

        private String url;

        private boolean byMultiThread = false;

        private boolean wifiAutoRetry = false;

        private String packageName;

        private int command = Command.START;

        private long fileSize;

        private int versionCode = -1;

        private Object tag;

        public Builder() {
        }

        public FileRequest build() {
            FileRequest fileRequest = new FileRequest();
            fileRequest.resKey = this.resKey;
            fileRequest.url = this.url;
            fileRequest.byMultiThread = this.byMultiThread;
            fileRequest.wifiAutoRetry = this.wifiAutoRetry;
            fileRequest.packageName = this.packageName;
            fileRequest.command = this.command;
            fileRequest.tag = this.tag;
            if (tag != null) {
                fileRequest.tagClassName = tag.getClass().getName();
            }
            fileRequest.fileSize = this.fileSize;
            fileRequest.versionCode = this.versionCode;
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

        public Builder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public Builder tag(Object tag) {
            this.tag = tag;
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

        public Builder command(int command) {
            this.command = command;
            return this;
        }

        public Builder fileSize(long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public Builder versionCode(int versionCode) {
            this.versionCode = versionCode;
            return this;
        }
    }
}
