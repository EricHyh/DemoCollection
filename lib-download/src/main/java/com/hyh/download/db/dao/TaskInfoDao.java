package com.hyh.download.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.hyh.download.State;
import com.hyh.download.db.annotation.Column;
import com.hyh.download.db.annotation.Id;
import com.hyh.download.db.annotation.NotNull;
import com.hyh.download.db.annotation.Unique;
import com.hyh.download.db.bean.TaskInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Administrator
 * @description
 * @data 2018/12/20
 */

public class TaskInfoDao {

    private static final String TABLE_NAME = "TaskInfo";

    private final static String[] sColumnNames = {"_id", "resKey",
            "requestUrl", "targetUrl", "cacheRequestUrl", "cacheTargetUrl",
            "versionCode", "priority", "fileDir", "filePath", "byMultiThread",
            "rangeNum", "totalSize", "currentSize", "progress", "currentStatus",
            "onlyWifiDownload", "wifiAutoRetry", "permitRetryInMobileData", "permitRetryInvalidFileTask", "permitRecoverTask",
            "responseCode", "failureCode", "eTag", "lastModified", "updateTimeMillis", "tag"};

    private static Map<String, ColumnInfo> sColumns = new TreeMap<>(new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            if (TextUtils.equals(o1, "_id")) {
                return -1;
            }
            return 1;
        }
    });

    static {
        loadColumns();
    }

    private static void loadColumns() {
        Field[] declaredFields = TaskInfo.class.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            Column column = declaredField.getAnnotation(Column.class);
            if (column == null) {
                continue;
            }
            Class<?> type = declaredField.getType();
            String columnName = column.nameInDb();

            boolean primaryKey = false;
            boolean notNull = false;
            boolean unique = false;

            Id id = declaredField.getAnnotation(Id.class);
            if (id != null) {
                primaryKey = true;
            }
            NotNull NotNull = declaredField.getAnnotation(NotNull.class);
            if (NotNull != null) {
                notNull = true;
            }
            Unique Unique = declaredField.getAnnotation(Unique.class);
            if (Unique != null) {
                unique = true;
            }
            sColumns.put(columnName, new ColumnInfo(declaredField, type, columnName, primaryKey, notNull, unique));
        }
    }

    static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        StringBuilder sb = new StringBuilder("CREATE TABLE " + constraint + " " + TABLE_NAME + " (");
        Collection<ColumnInfo> columnInfos = sColumns.values();
        for (ColumnInfo columnInfo : columnInfos) {
            Class<?> type = columnInfo.getType();
            String columnName = columnInfo.getColumnName();
            String columnType = getColumnType(type);
            sb.append(columnName).append(" ").append(columnType);
            if (columnInfo.isPrimaryKey()) {
                sb.append(" PRIMARY KEY AUTOINCREMENT");
            } else {
                boolean unique = columnInfo.isUnique();
                boolean notNull = columnInfo.isNotNull();
                if (notNull) {
                    sb.append(" NOT NULL");
                }
                if (unique) {
                    sb.append(" UNIQUE");
                }
            }
            sb.append(" ,");
        }
        String substring = sb.substring(0, sb.length() - 1);
        String sql = substring + ");";
        db.execSQL(sql);
    }

    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + TABLE_NAME;
        db.execSQL(sql);
    }

    private static String getColumnType(Class<?> type) {
        String columnType = null;
        if (type.equals(byte.class) || type.equals(Byte.class)) {
            columnType = "INTEGER";
        } else if (type.equals(short.class) || type.equals(Short.class)) {
            columnType = "INTEGER";
        } else if (type.equals(int.class) || type.equals(Integer.class)) {
            columnType = "INTEGER";
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            columnType = "INTEGER";
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            columnType = "INTEGER";
        } else if (type.equals(float.class) || type.equals(Float.class)) {
            columnType = "REAL";
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            columnType = "REAL";
        } else if (type.equals(String.class)) {
            columnType = "TEXT";
        }
        return columnType;
    }

    private final TaskSQLiteHelper mSqLiteHelper;

    public TaskInfoDao(Context context) {
        mSqLiteHelper = new TaskSQLiteHelper(context);
    }

    public synchronized void insertOrUpdate(TaskInfo taskInfo) {
        if (isExist(taskInfo)) {
            update(taskInfo);
        } else {
            insert(taskInfo);
        }
    }

    public synchronized void delete(String resKey) {
        SQLiteDatabase db = mSqLiteHelper.getWritableDatabase();
        String whereClause = "resKey=?";
        String[] whereArgs = {resKey};
        db.delete(TABLE_NAME, whereClause, whereArgs);
        db.close();
    }

    public synchronized TaskInfo getTaskInfoByKey(String resKey) {
        TaskInfo taskInfo = null;
        SQLiteDatabase db = mSqLiteHelper.getReadableDatabase();
        String[] columns = sColumnNames;
        String selection = "resKey=?";
        String[] selectionArgs = {resKey};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        Cursor cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy);
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            taskInfo = readTaskInfo(cursor);
            cursor.close();
        }
        db.close();
        return taskInfo;
    }


    public synchronized List<TaskInfo> queryInterruptedTask() {
        List<TaskInfo> taskInfoList = null;
        SQLiteDatabase db = mSqLiteHelper.getReadableDatabase();
        String[] columns = sColumnNames;
        String selection = "currentStatus in (?, ?, ?, ?)";
        String[] selectionArgs = {String.valueOf(State.PREPARE),
                String.valueOf(State.WAITING_IN_QUEUE),
                String.valueOf(State.CONNECTED),
                String.valueOf(State.DOWNLOADING)};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        Cursor cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy);
        if (null != cursor) {
            int count = cursor.getCount();
            if (count > 0 && cursor.moveToFirst()) {
                taskInfoList = new ArrayList<>();
                do {
                    taskInfoList.add(readTaskInfo(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return taskInfoList;
    }

    public synchronized List<TaskInfo> queryAllTask() {
        List<TaskInfo> taskInfoList = null;
        SQLiteDatabase db = mSqLiteHelper.getReadableDatabase();
        String[] columns = sColumnNames;
        String selection = null;
        String[] selectionArgs = null;
        String groupBy = null;
        String having = null;
        String orderBy = null;
        Cursor cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy);
        if (null != cursor) {
            int count = cursor.getCount();
            if (count > 0 && cursor.moveToFirst()) {
                taskInfoList = new ArrayList<>();
                do {
                    taskInfoList.add(readTaskInfo(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return taskInfoList;
    }


    private boolean isExist(TaskInfo taskInfo) {
        return isExist(taskInfo.getResKey());
    }

    private boolean isExist(String resKey) {
        boolean isExist = false;
        SQLiteDatabase db = mSqLiteHelper.getReadableDatabase();
        String[] columns = {"resKey"};
        String selection = "resKey=?";
        String[] selectionArgs = {resKey};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        Cursor cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy);
        if (cursor != null) {
            isExist = cursor.getCount() > 0;
            cursor.close();
        }
        db.close();
        return isExist;
    }

    private void insert(TaskInfo taskInfo) {
        SQLiteDatabase db = mSqLiteHelper.getWritableDatabase();
        ContentValues values = newContentValues(taskInfo);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    private void update(TaskInfo taskInfo) {
        SQLiteDatabase db = mSqLiteHelper.getWritableDatabase();
        ContentValues values = newContentValues(taskInfo);
        String whereClause = "resKey=?";
        String[] whereArgs = {taskInfo.getResKey()};
        db.update(TABLE_NAME, values, whereClause, whereArgs);
        db.close();
    }

    /**
     * "responseCode",
     * "failureCode", "eTag", "lastModified", "updateTimeMillis", "tag"};
     */
    private TaskInfo readTaskInfo(Cursor cursor) {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setId(cursor.getLong(0));
        taskInfo.setResKey(cursor.getString(1));

        taskInfo.setRequestUrl(cursor.getString(2));
        taskInfo.setTargetUrl(cursor.getString(3));
        taskInfo.setCacheTargetUrl(cursor.getString(4));
        taskInfo.setCacheTargetUrl(cursor.getString(5));

        taskInfo.setVersionCode(cursor.getInt(6));
        taskInfo.setPriority(cursor.getInt(7));

        taskInfo.setFileDir(cursor.getString(8));
        taskInfo.setFilePath(cursor.getString(9));

        taskInfo.setByMultiThread(cursor.getInt(10) == 1);
        taskInfo.setRangeNum(cursor.getInt(11));

        taskInfo.setTotalSize(cursor.getLong(12));
        taskInfo.setCurrentSize(cursor.getLong(13));
        taskInfo.setProgress(cursor.getInt(14));

        taskInfo.setCurrentStatus(cursor.getInt(15));

        taskInfo.setOnlyWifiDownload(cursor.getInt(16) == 1);
        taskInfo.setWifiAutoRetry(cursor.getInt(17) == 1);
        taskInfo.setPermitRetryInMobileData(cursor.getInt(18) == 1);
        taskInfo.setPermitRetryInvalidFileTask(cursor.getInt(19) == 1);
        taskInfo.setPermitRecoverTask(cursor.getInt(20) == 1);

        taskInfo.setResponseCode(cursor.getInt(21));
        taskInfo.setFailureCode(cursor.getInt(22));
        taskInfo.setETag(cursor.getString(23));
        taskInfo.setLastModified(cursor.getString(24));
        taskInfo.setUpdateTimeMillis(cursor.getLong(25));
        taskInfo.setTag(cursor.getString(26));
        return taskInfo;
    }

    private ContentValues newContentValues(TaskInfo taskInfo) {
        ContentValues contentValues = new ContentValues();
        long id = taskInfo.getId();
        if (id >= 0) {
            contentValues.put("_id", id);
        }
        contentValues.put("resKey", taskInfo.getResKey());

        contentValues.put("requestUrl", taskInfo.getRequestUrl());
        contentValues.put("targetUrl", taskInfo.getTargetUrl());
        contentValues.put("cacheRequestUrl", taskInfo.getCacheRequestUrl());
        contentValues.put("cacheTargetUrl", taskInfo.getCacheTargetUrl());

        contentValues.put("versionCode", taskInfo.getVersionCode());
        contentValues.put("priority", taskInfo.getPriority());
        contentValues.put("fileDir", taskInfo.getFileDir());
        contentValues.put("filePath", taskInfo.getFilePath());
        contentValues.put("byMultiThread", taskInfo.isByMultiThread() ? 1 : 0);
        contentValues.put("rangeNum", taskInfo.getRangeNum());
        contentValues.put("totalSize", taskInfo.getTotalSize());
        contentValues.put("currentSize", taskInfo.getCurrentSize());
        contentValues.put("progress", taskInfo.getProgress());
        contentValues.put("currentStatus", taskInfo.getCurrentStatus());

        contentValues.put("onlyWifiDownload", taskInfo.isOnlyWifiDownload() ? 1 : 0);
        contentValues.put("wifiAutoRetry", taskInfo.isWifiAutoRetry() ? 1 : 0);
        contentValues.put("permitRetryInMobileData", taskInfo.isPermitRetryInMobileData() ? 1 : 0);
        contentValues.put("permitRetryInvalidFileTask", taskInfo.isPermitRetryInvalidFileTask() ? 1 : 0);
        contentValues.put("permitRecoverTask", taskInfo.isPermitRecoverTask() ? 1 : 0);

        contentValues.put("responseCode", taskInfo.getResponseCode());
        contentValues.put("failureCode", taskInfo.getFailureCode());
        contentValues.put("eTag", taskInfo.getETag());
        contentValues.put("lastModified", taskInfo.getLastModified());
        contentValues.put("updateTimeMillis", taskInfo.getUpdateTimeMillis());
        contentValues.put("tag", taskInfo.getTag());
        return contentValues;
    }
}
