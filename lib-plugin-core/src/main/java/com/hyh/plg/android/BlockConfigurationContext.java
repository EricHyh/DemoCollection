package com.hyh.plg.android;

import android.content.Context;
import android.content.res.Resources;

/**
 * @author Administrator
 * @description
 * @data 2019/9/30
 */

public class BlockConfigurationContext extends BaseBlockApplication {

    private final BlockResources mBlockResources;

    public BlockConfigurationContext(Context context) {
        super(context);
        Resources resources = context.getResources();
        mBlockResources = new BlockResources(resources.getAssets(), resources.getDisplayMetrics(), resources.getConfiguration());
    }

    @Override
    public Resources getResources() {
        return mBlockResources;
    }

    @Override
    public Context getApplicationContext() {
        return this;
    }
}