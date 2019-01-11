package com.hyh.filedownloader.sample.image;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.hyh.filedownloader.sample.utils.FileUtil;

import java.io.File;

/**
 * @author Administrator
 * @description
 * @data 2019/1/7
 */

public class HappyImage {

    @SuppressLint("StaticFieldLeak")
    private volatile static HappyImage sInstance;

    public static HappyImage getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (HappyImage.class) {
            if (sInstance == null) {
                sInstance = new HappyImage();
            }
        }
        return sInstance;
    }

    Context context;

    LoaderConfig config;

    private HappyImage() {
    }

    public void init(Context context, LoaderConfig config) {
        if (this.context != null) {
            return;
        }
        this.context = context.getApplicationContext();
        if (config == null) {
            config = new LoaderConfig.Builder()
                    .config(Bitmap.Config.RGB_565)
                    .cacheDir(FileUtil.ensureCreated(new File(context.getExternalCacheDir(), "HappyImage")))
                    .maxCacheSize(1024 * 1024 * 100)
                    .build();
        }
        this.config = config;
    }

    public RequestCreator load(Uri uri) {
        return new RequestCreator(this, uri);
    }
}