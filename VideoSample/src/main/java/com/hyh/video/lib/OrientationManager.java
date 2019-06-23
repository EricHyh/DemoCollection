package com.hyh.video.lib;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by Eric_He on 2019/6/23.
 */

public class OrientationManager implements SensorEventListener {

    private static OrientationManager sInstance;

    public static OrientationManager getInstance(Context context) {
        if (sInstance != null) {
            sInstance.registerOrientationListener(context);
            return sInstance;
        }
        synchronized (OrientationManager.class) {
            if (sInstance == null) sInstance = new OrientationManager(context);
        }
        return sInstance;
    }

    private boolean mIsRegistered;

    private OrientationManager(Context context) {
        registerOrientationListener(context);
    }

    private void registerOrientationListener(Context context) {
        if (mIsRegistered) return;
        SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (manager == null) return;
        mIsRegistered = true;
        Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}