package com.hyh.plg.activity.transform;

import android.content.Intent;

/**
 * @author Administrator
 * @description
 * @data 2019/6/17
 */

public class TransformIntent {

    public Intent originalIntent;

    public Intent transformIntent;

    public TransformIntent(Intent originalIntent, Intent transformIntent) {
        this.originalIntent = originalIntent;
        this.transformIntent = transformIntent;
    }
}
