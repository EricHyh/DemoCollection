package com.hyh.plg.ins;

import android.content.Intent;
import android.util.LruCache;

/**
 * @author Administrator
 * @description
 * @data 2020/4/28
 */
class IntentCache {

    private static final IntentCache INSTANCE = new IntentCache();

    static IntentCache getInstance() {
        return INSTANCE;
    }

    private final LruCache<String, Intent> mIntentLruCache = new LruCache<>(50);

    private IntentCache() {
    }

    void saveIntent(String key, Intent intent) {
        mIntentLruCache.put(key, intent);
    }

    Intent getIntent(String key) {
        return mIntentLruCache.get(key);
    }

    Intent removeIntent(String key) {
        return mIntentLruCache.remove(key);
    }
}