package com.hyh.filedownloader.sample.image;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;

/**
 * @author Administrator
 * @description
 * @data 2019/1/7
 */

public class ImageLoader {

    @SuppressLint("StaticFieldLeak")
    private volatile static ImageLoader sInstance;

    public static ImageLoader getInstance(Context context) {
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (ImageLoader.class) {
            if (sInstance == null) {
                sInstance = new ImageLoader(context);
            }
        }
        return sInstance;
    }

    Context context;

    private ImageLoader(Context context) {
        this.context = context.getApplicationContext();
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
    }

    public ImageRequest load(Uri uri) {
        return new ImageRequest(this, uri);
    }
}
