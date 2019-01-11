package com.hyh.filedownloader.sample.image;

import android.graphics.Bitmap;

import java.io.File;

/**
 * @author Administrator
 * @description
 * @data 2019/1/11
 */

public class LoaderConfig {

    private Bitmap.Config config;

    private File cacheDir;

    private long maxCacheSize;

    private LoaderConfig() {
    }

    public Bitmap.Config getConfig() {
        return config;
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public long getMaxCacheSize() {
        return maxCacheSize;
    }

    public static class Builder {

        private Bitmap.Config config;

        private File cacheDir;

        private long maxCacheSize;

        public Builder config(Bitmap.Config config) {
            this.config = config;
            return this;
        }

        public Builder cacheDir(File cacheDir) {
            this.cacheDir = cacheDir;
            return this;
        }

        public Builder maxCacheSize(long maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
            return this;
        }

        public LoaderConfig build() {
            LoaderConfig config = new LoaderConfig();
            config.config = this.config;
            config.cacheDir = this.cacheDir;
            config.maxCacheSize = this.maxCacheSize;
            return config;
        }
    }
}
