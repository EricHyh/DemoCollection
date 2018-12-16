package com.hyh.download;

/**
 * @author Administrator
 * @description
 * @data 2018/12/12
 */

public class DownloaderConfig {

    private boolean byService = false;

    private boolean isIndependentProcess = false;

    private int maxSyncDownloadNum = 4;

    private String defaultFileDir;

    DownloaderConfig() {
    }

    public DownloaderConfig(boolean byService, boolean isIndependentProcess, int maxSyncDownloadNum, String defaultFileDir) {
        this.byService = byService;
        this.isIndependentProcess = isIndependentProcess;
        this.maxSyncDownloadNum = maxSyncDownloadNum;
        this.defaultFileDir = defaultFileDir;
    }

    public boolean isByService() {
        return byService;
    }

    public boolean isIndependentProcess() {
        return isIndependentProcess;
    }

    public int getMaxSyncDownloadNum() {
        return maxSyncDownloadNum;
    }

    public String getDefaultFileDir() {
        return defaultFileDir;
    }
}
