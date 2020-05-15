package com.hyh.plg.activity;

import android.content.Intent;

import java.util.HashMap;

/**
 * Created by tangdongwei on 2018/5/19.
 */

public class IntentCachePool extends HashMap<String, Intent> {

    private static volatile IntentCachePool mInstance = null;

    private static final int MAX_SIZE = 50;

    public static IntentCachePool getInstance() {
        if (mInstance == null) {
            synchronized (IntentCachePool.class) {
                if (mInstance == null) {
                    mInstance = new IntentCachePool();
                }
            }
        }
        return mInstance;
    }

    @Override
    public Intent getOrDefault(Object key, Intent defaultValue) {
        Intent intent;
        return (intent = remove(key)) == null ? defaultValue : intent;
    }

    @Override
    public Intent get(Object key) {
        return remove(key);
    }

    @Override
    public Intent put(String key, Intent value) {
        if (size() > MAX_SIZE) {
            clear();
        }
        return super.put(key, value);
    }
}
