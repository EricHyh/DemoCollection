package com.hyh.tools.download.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.hyh.tools.download.db.DefaultDatabase;
import com.hyh.tools.download.db.bean.TaskDBInfo;

import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2018/2/28
 */

public class TestDatabaseActivity extends Activity {

    DefaultDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.hyh.tools.download.test.R.layout.activity_testdatabase);
        mDatabase = new DefaultDatabase(getApplication());
    }


    public void insertOrReplace(View view) {
        String[] resKeyList = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        String[] packageNameList = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};
        int[] statusList = {0, 1, 2, 3};
        for (int i = 0; i < 1000; i++) {
            TaskDBInfo taskDBInfo = new TaskDBInfo();
            taskDBInfo.setResKey(resKeyList[i % 10]);
            taskDBInfo.setPackageName(packageNameList[i % 10]);
            taskDBInfo.setCurrentStatus(statusList[i % 4]);
            mDatabase.insertOrReplace(taskDBInfo);
        }
    }

    public void delete(View view) {
        TaskDBInfo taskDBInfo = new TaskDBInfo();
        taskDBInfo.setResKey("1");
        mDatabase.delete(taskDBInfo);

        taskDBInfo = new TaskDBInfo();
        taskDBInfo.setResKey("11");
        mDatabase.delete(taskDBInfo);
    }

    public void selectAll(View view) {
        List<TaskDBInfo> taskDBInfos = mDatabase.selectAll();
        Log.d("TAG", "selectAll: ");
    }

    public void selectByResKey(View view) {
        List<TaskDBInfo> taskDBInfos = mDatabase.selectByResKey("1");
        Log.d("TAG", "selectByResKey: ");
    }

    public void selectByPackageName(View view) {
        List<TaskDBInfo> taskDBInfos = mDatabase.selectByPackageName("b");
        Log.d("TAG", "selectByPackageName: ");
    }

    public void selectByStatus(View view) {
        List<TaskDBInfo> taskDBInfos = mDatabase.selectByStatus(0);
        Log.d("TAG", "selectByStatus: ");
    }
}
