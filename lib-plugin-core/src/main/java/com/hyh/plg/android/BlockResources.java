package com.hyh.plg.android;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.hyh.plg.api.BlockEnv;

/**
 * Created by tangdongwei on 2018/5/28.
 */

public class BlockResources extends BaseBlockResources {

    private PackageInfo mBlockPackageInfo;

    /**
     * Create a new Resources object on top of an existing set of assets in an
     * AssetManager.
     *
     * @param assets  Previously created AssetManager.
     * @param metrics Current display metrics to consider when
     *                selecting/computing resource values.
     * @param config  Desired device configuration to consider when
     * @deprecated Resources should not be constructed by apps.
     * See {@link Context#createConfigurationContext(Configuration)}.
     */
    public BlockResources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
        super(assets, metrics, config);
        mBlockPackageInfo = BlockEnv.sBlockPackageInfo;
    }


    @Override
    public int getIdentifier(String name, String defType, String defPackage) {
        if (TextUtils.equals(defPackage, BlockApplication.hostApplicationContext.getPackageName())
                && mBlockPackageInfo != null) {
            int identifier = super.getIdentifier(name, defType, mBlockPackageInfo.packageName);
            return identifier != 0 ? identifier : super.getIdentifier(name, defType, defPackage);
        } else {
            return super.getIdentifier(name, defType, defPackage);
        }
    }
}