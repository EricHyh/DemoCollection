package com.hyh.plg.activity;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by tangdongwei on 2018/5/21.
 */

public class ActivityCachePool implements Map<Integer, IActivity> {

    private static volatile ActivityCachePool mInstance = null;

    private HashMap<Integer, WeakReference<IActivity>> mCache;

    private ActivityCachePool() {
        this.mCache = new HashMap<>();
    }

    public static ActivityCachePool getInstance() {
        if (mInstance == null) {
            synchronized (ActivityCachePool.class) {
                if (mInstance == null) {
                    mInstance = new ActivityCachePool();
                }
            }
        }
        return mInstance;
    }

    @Override
    public int size() {
        return mCache.size();
    }

    @Override
    public boolean isEmpty() {
        return mCache.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return mCache.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return mCache.containsValue(value);
    }

    @Override
    public IActivity get(Object key) {
        WeakReference<IActivity> weakReference = mCache.get(key);
        return weakReference == null ? null : weakReference.get();
    }

    @Override
    public IActivity put(Integer key, IActivity value) {
        WeakReference<IActivity> weakReference = new WeakReference<IActivity>(value);
        WeakReference<IActivity> old = mCache.put(key, weakReference);
        return old == null ? null : old.get();
    }

    @Override
    public IActivity remove(Object key) {
        WeakReference<IActivity> weakReference = mCache.remove(key);
        return weakReference == null ? null : weakReference.get();
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends IActivity> m) {
        return;
    }

    @Override
    public void clear() {
        mCache.clear();
    }

    @Override
    public Set<Integer> keySet() {
        return mCache.keySet();
    }

    @Override
    public Collection<IActivity> values() {
        return null;
    }

    @Override
    public Set<Entry<Integer, IActivity>> entrySet() {
        return null;
    }
}
