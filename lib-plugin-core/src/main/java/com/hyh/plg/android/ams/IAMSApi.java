package com.hyh.plg.android.ams;


import android.app.Activity;
import android.content.Context;

import com.hyh.plg.activity.IActivity;

/**
 * Created by tangdongwei on 2018/9/17.
 */
public interface IAMSApi {

    void addIActivityMap(String source, Class<? extends IActivity> map);

    void addIActivityMap(String source, Class<? extends IActivity> map, int mapLaunchMode);

    void addActivityMap(String source, Class<? extends Activity> map);

    void addActivityMap(String source, Class<? extends Activity> map, int mapLaunchMode);

    ProxyActivityMap getActivityMap(String source);

    void proxyInstrumentationPersist(Context context);

    void proxyInstrumentationPersist(Activity activity);

    void recoverInstrumentation(Context context);

    void recoverInstrumentation(Activity activity);

}