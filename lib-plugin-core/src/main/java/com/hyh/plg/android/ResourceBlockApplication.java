package com.hyh.plg.android;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;

import com.hyh.plg.utils.Logger;


/**
 * Created by tangdongwei on 2018/6/25.
 */

public class ResourceBlockApplication extends BaseBlockApplication {

    public static final int DEFAULT_THEME_RESOURCE = android.R.style.Theme_Holo_Light_NoActionBar;

    private AssetManager mAssetManager;
    private Resources mResources;
    private Resources.Theme mTheme;
    private LayoutInflater mLayoutInflater;

    public ResourceBlockApplication(Context context, String blockPath) {
        super(context);
        this.mAssetManager = ResourceLoader.createAssetManager(blockPath);
        this.mResources = ResourceLoader.getBundleResource(context, mAssetManager);
        this.mLayoutInflater = LayoutInflater.from(context).cloneInContext(this);
        mLayoutInflater.setFactory2(new LayoutFactory());
    }

    @Override
    public AssetManager getAssets() {
        return mAssetManager;
    }

    @Override
    public Resources getResources() {
        return mResources;
    }

    public void setDefaultTheme(int resid) {
        mTheme = mResources.newTheme();
        mTheme.applyStyle(resid, true);
    }

    @Override
    public void setTheme(int resid) {
        mTheme.applyStyle(resid, true);
    }

    @Override
    public Resources.Theme getTheme() {
        return mTheme;
    }

    @Override
    public Object getSystemService(String name) {
        if (TextUtils.equals(Context.LAYOUT_INFLATER_SERVICE, name)) {
            return mLayoutInflater;
        }
        return super.getSystemService(name);
    }

    @Override
    public Context createConfigurationContext(Configuration overrideConfiguration) {
        Logger.d("ResourceBlockApplication createConfigurationContext");
        Context configurationContext = super.createConfigurationContext(overrideConfiguration);
        return new BlockConfigurationContext(configurationContext);
    }

    public LayoutInflater getLayoutInflater() {
        return mLayoutInflater;
    }
}