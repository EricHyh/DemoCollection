package com.hyh.plg.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.yly.mob.utils.Logger;

/**
 * @author Administrator
 * @description
 * @data 2018/3/14
 */
class ResourceLoader {

    @SuppressLint("PrivateApi")
    static AssetManager createAssetManager(String apkPath) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            try {
                AssetManager.class.getDeclaredMethod("addAssetPath", String.class).invoke(assetManager, apkPath);
            } catch (Throwable th) {
                Logger.e("createAssetManager failed", th);
            }
            return assetManager;
        } catch (Throwable th) {
            Logger.e("createAssetManager failed", th);
        }
        return null;
    }

    static Resources getBundleResource(Context context, AssetManager assetManager) {
        return new BlockResources(assetManager, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
    }
}