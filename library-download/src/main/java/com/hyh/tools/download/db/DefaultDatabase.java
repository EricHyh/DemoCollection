package com.hyh.tools.download.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.hyh.tools.download.bean.State;
import com.hyh.tools.download.db.bean.SQLiteConstants;
import com.hyh.tools.download.db.bean.TaskDBInfo;
import com.hyh.tools.download.db.dao.SQLiteHelper;
import com.hyh.tools.download.utils.FD_NumberUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2018/2/26
 */

public class DefaultDatabase implements Database {


    private SQLiteHelper mSqLiteHelper;

    public DefaultDatabase(Context context) {
        mSqLiteHelper = new SQLiteHelper(context);
    }


    @Override
    public void insertOrReplace(TaskDBInfo taskDBInfo) {
        if (taskDBInfo == null) {
            return;
        }
        String resKey = taskDBInfo.getResKey();
        if (TextUtils.isEmpty(resKey)) {
            return;
        }
        List<TaskDBInfo> taskDBInfos = selectByResKey(resKey);
        if (taskDBInfos == null || taskDBInfos.isEmpty()) {
            insert(taskDBInfo);
        } else {
            update(taskDBInfos.get(0), taskDBInfo);
        }
    }

    private void insert(TaskDBInfo taskDBInfo) {
        SQLiteDatabase database = mSqLiteHelper.getWritableDatabase();
        String table = SQLiteConstants.TABLE_NAME;
        ContentValues values = getValuesFromTaskDBInfo(taskDBInfo);
        database.insert(table, null, values);
        database.close();

    }

    private void update(TaskDBInfo oldTaskDBInfo, TaskDBInfo newTaskDBInfo) {
        String table = SQLiteConstants.TABLE_NAME;
        ContentValues values = getValuesFromTaskDBInfo(oldTaskDBInfo, newTaskDBInfo);
        if (values.size() <= 0) {
            return;
        }
        SQLiteDatabase database = mSqLiteHelper.getWritableDatabase();
        String whereClause = SQLiteConstants.RESOUCE_KEY.concat("=?");
        String[] whereArgs = {newTaskDBInfo.getResKey()};
        database.update(table, values, whereClause, whereArgs);
        database.close();
    }


    @Override
    public synchronized void delete(TaskDBInfo taskDBInfo) {
        if (taskDBInfo == null) {
            return;
        }
        String resKey = taskDBInfo.getResKey();
        if (TextUtils.isEmpty(resKey)) {
            return;
        }
        SQLiteDatabase database = mSqLiteHelper.getWritableDatabase();
        String table = SQLiteConstants.TABLE_NAME;
        String whereClause = SQLiteConstants.RESOUCE_KEY.concat("=?");
        String[] whereArgs = {resKey};
        database.delete(table, whereClause, whereArgs);
        database.close();
    }

    @Override
    public List<TaskDBInfo> selectAll() {
        return select(null, null);
    }

    @Override
    public List<TaskDBInfo> selectByResKey(String resKey) {
        String selection = SQLiteConstants.RESOUCE_KEY.concat("=?");
        String[] selectionArgs = {resKey};
        return select(selection, selectionArgs);
    }

    @Override
    public List<TaskDBInfo> selectByPackageName(String packageName) {
        String selection = SQLiteConstants.PACKAGE_NAME.concat("=?");
        String[] selectionArgs = {packageName};
        return select(selection, selectionArgs);
    }

    @Override
    public List<TaskDBInfo> selectByStatus(int status) {
        String selection = SQLiteConstants.CURRENT_STATUS.concat("=?");
        String[] selectionArgs = {String.valueOf(status)};
        return select(selection, selectionArgs);
    }


