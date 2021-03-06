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

    private Context context;

    private boolean isPermitRetryInMobileData;

    private int maxRetryTimes = 3;

    private int totalMaxRetryTimes = 20;

    private int searchSuitableNetMaxTimes = 10;

    private long retryBaseDelay = 1000 * 3;

    private long searchSuitableNetDelay = 1000 * 3;

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

    private boolean waitingSuitableNetworkType() {
        int waitingNumber = 0;
        while (true) {
            if (cancel) {
                return false;
            }
            if (isSuitableNetworkType()) {
                return true;
            }
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
    public boolean shouldRetry() {
        for (; ; ) {
            if (cancel) {
                return false;
            }
            if (currentRetryTimes >= maxRetryTimes || totalRetryTimes >= totalMaxRetryTimes) {
                return false;
            }
            currentRetryTimes++;
            totalRetryTimes++;

            if (waitingSuitableNetworkType()) {
                if (currentRetryTimes == 0 || currentRetryTimes == 1) {
                    SystemClock.sleep(retryBaseDelay);
                }
                if (currentRetryTimes == 2) {
                    SystemClock.sleep(2 * retryBaseDelay);
                }
                return !cancel;
            }
        }
    }

    @Override
    public void clearCurrentRetryTimes() {
        currentRetryTimes = 0;

    }

    @Override
    public void subtractTotalRetryTimesIfReachMax() {
        if (totalRetryTimes > 17) {
            totalRetryTimes = 17;
        }
    }

    @Override
    public void cancel() {
        cancel = true;
    }
}
