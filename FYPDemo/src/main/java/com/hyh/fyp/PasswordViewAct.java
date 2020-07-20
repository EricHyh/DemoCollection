package com.hyh.fyp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.TreeMap;

/**
 * @author Administrator
 * @description
 * @data 2020/6/3
 */
public class PasswordViewAct extends Activity {

    private static final String TAG = "PasswordViewAct_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_view);


    }

    public void requestFocus(View view) {
        E<C, D> e = new E<C, D>() {
        };
        Log.d(TAG, "requestFocus: ");
    }

    public void clearPassword(View view) {
        TreeMap<String, Object> map = new TreeMap<>();
        map.put("a", 1);
        map.put("b", "2");
        map.put("c", false);
        map.put("d", new D("4"));
        String s = new Gson().toJson(map);

        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONArray names = jsonObject.names();
            Log.d(TAG, "clearPassword: ");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //TreeMap<String, String> smap = new Gson().fromJson(s,new TypeToken<TreeMap<String, String>>(){}.getType());
        F f = new Gson().fromJson(s, F.class);

        Method method = Test.class.getDeclaredMethods()[0];
        // public void com.test.Test.show(java.util.List[],java.lang.Object[],java.util.List,java.lang.String[],int[])
        System.out.println(method);

        Type[] types = method.getGenericParameterTypes();  // 这是 Method 中的方法
        for (Type type : types) {
            Log.d(TAG, "type: " + type);
        }

        Class<String[][]> aClass = String[][].class;

        Log.d(TAG, "clearPassword: ");
    }


    class Test<T> {
        public void show(List<String>[][] pTypeArray, String[][] arrays, T[] vTypeArray, List<String> list, String[] strings, int[] ints) {
        }
    }


    private static class A<T> {
        T value;
    }

    private static class B<T> {
        T value;
    }

    private static class C {
        String value;
    }

    private static class D {
        String value;

        public D(String value) {
            this.value = value;
        }
    }

    private static class E<T, F> {
        T value1;

        F value2;
    }

    private static class F {

        int a;
        String b;
        boolean c;
        D d;

    }

}