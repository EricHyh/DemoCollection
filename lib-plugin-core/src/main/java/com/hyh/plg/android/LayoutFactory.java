package com.hyh.plg.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.reflect.Constructor;

/**
 * @author Administrator
 * @description
 * @data 2018/3/16
 */

public class LayoutFactory implements LayoutInflater.Factory2 {

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        try {
            Class<?> viewClass = getClass().getClassLoader().loadClass(name);
            Constructor<?> constructor = viewClass.getConstructor(Context.class, AttributeSet.class);
            return (View) constructor.newInstance(context, attrs);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return onCreateView(null, name, context, attrs);
    }
}
