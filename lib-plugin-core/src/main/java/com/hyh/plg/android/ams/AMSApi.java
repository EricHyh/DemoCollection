package com.hyh.plg.android.ams;


import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.hyh.plg.activity.ActivityProxyImpl;
import com.hyh.plg.activity.ActivityProxyInner;
import com.hyh.plg.activity.ActivityProxyInner2;
import com.hyh.plg.activity.IActivity;
import com.hyh.plg.activity.LaunchMode;
import com.hyh.plg.hook.instrumentation.ActivityInstrumentation;
import com.hyh.plg.hook.instrumentation.ApplicationInstrumentation;
import com.hyh.plg.utils.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tangdongwei on 2018/9/17.
 */
public class AMSApi implements IAMSApi {

    private static AMSApi mInstance = null;

    private Map<String, ProxyActivityMap> mProxyMap;

    private AMSApi() {
        mProxyMap = new HashMap<>();
    }

    public static AMSApi getInstance() {
        if (mInstance == null) {
            synchronized (AMSApi.class) {
                if (mInstance == null) {
                    mInstance = new AMSApi();
                }
            }
        }
        return mInstance;
    }

    @Override
    public void addIActivityMap(String source, Class<? extends IActivity> map) {
        addIActivityMap(source, map, LaunchMode.ANYWAY);
    }

    @Override
    public void addIActivityMap(String source, Class<? extends IActivity> map, int mapLaunchMode) {
        addProxyActivityMap(source, map, mapLaunchMode);
    }

    @Override
    public void addActivityMap(String source, Class<? extends Activity> map) {
        addActivityMap(source, map, LaunchMode.ANYWAY);
    }

    @Override
    public void addActivityMap(String source, Class<? extends Activity> map, int mapLaunchMode) {
        addProxyActivityMap(source, map, mapLaunchMode);
    }

    private void addProxyActivityMap(String source, Class<?> map, int mapLaunchMode) {
        if (TextUtils.isEmpty(source) || map == null) {
            return;
        }
        ProxyActivityMap proxyActivityMap = getActivityMap(source);
        if (proxyActivityMap != null) {
            proxyActivityMap.map = map;
            proxyActivityMap.mapLaunchMode = mapLaunchMode;
            Logger.v("IN AMSApi, addProxyActivityMap(), replace proxyActivityMap=" + proxyActivityMap);
        } else {
            proxyActivityMap = new ProxyActivityMap();
            proxyActivityMap.source = source;
            proxyActivityMap.map = map;
            proxyActivityMap.mapLaunchMode = mapLaunchMode;
            mProxyMap.put(source, proxyActivityMap);
            Logger.v("IN AMSApi, addProxyActivityMap(), add proxyActivityMap=" + proxyActivityMap);
        }
    }

    @Override
    public ProxyActivityMap getActivityMap(String source) {
        if (TextUtils.isEmpty(source) || mProxyMap.isEmpty()) {
            return null;
        }
        return mProxyMap.get(source);
    }

    @Override
    public void proxyInstrumentationPersist(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity instanceof ActivityProxyInner
                    || activity instanceof ActivityProxyInner2
                    || ActivityProxyImpl.isBlockActivity(activity)) {
                return;
            }
            ActivityInstrumentation.obtain(activity).requestReplaceInstrumentation();
        } else {
            ApplicationInstrumentation.getInstance().requestReplaceInstrumentationPersist();
        }
    }

    @Override
    public void proxyInstrumentationPersist(Activity activity) {
        if (activity instanceof ActivityProxyInner
                || activity instanceof ActivityProxyInner2
                || ActivityProxyImpl.isBlockActivity(activity)) {
            return;
        }
        ActivityInstrumentation.obtain(activity).requestReplaceInstrumentation();
    }

    @Override
    public void recoverInstrumentation(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity instanceof ActivityProxyInner
                    || activity instanceof ActivityProxyInner2
                    || ActivityProxyImpl.isBlockActivity(activity)) {
                return;
            }
            ActivityInstrumentation.obtain(activity).requestRecoverInstrumentation();
        } else {
            ApplicationInstrumentation.getInstance().requestRecoverInstrumentationPersist();
        }
    }

    @Override
    public void recoverInstrumentation(Activity activity) {
        if (activity instanceof ActivityProxyInner
                || activity instanceof ActivityProxyInner2
                || ActivityProxyImpl.isBlockActivity(activity)) {
            return;
        }
        ActivityInstrumentation.obtain(activity).requestRecoverInstrumentation();
    }
}