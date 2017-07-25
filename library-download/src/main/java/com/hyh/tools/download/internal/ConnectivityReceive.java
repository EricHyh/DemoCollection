package com.hyh.tools.download.internal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.hyh.tools.download.api.FileDownloader;


/**
 * Created by Administrator on 2017/3/20.
 */

public class ConnectivityReceive extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        FileDownloader fileDownloader = FileDownloader.getInstance();
        if (fileDownloader == null) {
            return;
        }
        // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                //如果当前的网络连接成功并且网络连接可用
                if (NetworkInfo.State.CONNECTED == networkInfo.getState() && networkInfo.isAvailable()) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        //有网络
                        FileDownloader.getInstance().startWaitingForWifiTasks();
                    }
                }
            }
        }
    }
}
