package com.hyh.download.sample.base;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.hyh.download.FileDownloader;

/**
 * @author Administrator
 * @description
 * @data 2018/12/25
 */

public class AppContext extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        FileDownloader.getInstance().init(this);
    }
}
