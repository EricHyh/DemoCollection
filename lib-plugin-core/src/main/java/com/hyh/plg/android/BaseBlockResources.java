package com.hyh.plg.android;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Movie;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.hyh.plg.api.BlockEnv;
import com.yly.mob.utils.Logger;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tangdongwei on 2018/5/28.
 */

public class BaseBlockResources extends Resources {

    private final Resources mHostResources;

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
    public BaseBlockResources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
        super(assets, metrics, config);
        mHostResources = BlockEnv.sHostApplicationContext.getResources();
    }


    @Override
    public XmlResourceParser getLayout(int id) throws NotFoundException {
        try {
            return super.getLayout(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources, getLayout[id = " + id + "] from block failed ", e);
            return mHostResources.getLayout(id);
        }
    }

    @Override
    public CharSequence getText(int id) throws NotFoundException {
        try {
            return super.getText(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getText[id = " + id + "] from block failed ", e);
            return mHostResources.getText(id);
        }
    }

    @Override
    public Typeface getFont(int id) throws NotFoundException {
        try {
            return super.getFont(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getFont[id = " + id + "] from block failed ", e);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return mHostResources.getFont(id);
            } else {
                throw e;
            }
        }
    }

    @Override
    public CharSequence getQuantityText(int id, int quantity) throws NotFoundException {
        try {
            return super.getQuantityText(id, quantity);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getQuantityText[id = " + id + "] from block failed ", e);
            return mHostResources.getQuantityText(id, quantity);
        }
    }

    @Override
    public String getString(int id) throws NotFoundException {
        try {
            return super.getString(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getString[id = " + id + "] from block failed ", e);
            return mHostResources.getString(id);
        }
    }

    @Override
    public String getString(int id, Object... formatArgs) throws NotFoundException {
        try {
            return super.getString(id, formatArgs);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getString[id = " + id + "] from block failed ", e);
            return mHostResources.getString(id, formatArgs);
        }
    }

    @Override
    public String getQuantityString(int id, int quantity, Object... formatArgs) throws NotFoundException {
        try {
            return super.getQuantityString(id, quantity, formatArgs);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getQuantityString[id = " + id + "] from block failed ", e);
            return mHostResources.getQuantityString(id, quantity, formatArgs);
        }
    }

    @Override
    public String getQuantityString(int id, int quantity) throws NotFoundException {
        try {
            return super.getQuantityString(id, quantity);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getQuantityString[id = " + id + "] from block failed ", e);
            return mHostResources.getQuantityString(id, quantity);
        }
    }

    @Override
    public CharSequence getText(int id, CharSequence def) {
        try {
            return super.getText(id, def);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getText[id = " + id + "] from block failed ", e);
            return mHostResources.getText(id, def);
        }
    }

    @Override
    public CharSequence[] getTextArray(int id) throws NotFoundException {
        try {
            return super.getTextArray(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getTextArray[id = " + id + "] from block failed ", e);
            return mHostResources.getTextArray(id);
        }
    }

    @Override
    public String[] getStringArray(int id) throws NotFoundException {
        try {
            return super.getStringArray(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getStringArray[id = " + id + "] from block failed ", e);
            return mHostResources.getStringArray(id);
        }
    }

    @Override
    public int[] getIntArray(int id) throws NotFoundException {
        try {
            return super.getIntArray(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getIntArray[id = " + id + "] from block failed ", e);
            return mHostResources.getIntArray(id);
        }
    }

    @Override
    public TypedArray obtainTypedArray(int id) throws NotFoundException {
        try {
            return super.obtainTypedArray(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources obtainTypedArray[id = " + id + "] from block failed ", e);
            return mHostResources.obtainTypedArray(id);
        }
    }

    @Override
    public float getDimension(int id) throws NotFoundException {
        try {
            return super.getDimension(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getDimension[id = " + id + "] from block failed ", e);
            return mHostResources.getDimension(id);
        }
    }

    @Override
    public int getDimensionPixelOffset(int id) throws NotFoundException {
        try {
            return super.getDimensionPixelOffset(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getDimensionPixelOffset[id = " + id + "] from block failed ", e);
            return mHostResources.getDimensionPixelOffset(id);
        }
    }

    @Override
    public int getDimensionPixelSize(int id) throws NotFoundException {
        try {
            return super.getDimensionPixelSize(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getDimensionPixelSize[id = " + id + "] from block failed ", e);
            return mHostResources.getDimensionPixelSize(id);
        }
    }

    @Override
    public float getFraction(int id, int base, int pbase) {
        try {
            return super.getFraction(id, base, pbase);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getFraction[id = " + id + "] from block failed ", e);
            return mHostResources.getFraction(id, base, pbase);
        }
    }

    @Override
    public Drawable getDrawable(int id) throws NotFoundException {
        try {
            return super.getDrawable(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getDrawable[id = " + id + "] from block failed ", e);
            return mHostResources.getDrawable(id);
        }
    }



    @Override
    public Drawable getDrawable(int id, Theme theme) throws NotFoundException {
        try {
            return super.getDrawable(id, theme);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getDrawable[id = " + id + "] from block failed ", e);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return mHostResources.getDrawable(id, theme);
            } else {
                throw e;
            }
        }
    }

    @Override
    public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
        try {
            return super.getDrawableForDensity(id, density);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getDrawableForDensity[id = " + id + "] from block failed ", e);
            return mHostResources.getDrawableForDensity(id, density);
        }
    }

    @Override
    public Drawable getDrawableForDensity(int id, int density, Theme theme) {
        try {
            return super.getDrawableForDensity(id, density, theme);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getDrawableForDensity[id = " + id + "] from block failed ", e);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return mHostResources.getDrawableForDensity(id, density, theme);
            } else {
                throw e;
            }
        }
    }

    @Override
    public Movie getMovie(int id) throws NotFoundException {
        try {
            return super.getMovie(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getMovie[id = " + id + "] from block failed ", e);
            return mHostResources.getMovie(id);
        }
    }

    @Override
    public int getColor(int id) throws NotFoundException {
        try {
            return super.getColor(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getColor[id = " + id + "] from block failed ", e);
            return mHostResources.getColor(id);
        }
    }

    @Override
    public int getColor(int id, Theme theme) throws NotFoundException {
        try {
            return super.getColor(id, theme);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getColor[id = " + id + "] from block failed ", e);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return mHostResources.getColor(id, theme);
            } else {
                throw e;
            }
        }
    }

    @Override
    public ColorStateList getColorStateList(int id) throws NotFoundException {
        try {
            return super.getColorStateList(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getColorStateList[id = " + id + "] from block failed ", e);
            return mHostResources.getColorStateList(id);
        }
    }

    @Override
    public ColorStateList getColorStateList(int id, Theme theme) throws NotFoundException {
        try {
            return super.getColorStateList(id, theme);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getBoolean[id = " + id + "] from block failed ", e);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return mHostResources.getColorStateList(id, theme);
            } else {
                throw e;
            }
        }
    }

    @Override
    public boolean getBoolean(int id) throws NotFoundException {
        try {
            return super.getBoolean(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getBoolean[id = " + id + "] from block failed ", e);
            return mHostResources.getBoolean(id);
        }
    }

    @Override
    public int getInteger(int id) throws NotFoundException {
        try {
            return super.getInteger(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getInteger[id = " + id + "] from block failed ", e);
            return mHostResources.getInteger(id);
        }
    }

    @Override
    public XmlResourceParser getAnimation(int id) throws NotFoundException {
        try {
            return super.getAnimation(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getAnimation[id = " + id + "] from block failed ", e);
            return mHostResources.getAnimation(id);
        }
    }

    @Override
    public XmlResourceParser getXml(int id) throws NotFoundException {
        try {
            return super.getXml(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getXml[id = " + id + "] from block failed ", e);
            return mHostResources.getXml(id);
        }
    }

    @Override
    public InputStream openRawResource(int id) throws NotFoundException {
        try {
            return super.openRawResource(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources openRawResource[id = " + id + "] from block failed ", e);
            return mHostResources.openRawResource(id);
        }
    }

    @Override
    public InputStream openRawResource(int id, TypedValue value) throws NotFoundException {
        try {
            return super.openRawResource(id, value);
        } catch (NotFoundException e) {
            Logger.d("BlockResources openRawResource[id = " + id + "] from block failed ", e);
            return mHostResources.openRawResource(id, value);
        }
    }

    @Override
    public AssetFileDescriptor openRawResourceFd(int id) throws NotFoundException {
        try {
            return super.openRawResourceFd(id);
        } catch (NotFoundException e) {
            Logger.d("BlockResources openRawResourceFd[id = " + id + "] from block failed ", e);
            return mHostResources.openRawResourceFd(id);
        }
    }

    @Override
    public void getValue(int id, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        try {
            super.getValue(id, outValue, resolveRefs);
            if (!checkTypedValue(outValue)) {
                Logger.d("BlockResources getValue[id = " + id + "] from block success, but check failed");
                mHostResources.getValue(id, outValue, resolveRefs);
            }
        } catch (NotFoundException e) {
            Logger.d("BlockResources getValue[id = " + id + "] from block failed ", e);
            mHostResources.getValue(id, outValue, resolveRefs);
        }
    }

    @Override
    public void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        Logger.d("BlockResources getValueForDensity id = " + id);
        try {
            super.getValueForDensity(id, density, outValue, resolveRefs);
            if (!checkTypedValue(outValue)) {
                Logger.d("BlockResources getValueForDensity[id = " + id + "] from block success, but check failed");
                mHostResources.getValueForDensity(id, density, outValue, resolveRefs);
            }
        } catch (NotFoundException e) {
            Logger.d("BlockResources getValueForDensity[id = " + id + "] from block failed ", e);
            mHostResources.getValueForDensity(id, density, outValue, resolveRefs);
        }
    }

    @Override
    public void getValue(String name, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        try {
            super.getValue(name, outValue, resolveRefs);
            if (!checkTypedValue(outValue)) {
                Logger.d("BlockResources getValue[name = " + name + "] from block success, but check failed");
                mHostResources.getValue(name, outValue, resolveRefs);
            }
        } catch (NotFoundException e) {
            Logger.d("BlockResources getValue[name = " + name + "] from block failed ", e);
            mHostResources.getValue(name, outValue, resolveRefs);
        }
    }

    @Override
    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
        TypedArray typedArray = super.obtainAttributes(set, attrs);
        if (typedArray == null) {
            Logger.d("BlockResources obtainAttributes from block failed");
            typedArray = mHostResources.obtainAttributes(set, attrs);
        }
        return typedArray;
    }

    @Override
    public void updateConfiguration(Configuration config, DisplayMetrics metrics) {
        super.updateConfiguration(config, metrics);
    }

    @Override
    public DisplayMetrics getDisplayMetrics() {
        return super.getDisplayMetrics();
    }

    @Override
    public Configuration getConfiguration() {
        return super.getConfiguration();
    }

    @Override
    public String getResourceName(int resid) throws NotFoundException {
        try {
            return super.getResourceName(resid);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getResourceName[resid = " + resid + "] from block failed ", e);
            return mHostResources.getResourceName(resid);
        }
    }

    @Override
    public String getResourcePackageName(int resid) throws NotFoundException {
        try {
            return super.getResourcePackageName(resid);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getResourcePackageName[resid = " + resid + "] from block failed ", e);
            return mHostResources.getResourcePackageName(resid);
        }
    }

    @Override
    public String getResourceTypeName(int resid) throws NotFoundException {
        try {
            return super.getResourceTypeName(resid);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getResourceTypeName[resid = " + resid + "] from block failed ", e);
            return mHostResources.getResourceTypeName(resid);
        }
    }

    @Override
    public String getResourceEntryName(int resid) throws NotFoundException {
        try {
            return super.getResourceEntryName(resid);
        } catch (NotFoundException e) {
            Logger.d("BlockResources getResourceEntryName[resid = " + resid + "] from block failed ", e);
            return mHostResources.getResourceEntryName(resid);
        }
    }

    @Override
    public void parseBundleExtras(XmlResourceParser parser, Bundle outBundle) throws XmlPullParserException, IOException {
        super.parseBundleExtras(parser, outBundle);
    }

    @Override
    public void parseBundleExtra(String tagName, AttributeSet attrs, Bundle outBundle) throws XmlPullParserException {
        super.parseBundleExtra(tagName, attrs, outBundle);
    }

    private boolean checkTypedValue(TypedValue outValue) {
        /*int type = outValue.type;
        switch (type) {
            case TypedValue.TYPE_REFERENCE: {
                int resourceId = outValue.resourceId;
                String resourceTypeName = getResourceTypeName(resourceId);
                Logger.d("BlockResources checkTypedValue: resourceTypeName = " + resourceTypeName);
                if (TextUtils.equals(resourceTypeName, "drawable")
                        || TextUtils.equals(resourceTypeName, "mipmap")) {
                    try {
                        super.getDrawable(resourceId);
                    } catch (NotFoundException e) {
                        return false;
                    }
                }
                break;
            }
        }*/
        return true;
    }
}