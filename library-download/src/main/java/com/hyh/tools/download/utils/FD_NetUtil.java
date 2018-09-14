package com.hyh.tools.download.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author Administrator
 * @description
 * @data 2018/2/7
 */

public class FD_NetUtil {


    public static boolean isNetEnv(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = null;
        if (manager != null) {
            info = manager.getActiveNetworkInfo();
        }
        return (info != null && info.isAvailable());
    }

    public static boolean isWifi(Context context) {
        ConnectivityManager connectMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectMgr != null) {
            networkInfo = connectMgr.getActiveNetworkInfo();
        }
        if (networkInfo != null) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }


}
