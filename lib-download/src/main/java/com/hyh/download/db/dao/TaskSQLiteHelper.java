package com.hyh.download.db.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Administrator
 * @description
 * @data 2018/12/20
 */

public class TaskSQLiteHelper extends SQLiteOpenHelper {

    private static final String NAME = "FileDownloader.db";

    private static final int VERSION = 1;

    TaskSQLiteHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        TaskInfoDao.createTable(db, false);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
