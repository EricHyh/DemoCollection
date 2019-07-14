package com.hyh.video.base;

import android.app.Application;

/**
 * Created by Eric_He on 2019/7/14.
 */

public class AppContext extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        /*IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent intent1 = new Intent(getApplicationContext(), VideoActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);
            }
        }, intentFilter);*/
    }
}
