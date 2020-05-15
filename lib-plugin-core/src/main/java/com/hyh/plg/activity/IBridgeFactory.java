package com.hyh.plg.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

/**
 * @author Administrator
 * @description
 * @data 2019/2/26
 */

public interface IBridgeFactory {

    Context replaceBaseContext(Activity activity, Context newBase);

    IActivityBridge produce(Activity activity, Bundle savedInstanceState);

}
