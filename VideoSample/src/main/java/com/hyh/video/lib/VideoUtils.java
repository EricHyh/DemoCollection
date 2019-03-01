package com.hyh.video.lib;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author Administrator
 * @description
 * @data 2019/3/1
 */

public class VideoUtils {

    public static boolean isWifiEnv(Context context) {
        try {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = null;
            if (manager != null) {
                info = manager.getActiveNetworkInfo();
            }
            return info != null && info.isAvailable() && info.getType() == ConnectivityManager.TYPE_WIFI;
        } catch (Exception e) {
            return false;
        }
    }


    public static boolean isNetEnv(Context context) {
        try {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = null;
            if (manager != null) {
                info = manager.getActiveNetworkInfo();
            }
            return (info != null && info.isAvailable());
        } catch (Exception e) {
            return false;
        }
    }
}
