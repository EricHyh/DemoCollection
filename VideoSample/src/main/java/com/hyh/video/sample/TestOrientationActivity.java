package com.hyh.video.sample;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.hyh.video.lib.OrientationManager;

/**
 * @author Administrator
 * @description
 * @data 2019/7/1
 */

public class TestOrientationActivity extends Activity implements SensorEventListener, OrientationManager.OrientationChangedListener {

    private TextView mTextView1;
    private TextView mTextView2;
    private TextView mTextView3;
    private TextView mTextView4;

    private long mLastTimeMillis;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_orientation_activity);

        mTextView1 = findViewById(R.id.tv1);
        mTextView2 = findViewById(R.id.tv2);
        mTextView3 = findViewById(R.id.tv3);
        mTextView4 = findViewById(R.id.tv4);

        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (manager != null) {
            Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        }

        OrientationManager.getInstance(this).addOrientationChangedListener(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long currentTimeMillis = System.currentTimeMillis();
        if (Math.abs(currentTimeMillis - mLastTimeMillis) > 1000) {
            float[] values = event.values;
            mTextView1.setText(String.valueOf(values[0]));
            mTextView2.setText(String.valueOf(values[1]));
            mTextView3.setText(String.valueOf(values[2]));
            mTextView4.setText(String.valueOf(event.accuracy));
            mLastTimeMillis = currentTimeMillis;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onChanged(int oldOrientation, int newOrientation) {
        setRequestedOrientation(newOrientation);
    }
}