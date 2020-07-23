package com.hyh.fyp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

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


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewGroup contentView = findViewById(android.R.id.content);
                View view = new View(getApplicationContext()) {
                    @Override
                    public void dispatchWindowFocusChanged(boolean hasFocus) {
                        super.dispatchWindowFocusChanged(hasFocus);
                    }

                    @Override
                    protected void onAttachedToWindow() {
                        super.onAttachedToWindow();
                        int[] drawableState = getDrawableState();
                        boolean hasFocus = false;
                        for (int state : drawableState) {
                            if (state == android.R.attr.state_window_focused) {
                                hasFocus = true;
                                break;
                            }
                        }
                        Log.d(TAG, "onAttachedToWindow: hasFocus = " + hasFocus);
                        Log.d(TAG, "onAttachedToWindow: hasWindowFocus = " + hasWindowFocus());
                    }
                };
                view.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
                view.setBackgroundColor(Color.RED);
                contentView.addView(view);
            }
        }, 5000);
    }

    public void requestFocus(View view) {
        E<C, D> e = new E<C, D>() {
        };


        int add = add(1, 1);
        Log.d(TAG, "add after return1: " + add);
        add = add(1, 2);
        Log.d(TAG, "add after return2: " + add);
        add = add(1, 3);
        Log.d(TAG, "add after return3: " + add);
    }

    public void clearPassword(View view) {

        /*TreeMap<D, F> map1 = new TreeMap<>();
        map1.put(new D("1"), new F(1, "1", false));
        map1.put(new D("2"), new F(2, "2", true));
        map1.put(new D("3"), new F(3, "3", false));
        map1.put(new D("4"), new F(4, "4", true));
        String s1 = new Gson().toJson(map1);
        Log.d(TAG, "clearPassword: ");

        Object json = new Gson().fromJson(s1, new TypeToken<TreeMap<D, F>>() {
        }.getType());
        Log.d(TAG, "clearPassword: ");


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

        Log.d(TAG, "clearPassword: ");*/
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

    private static class D implements Comparable<D> {
        String value;

        public D(String value) {
            this.value = value;
        }

        @Override
        public int compareTo(@NonNull D o) {
            return value.compareTo(o.value);
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

        public F() {
        }

        public F(int a, String b, boolean c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }

    private int add(int a, int b) {
        try {
            Log.d(TAG, "add try: ");
            return readAdd(a, b);
        } finally {
            Log.d(TAG, "add finally: " + (a + b));
        }
    }

    private int readAdd(int a, int b) {
        Log.d(TAG, "readAdd: ");
        return a + b;
    }
}