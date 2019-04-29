package com.hyh.filedownloader.sample.preference;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

/**
 * @author Administrator
 * @description
 * @data 2019/4/26
 */

public class Preference {





    @SuppressWarnings("unchecked")
    public <T> T create(Context context, Class<T> table) {
        if (context == null || table == null) return null;
        context = context.getApplicationContext();
        Table tableAnnotation = table.getAnnotation(Table.class);
        String tableName = null;
        int mode = Context.MODE_PRIVATE;
        if (tableAnnotation != null) {
            tableName = tableAnnotation.value();
            mode = tableAnnotation.mode();
        }
        PreferenceHandler handler = new PreferenceHandler(context, tableName, mode);
        return (T) Proxy.newProxyInstance(table.getClassLoader(), new Class[]{table}, handler);
    }

    private static class PreferenceHandler implements InvocationHandler {

        private final SharedPreferences mSharedPreferences;

        PreferenceHandler(Context context, String name, int mode) {
            mSharedPreferences = context.getSharedPreferences(name, mode);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return null;
        }
    }


    @Table("TableSample")
    public interface TableSample {

        TableSample test_putString(@Field("string_key") String string);

        TableSample test_putInt(@Field("int_key") int i);

        TableSample test_putLong(@Field("long_key") long l);

        TableSample test_putBoolean(@Field("boolean_key") boolean b);

        TableSample test_putStringSet(@Field("set_key") Set<String> set);

    }
}