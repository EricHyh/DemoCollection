package com.hyh.download.core;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Eric_He on 2018/12/16.
 */

public class TaskStateCache {

    private CopyOnWriteArrayList<String> mPreparedResKeyList = new CopyOnWriteArrayList<>();

    void addPreparedResKey(String resKey) {
        if (!mPreparedResKeyList.contains(resKey)) {
            mPreparedResKeyList.add(resKey);
        }
    }

    void removePreparedResKey(String resKey) {
        mPreparedResKeyList.remove(resKey);
    }

    public boolean isTaskPrepared(String resKey) {
        return mPreparedResKeyList.contains(resKey);
    }
}
