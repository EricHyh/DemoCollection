package com.hyh.plg.activity;

import android.text.TextUtils;

import com.yly.mob.utils.Logger;

import org.json.JSONObject;

/**
 * Created by tangdongwei on 2019/3/6.
 */
public class IntentDataCarrier {

    public static final String KEY_PROXY_IMPL_CLASS_PATH_STRING = ActivityProxyImpl.EXTRA_PROXY_IMPL_CLASS_PATH;
    public static final String KEY_BLOCK_ACTIVITY_CLASS_PATH_STRING = ActivityProxyImpl.EXTRA_BLOCK_ACTIVITY_CLASS_PATH;
    public static final String KEY_KEY_OF_INTENT_STRING = ActivityProxyImpl.EXTRA_KEY_OF_INTENT;
    public static final String KEY_LAUNCH_MODE_INT = ActivityProxyImpl.EXTRA_LAUNCH_MODE;

    public static final String KEY_BLOCK_CLASS_LOADER_HASH_CODE_INT = "blockClassLoaderHashCode";
    public static final String KEY_OPEN_BLOCK_ACTIVITY_BOOLEAN = "openBlockActivity";
    public static final String KEY_FROM_BLOCK_BOOLEAN = "fromBlock";

    private static final String TAG_PROXY_DATA_HEADER = "Proxy:";

    private int blockClassLoaderHashCode;
    private String proxyImplClassPath = null;
    private String blockActivityClassPath = null;
    private String keyOfIntent = null;
    private int launchMode = -1;
    private boolean openBlockActivity = false;
    private boolean fromBlock = false;

    public static IntentDataCarrier create(String data) {
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        if (!data.startsWith(TAG_PROXY_DATA_HEADER)) {
            return null;
        } else {
            data = data.substring(TAG_PROXY_DATA_HEADER.length());
        }
        try {
            IntentDataCarrier intentDataCarrier = new IntentDataCarrier();
            JSONObject jsonObject = new JSONObject(data);
            intentDataCarrier.setBlockClassLoaderHashCode(jsonObject.optInt(KEY_BLOCK_CLASS_LOADER_HASH_CODE_INT, 0));
            intentDataCarrier.setProxyImplClassPath(jsonObject.optString(KEY_PROXY_IMPL_CLASS_PATH_STRING, ""));
            intentDataCarrier.setBlockActivityClassPath(jsonObject.optString(KEY_BLOCK_ACTIVITY_CLASS_PATH_STRING, ""));
            intentDataCarrier.setKeyOfIntent(jsonObject.optString(KEY_KEY_OF_INTENT_STRING, ""));
            intentDataCarrier.setLaunchMode(jsonObject.optInt(KEY_LAUNCH_MODE_INT, -1));
            intentDataCarrier.setOpenBlockActivity(jsonObject.optBoolean(KEY_OPEN_BLOCK_ACTIVITY_BOOLEAN, false));
            intentDataCarrier.setFromBlock(jsonObject.optBoolean(KEY_FROM_BLOCK_BOOLEAN, false));
            return intentDataCarrier;
        } catch (Exception e) {
            Logger.w("IN IntentDataCarrier, create() ERROR : " + e.toString());
        }
        return null;
    }

    public int getBlockClassLoaderHashCode() {
        return blockClassLoaderHashCode;
    }

    public void setBlockClassLoaderHashCode(int blockClassLoaderHashCode) {
        this.blockClassLoaderHashCode = blockClassLoaderHashCode;
    }

    public String getProxyImplClassPath() {
        return proxyImplClassPath;
    }

    public void setProxyImplClassPath(String proxyImplClassPath) {
        this.proxyImplClassPath = proxyImplClassPath;
    }

    public String getBlockActivityClassPath() {
        return blockActivityClassPath;
    }

    public void setBlockActivityClassPath(String blockActivityClassPath) {
        this.blockActivityClassPath = blockActivityClassPath;
    }

    public String getKeyOfIntent() {
        return keyOfIntent;
    }

    public void setKeyOfIntent(String keyOfIntent) {
        this.keyOfIntent = keyOfIntent;
    }

    public int getLaunchMode() {
        return launchMode;
    }

    public void setLaunchMode(int mLaunchMode) {
        this.launchMode = mLaunchMode;
    }

    public boolean isOpenBlockActivity() {
        return openBlockActivity;
    }

    public void setOpenBlockActivity(boolean openBlockActivity) {
        this.openBlockActivity = openBlockActivity;
    }

    public boolean isFromBlock() {
        return fromBlock;
    }

    public void setFromBlock(boolean fromBlock) {
        this.fromBlock = fromBlock;
    }

    public boolean isFromCurrentClassLoader() {
        return blockClassLoaderHashCode == System.identityHashCode(getClass().getClassLoader());
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(KEY_BLOCK_CLASS_LOADER_HASH_CODE_INT, this.blockClassLoaderHashCode);
            jsonObject.put(KEY_PROXY_IMPL_CLASS_PATH_STRING, this.proxyImplClassPath);
            jsonObject.put(KEY_BLOCK_ACTIVITY_CLASS_PATH_STRING, this.blockActivityClassPath);
            jsonObject.put(KEY_KEY_OF_INTENT_STRING, this.keyOfIntent);
            jsonObject.put(KEY_LAUNCH_MODE_INT, this.launchMode);
            jsonObject.put(KEY_OPEN_BLOCK_ACTIVITY_BOOLEAN, this.openBlockActivity);
            jsonObject.put(KEY_FROM_BLOCK_BOOLEAN, this.fromBlock);
            String jsonString = jsonObject.toString();
            return TAG_PROXY_DATA_HEADER + jsonString;
        } catch (Exception e) {
            Logger.w("IN IntentDataCarrier, toString() ERROR : " + e.toString());
        }
        return "";
    }
}
