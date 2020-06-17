package com.hyh.fyp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.hyh.fyp.cache.DiskLruCache;

/**
 * @author Administrator
 * @description
 * @data 2020/3/4
 */
public class TestCacheActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testcache);
    }

    public void saveCache(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DiskLruCache diskLruCache = DiskLruCache.open(getExternalFilesDir("test1"), 1, 1, 5);
                    {
                        DiskLruCache.Editor edit = diskLruCache.edit("1");
                        edit.set(0, "1111");
                        edit.commit();
                    }
                    {
                        DiskLruCache.Editor edit = diskLruCache.edit("2");
                        edit.set(0, "2222");
                        edit.commit();
                    }
                    {
                        DiskLruCache.Editor edit = diskLruCache.edit("3");
                        edit.set(0, "3333");
                        edit.commit();
                    }
                    {
                        DiskLruCache.Editor edit = diskLruCache.edit("4");
                        edit.set(0, "4444");
                        edit.commit();
                    }
                    {
                        DiskLruCache.Editor edit = diskLruCache.edit("5");
                        edit.set(0, "5555");
                        edit.commit();
                    }
                    {
                        DiskLruCache.Editor edit = diskLruCache.edit("6");
                        edit.set(0, "6666");
                        edit.commit();
                    }
                    {
                        DiskLruCache.Editor edit = diskLruCache.edit("7");
                        edit.set(0, "fdsfffffffffffffffffffffffffffffffffffffffffffffff");
                        edit.commit();
                    }
                    {
                        DiskLruCache.Editor edit = diskLruCache.edit("8");
                        edit.set(0, "fdsfffffffffffffffffffffffffffffffffffffffffffffff");
                        edit.commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void getCache(View view) {

    }
}
