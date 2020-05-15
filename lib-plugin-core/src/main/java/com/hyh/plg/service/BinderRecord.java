package com.hyh.plg.service;

import android.content.Intent;
import android.os.IBinder;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;

class BinderRecord {

    /**
     * 当使用同一个ServiceConnection，不同的Intent去执行bindService时，ServiceConnection会被重新回调；
     * 具体顺序为:
     * 1.{@link android.content.ServiceConnection#onServiceDisconnected}
     * 2.{@link android.content.ServiceConnection#onServiceConnected}
     * <p>
     * 由上面的现象得出结论，每个ServiceConnection会对应一个Intent.
     * 这个SparseArray以ServiceConnection的hashCode值为Key，目的是保存ServiceConnection对象对应的Intent.
     */
    final SparseArray<Intent> connectionIntentCache = new SparseArray<>();

    /**
     * 发现系统的IBinder是与请求的Intent一一对应的，对于同一个Intent，只会执行一次{@link android.app.Service#onBind}.
     * 这个Map的作用是用于保存每次执行{@link android.app.Service#onBind}得到的结果.
     */
    final Map<Intent.FilterComparison, IBinder> binderCache = new HashMap<>();

    @Override
    public String toString() {
        return "BinderRecord{" +
                "connectionIntentCache size =" + connectionIntentCache.size() +
                ", binderCache size =" + binderCache.size() +
                '}';
    }
}