package com.hyh.plg.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;

/**
 * Created by tangdongwei on 2018/11/20.
 */
public interface IServiceServer {

    ComponentName startService(Intent service);

    ComponentName startForegroundService(Intent service);

    boolean bindService(Intent service, ServiceConnection conn, int flags);

    boolean unbindService(ServiceConnection conn);

    boolean stopService(Intent name);

}
