package com.hyh.tools.download.test;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * @author Administrator
 * @description
 * @data 2018/2/28
 */

public class TestAppContext extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
