package com.hyh.plg.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.hyh.plg.api.BlockEnv;
import com.yly.mob.protocol.ReceiverOperation;
import com.yly.mob.utils.Logger;

/**
 * @author Administrator
 * @description
 * @data 2018/4/10
 */
public class DynamicReceiverProxy extends BroadcastReceiver {

    private BroadcastReceiver mReceiverClient;

    public DynamicReceiverProxy(BroadcastReceiver receiverClient) {
        mReceiverClient = receiverClient;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mReceiverClient != null) {
            mReceiverClient.onReceive(getBlockBroadcastBaseContext(mReceiverClient), intent);
            if (intent != null) {
                String operation = intent.getStringExtra(ReceiverOperation.KEY);
                if (TextUtils.isEmpty(operation)) {
                    return;
                }
                switch (operation) {
                    case ReceiverOperation.Value.ABORT_BROADCAST: {
                        try {
                            abortBroadcast();
                        } catch (Throwable th) {
                            Logger.d("DynamicReceiverProxy: abortBroadcast failed", th);
                        }
                        break;
                    }
                }
            }
        }
    }

    private Context getBlockBroadcastBaseContext(BroadcastReceiver broadcastReceiver) {
        Context context = BlockEnv.sBlockBroadcastBaseContextMap.get(broadcastReceiver.getClass().getName());
        if (context == null) return BlockEnv.sBlockApplication;
        return context;
    }
}