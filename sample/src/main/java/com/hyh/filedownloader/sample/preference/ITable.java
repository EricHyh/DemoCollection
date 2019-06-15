package com.hyh.filedownloader.sample.preference;

import android.content.SharedPreferences;

import java.util.Map;
import java.util.Set;

/**
 * Created by Eric_He on 2019/4/27.
 */

public interface ITable {

    Map<String, Object> getAll();

    boolean getBoolean(String key);

    boolean getBoolean(String key, boolean defaultValue);

    ITable putBoolean(String key, boolean value);


    String getString(String key);

    String getString(String key, String defaultValue);

    ITable putString(String key, String value);


    int getInt(String key);

    int getInt(String key, int defaultValue);

    ITable putInt(String key, int value);


    long getLong(String key);

    long getLong(String key, long defaultValue);

    ITable putLong(String key, long value);


    Set<String> getStringSet(String key);

    Set<String> getStringSet(String key, Set<String> defaultValue);

    ITable putStringSet(String key, Set<String> value);


    boolean contains(String key);

    void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener);

    void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener);


    @interface Register {

    }

    @interface UnRegister {

    }
}