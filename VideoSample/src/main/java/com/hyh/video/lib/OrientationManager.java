package com.hyh.video.lib;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Eric_He on 2019/6/23.
 */

public class OrientationManager implements SensorEventListener {

    public static final int ORIENTATION_UNSPECIFIED = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    public static final int ORIENTATION_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    public static final int ORIENTATION_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    public static final int ORIENTATION_REVERSE_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
    public static final int ORIENTATION_REVERSE_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;

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

    private final int DETECT_ORIENTATION_CHANGE_INTERVAL = 500;

    private final List<OrientationChangedListener> mListeners = new CopyOnWriteArrayList<>();

    private SensorEvent mCurrentSensorEvent;

    private int mLastOrientation = ORIENTATION_UNSPECIFIED;

    private boolean mIsRegistered;

    private long mLastDetectTimeMillis;

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

    public void addOrientationChangedListener(OrientationChangedListener listener) {
        if (listener == null || mListeners.contains(listener)) return;
        mListeners.add(listener);
    }

    public void removeOrientationChangedListener(OrientationChangedListener listener) {
        if (listener == null) return;
        mListeners.remove(listener);
        VideoUtils.log("removeOrientationChangedListener size = " + mListeners.size());
    }

    public int getCurrentOrientation() {
        return getOrientation(mCurrentSensorEvent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        mCurrentSensorEvent = event;
        int orientation = getOrientation(event);
        if (orientation == ORIENTATION_UNSPECIFIED || orientation == mLastOrientation) {
            return;
        }

        onOrientationChanged(orientation);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void onOrientationChanged(int curOrientation) {
        long currentTimeMillis = System.currentTimeMillis();
        if (Math.abs(currentTimeMillis - mLastDetectTimeMillis) < DETECT_ORIENTATION_CHANGE_INTERVAL) {
            return;
        }
        mLastDetectTimeMillis = currentTimeMillis;

        final int oldOrientation = mLastOrientation;
        mLastOrientation = curOrientation;
        if (!mListeners.isEmpty()) {
            for (OrientationChangedListener listener : mListeners) {
                listener.onChanged(oldOrientation, curOrientation);
            }
        }
    }

    private int getOrientation(SensorEvent sensorEvent) {
        if (sensorEvent == null) return ORIENTATION_UNSPECIFIED;
        float[] values = sensorEvent.values;
        if (values == null || values.length < 3) return ORIENTATION_UNSPECIFIED;
        float gradient = values[1];
        float rollAngle = values[2];
        if (rollAngle >= 45) {
            return ORIENTATION_LANDSCAPE;
        } else if (rollAngle <= -45) {
            return ORIENTATION_REVERSE_LANDSCAPE;
        } else if (gradient <= -45 && gradient >= -135) {
            return ORIENTATION_PORTRAIT;
        } else if (gradient >= 45 && gradient <= 135) {
            return ORIENTATION_REVERSE_PORTRAIT;
        }
        return ORIENTATION_UNSPECIFIED;
    }


    public interface OrientationChangedListener {

        void onChanged(int oldOrientation, int newOrientation);

    }
}