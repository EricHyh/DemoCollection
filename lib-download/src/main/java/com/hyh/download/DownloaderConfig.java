package com.hyh.download;

/**
 * @author Administrator
 * @description
 * @data 2018/12/12
 */

public class DownloaderConfig {

    private boolean byService;

    private boolean independentProcess;

    private int maxSyncDownloadNum;

    private String defaultFileDir;

    private FileChecker globalFileChecker;

    private int threadMode;

    private DownloaderConfig() {
    }

    public boolean isByService() {
        return byService;
    }

    public boolean isIndependentProcess() {
        return independentProcess;
    }

    public int getMaxSyncDownloadNum() {
        return maxSyncDownloadNum;
    }

    public String getDefaultFileDir() {
        return defaultFileDir;
    }

    public FileChecker getGlobalFileChecker() {
        return globalFileChecker;
    }

    public int getThreadMode() {
        return threadMode;
    }

    public static class Builder {

        private boolean byService = false;

        private boolean independentProcess = false;

        private int maxSyncDownloadNum = 4;

        private String defaultFileDir;

        private FileChecker globalFileChecker;

        private int threadMode = ThreadMode.UI;

        public Builder() {
        }

        public Builder byService(boolean byService) {
            //this.byService = byService;暂不支持开服务
            return this;
        }

        public Builder independentProcess(boolean independentProcess) {
            this.independentProcess = independentProcess;
            return this;
        }

        public Builder maxSyncDownloadNum(int maxSyncDownloadNum) {
            this.maxSyncDownloadNum = maxSyncDownloadNum;
            return this;
        }

        public Builder defaultFileDir(String defaultFileDir) {
            this.defaultFileDir = defaultFileDir;
            return this;
        }

        public Builder globalFileChecker(FileChecker globalFileChecker) {
            this.globalFileChecker = globalFileChecker;
            return this;
        }

        public Builder threadMode(int threadMode) {
            this.threadMode = threadMode;
            return this;
        }

        public DownloaderConfig build() {
            DownloaderConfig downloaderConfig = new DownloaderConfig();
            downloaderConfig.byService = this.byService;
            downloaderConfig.independentProcess = this.independentProcess;
            downloaderConfig.maxSyncDownloadNum = this.maxSyncDownloadNum;
            downloaderConfig.defaultFileDir = this.defaultFileDir;
            downloaderConfig.globalFileChecker = this.globalFileChecker;
            downloaderConfig.threadMode = this.threadMode;
            return downloaderConfig;
        }
    }
}
