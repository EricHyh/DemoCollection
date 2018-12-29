package com.hyh.download.core;

import android.content.Context;
import android.os.SystemClock;

import com.hyh.download.utils.NetworkHelper;

/**
 * @author Administrator
 * @description
 * @data 2018/12/29
 */

public class RetryStrategyImpl implements IRetryStrategy {

    private final Object mLock = new Object();

    private Context context;

    private boolean isPermitRetryInMobileData;

    private int maxRetryTimes = 3;

    private int totalMaxRetryTimes = 20;

    private int searchSuitableNetMaxTimes = 15;

    private long retryBaseDelay = 1000 * 2;

    private long searchSuitableNetDelay = 1000 * 2;

    private int totalRetryTimes;

    private int currentRetryTimes;

    private volatile boolean cancel;


    RetryStrategyImpl(Context context, boolean isPermitRetryInMobileData) {
        this.context = context;
        this.isPermitRetryInMobileData = isPermitRetryInMobileData;
    }


    public RetryStrategyImpl(Context context, boolean isPermitRetryInMobileData, int maxRetryTimes, int totalMaxRetryTimes, int searchSuitableNetMaxTimes, long retryBaseDelay, long searchSuitableNetDelay) {
        this.context = context;
        this.isPermitRetryInMobileData = isPermitRetryInMobileData;
        this.maxRetryTimes = maxRetryTimes;
        this.totalMaxRetryTimes = totalMaxRetryTimes;
        this.searchSuitableNetMaxTimes = searchSuitableNetMaxTimes;
        this.retryBaseDelay = retryBaseDelay;
        this.searchSuitableNetDelay = searchSuitableNetDelay;
    }

    private boolean waitingSuitableNetworkType(onWaitingListener listener) {
        int waitingNumber = 0;
        while (true) {
            if (cancel) {
                return false;
            }
            if (isSuitableNetworkType()) {
                return true;
            }
            listener.onWaiting();
            SystemClock.sleep(searchSuitableNetDelay);
            waitingNumber++;
            if (waitingNumber == searchSuitableNetMaxTimes) {
                return false;
            }
        }
    }

    private boolean isSuitableNetworkType() {
        return NetworkHelper.isWifiEnv(context)
                || isPermitRetryInMobileData && NetworkHelper.isNetEnv(context);
    }

    @Override
    public boolean shouldRetry(onWaitingListener listener) {
        for (; ; ) {
            synchronized (mLock) {
                if (cancel) {
                    return false;
                }
            }
            if (currentRetryTimes >= maxRetryTimes || totalRetryTimes >= totalMaxRetryTimes) {
                return false;
            }
            currentRetryTimes++;
            totalRetryTimes++;

            if (waitingSuitableNetworkType(listener)) {
                if (currentRetryTimes == 0 || currentRetryTimes == 1) {
                    listener.onWaiting();
                    SystemClock.sleep(retryBaseDelay);
                }
                if (currentRetryTimes == 2) {
                    listener.onWaiting();
                    SystemClock.sleep(2 * retryBaseDelay);
                }
                synchronized (mLock) {
                    if (cancel) {
                        return false;
                    }
                }
                return true;
            }
        }
    }

    @Override
    public void clearCurrentRetryTimes() {
        currentRetryTimes = 0;
    }

    @Override
    public void cancel() {
        synchronized (mLock) {
            cancel = true;
        }
    }
}