    private List<TaskDBInfo> select(String selection, String[] selectionArgs) {
        SQLiteDatabase database = mSqLiteHelper.getReadableDatabase();
        String table = SQLiteConstants.TABLE_NAME;
        String[] columns = {
                SQLiteConstants.ID,
                SQLiteConstants.RESOUCE_KEY,
                SQLiteConstants.CURRENT_STATUS,
                SQLiteConstants.PROGRESS,
                SQLiteConstants.VERSION_CODE,
                SQLiteConstants.RESPONSE_CODE,
                SQLiteConstants.RANGE_NUM,
                SQLiteConstants.TOTAL_SIZE,
                SQLiteConstants.CURRENT_SIZE,
                SQLiteConstants.TIME_MILLIS,
                SQLiteConstants.PACKAGE_NAME,
                SQLiteConstants.FILE_PATH,
                SQLiteConstants.WIFI_AUTO_RETRY,
                SQLiteConstants.TAG_JSON,
                SQLiteConstants.TAG_CLASS_NAME};
        Cursor cursor = database.query(table, columns, selection, selectionArgs, null, null, null);
        List<TaskDBInfo> taskDBInfos = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                TaskDBInfo taskDBInfo = new TaskDBInfo();

                long _id = cursor.isNull(0) ? -1 : cursor.getLong(0);
                String resKey = cursor.getString(1);
                int currentStatus = cursor.getInt(2);
                int progress = cursor.getInt(3);
                int versionCode = cursor.isNull(4) ? -1 : cursor.getInt(4);
                int responseCode = cursor.isNull(5) ? 0 : cursor.getInt(5);
                int rangeNum = cursor.isNull(6) ? 1 : cursor.getInt(6);
                long totalSize = cursor.isNull(7) ? 0 : cursor.getLong(7);
                long currentSize = cursor.isNull(8) ? 0 : cursor.getLong(8);
                long time = cursor.isNull(9) ? 0 : cursor.getLong(9);
                String packageName = cursor.getString(10);
                String filePath = cursor.getString(11);
                boolean wifiAutoRetry = !cursor.isNull(12) && cursor.getInt(12) == 1;
                String tagJson = cursor.getString(13);
                String tagClassName = cursor.getString(14);

                taskDBInfo.setId(_id);
                taskDBInfo.setResKey(resKey);
                taskDBInfo.setCurrentStatus(currentStatus);
                taskDBInfo.setProgress(progress);
                taskDBInfo.setVersionCode(versionCode);
                taskDBInfo.setResponseCode(responseCode);
                taskDBInfo.setRangeNum(rangeNum);
                taskDBInfo.setTotalSize(totalSize);
                taskDBInfo.setCurrentSize(currentSize);
                taskDBInfo.setTimeMillis(time);
                taskDBInfo.setPackageName(packageName);
                taskDBInfo.setFilePath(filePath);
                taskDBInfo.setWifiAutoRetry(wifiAutoRetry);
                taskDBInfo.setTagStr(tagJson);
                taskDBInfo.setTagClassName(tagClassName);

                taskDBInfos.add(taskDBInfo);
            }
            cursor.close();
        }
        database.close();
        return taskDBInfos;
    }

    private ContentValues getValuesFromTaskDBInfo(TaskDBInfo taskDBInfo) {
        ContentValues values = new ContentValues();
        values.put(SQLiteConstants.ID, taskDBInfo.getId());
        values.put(SQLiteConstants.RESOUCE_KEY, taskDBInfo.getResKey());
        values.put(SQLiteConstants.CURRENT_STATUS, taskDBInfo.getCurrentStatus());
        values.put(SQLiteConstants.PROGRESS, taskDBInfo.getProgress());
        values.put(SQLiteConstants.VERSION_CODE, taskDBInfo.getVersionCode());
        values.put(SQLiteConstants.RESPONSE_CODE, taskDBInfo.getResponseCode());
        values.put(SQLiteConstants.RANGE_NUM, taskDBInfo.getRangeNum());
        values.put(SQLiteConstants.TOTAL_SIZE, taskDBInfo.getTotalSize());
        values.put(SQLiteConstants.CURRENT_SIZE, taskDBInfo.getCurrentSize());
        values.put(SQLiteConstants.TIME_MILLIS, taskDBInfo.getTimeMillis());
        values.put(SQLiteConstants.PACKAGE_NAME, taskDBInfo.getPackageName());
        values.put(SQLiteConstants.FILE_PATH, taskDBInfo.getFilePath());
        values.put(SQLiteConstants.WIFI_AUTO_RETRY, (taskDBInfo.getWifiAutoRetry() ==
                null) ? 0 : (taskDBInfo.getWifiAutoRetry() ? 1 : 0));
        values.put(SQLiteConstants.TAG_JSON, taskDBInfo.getTagStr());
        values.put(SQLiteConstants.TAG_CLASS_NAME, taskDBInfo.getTagClassName());
        return values;
    }

    private ContentValues getValuesFromTaskDBInfo(TaskDBInfo oldTaskDBInfo, TaskDBInfo newTaskDBInfo) {
        ContentValues values = new ContentValues();
        int oldCurrentStatus = FD_NumberUtil.getValue(oldTaskDBInfo.getCurrentStatus(), State.NONE);
        int newCurrentStatus = FD_NumberUtil.getValue(newTaskDBInfo.getCurrentStatus(), State.NONE);
        if (oldCurrentStatus != newCurrentStatus) {
            values.put(SQLiteConstants.CURRENT_STATUS, newCurrentStatus);
        }
        int oldProgress = FD_NumberUtil.getValue(oldTaskDBInfo.getProgress(), 0);
        int newProgress = FD_NumberUtil.getValue(newTaskDBInfo.getProgress(), 0);
        if (oldProgress != newProgress) {
            values.put(SQLiteConstants.PROGRESS, newProgress);
        }
        int oldVersionCode = FD_NumberUtil.getValue(oldTaskDBInfo.getVersionCode(), -1);
        int newVersionCode = FD_NumberUtil.getValue(newTaskDBInfo.getVersionCode(), -1);
        if (oldVersionCode != newVersionCode) {
            values.put(SQLiteConstants.VERSION_CODE, newVersionCode);
        }
        int oldResponseCode = FD_NumberUtil.getValue(oldTaskDBInfo.getResponseCode(), 0);
        int newResponseCode = FD_NumberUtil.getValue(newTaskDBInfo.getResponseCode(), 0);
        if (oldResponseCode != newResponseCode) {
            values.put(SQLiteConstants.RESPONSE_CODE, newResponseCode);
        }
        int oldRangeNum = FD_NumberUtil.getValue(oldTaskDBInfo.getRangeNum(), 1);
        int newRangeNum = FD_NumberUtil.getValue(newTaskDBInfo.getRangeNum(), 1);
        if (oldRangeNum != newRangeNum) {
            values.put(SQLiteConstants.RANGE_NUM, newRangeNum);
        }
        long oldTotalSize = FD_NumberUtil.getValue(oldTaskDBInfo.getTotalSize(), 0L);
        long newTotalSize = FD_NumberUtil.getValue(newTaskDBInfo.getTotalSize(), 0L);
        if (oldTotalSize != newTotalSize) {
            values.put(SQLiteConstants.TOTAL_SIZE, newTotalSize);
        }
        long oldCurrentSize = FD_NumberUtil.getValue(oldTaskDBInfo.getCurrentSize(), 0L);
        long newCurrentSize = FD_NumberUtil.getValue(newTaskDBInfo.getCurrentSize(), 0L);
        if (oldCurrentSize != newCurrentSize) {
            values.put(SQLiteConstants.CURRENT_SIZE, newCurrentSize);
        }
        long oldTime = FD_NumberUtil.getValue(oldTaskDBInfo.getTimeMillis(), 0L);
        long newTime = FD_NumberUtil.getValue(newTaskDBInfo.getTimeMillis(), 0L);
        if (oldTime != newTime) {
            values.put(SQLiteConstants.TIME_MILLIS, newTime);
        }
        String oldPackageName = oldTaskDBInfo.getPackageName();
        String newPackageName = newTaskDBInfo.getPackageName();
        if (!TextUtils.equals(oldPackageName, newPackageName)) {
            values.put(SQLiteConstants.PACKAGE_NAME, newPackageName);
        }
        String oldFilePath = oldTaskDBInfo.getFilePath();
        String newFilePath = newTaskDBInfo.getFilePath();
        if (!TextUtils.equals(oldFilePath, newFilePath)) {
            values.put(SQLiteConstants.FILE_PATH, newFilePath);
        }
        int oldWifiAutoRetry = FD_NumberUtil.getValue(oldTaskDBInfo.getWifiAutoRetry(), false) ? 1 : 0;
        int newWifiAutoRetry = FD_NumberUtil.getValue(newTaskDBInfo.getWifiAutoRetry(), false) ? 1 : 0;
        if (oldWifiAutoRetry != newWifiAutoRetry) {
            values.put(SQLiteConstants.WIFI_AUTO_RETRY, newWifiAutoRetry);
        }
        String oldTagJson = oldTaskDBInfo.getTagStr();
        String newTagJson = newTaskDBInfo.getTagStr();
        if (!TextUtils.equals(oldTagJson, newTagJson)) {
            values.put(SQLiteConstants.TAG_JSON, newTagJson);
        }
        String oldTagClassName = oldTaskDBInfo.getTagClassName();
        String newTagClassName = newTaskDBInfo.getTagClassName();
        if (!TextUtils.equals(oldTagClassName, newTagClassName)) {
            values.put(SQLiteConstants.TAG_CLASS_NAME, newTagClassName);
        }
        return values;
    }

}
