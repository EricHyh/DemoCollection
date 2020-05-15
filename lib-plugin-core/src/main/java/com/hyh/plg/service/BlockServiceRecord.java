package com.hyh.plg.service;

import android.app.Service;

/**
 * Created by tangdongwei on 2018/11/20.
 */
public class BlockServiceRecord {

    private Service serviceInstance;

    private BinderRecord binderRecord;

    private boolean isStarted = false;

    private int startId;

    Service getServiceInstance() {
        return serviceInstance;
    }

    void setServiceInstance(Service serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    boolean isStarted() {
        return isStarted;
    }

    void setStarted(boolean started) {
        isStarted = started;
    }

    BinderRecord getBinderRecord() {
        return binderRecord;
    }

    void setBinderRecord(BinderRecord binderRecord) {
        this.binderRecord = binderRecord;
    }

    int getStartId() {
        return startId;
    }

    void setStartId(int startId) {
        this.startId = startId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlockServiceRecord that = (BlockServiceRecord) o;

        return serviceInstance != null ? serviceInstance.equals(that.serviceInstance) : that.serviceInstance == null;
    }

    @Override
    public int hashCode() {
        return serviceInstance != null ? serviceInstance.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "BlockServiceRecord{" +
                "serviceInstance=" + serviceInstance +
                ", binderRecord=" + binderRecord +
                ", isStarted=" + isStarted +
                ", startId=" + startId +
                '}';
    }
}