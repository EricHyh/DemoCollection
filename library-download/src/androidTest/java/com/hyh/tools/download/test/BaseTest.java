package com.hyh.tools.download.test;

import android.content.Context;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.runner.RunWith;

/**
 * @author Administrator
 * @description
 * @data 2018/2/28
 */
@RunWith(AndroidJUnit4.class)
public class BaseTest {

    protected Context mContext = InstrumentationRegistry.getTargetContext();

    public void waiting() {
        SystemClock.sleep(2000000000);
    }
}
