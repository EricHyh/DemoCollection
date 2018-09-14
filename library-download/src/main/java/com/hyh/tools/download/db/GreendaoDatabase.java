package com.hyh.tools.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.hyh.tools.download.db.bean.SQLiteConstants;
import com.hyh.tools.download.db.bean.TaskDBInfo;
import com.hyh.tools.download.db.greendao.DaoMaster;
import com.hyh.tools.download.db.greendao.DaoSession;
import com.hyh.tools.download.db.greendao.TaskDBInfoDao;

import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2018/2/26
 */

public class GreendaoDatabase implements Database {

    private TaskDBInfoDao mDao;

    public GreendaoDatabase(Context context) {
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(context
                .getApplicationContext(), SQLiteConstants.DB_FILE_NAME);
        SQLiteDatabase db = devOpenHelper.getWritableDatabase();
        DaoMaster master = new DaoMaster(db);
        DaoSession session = master.newSession();
        this.mDao = session.getTaskDBInfoDao();
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
        mDao.insertOrReplace(taskDBInfo);
    }

    @Override
    public void delete(TaskDBInfo taskDBInfo) {
        if (taskDBInfo == null) {
            return;
        }
        String resKey = taskDBInfo.getResKey();
        if (TextUtils.isEmpty(resKey)) {
            return;
        }
        mDao.delete(taskDBInfo);
    }

    @Override
    public List<TaskDBInfo> selectAll() {
        return mDao.loadAll();
    }

    @Override
    public List<TaskDBInfo> selectByResKey(String resKey) {
        Query<TaskDBInfo> query = mDao.queryBuilder().where(TaskDBInfoDao.Properties.ResKey.eq(resKey)).build();
        if (query != null && query.list() != null && query.list().size() > 0) {
            return query.list();
        } else {
            return null;
        }
    }

    @Override
    public List<TaskDBInfo> selectByPackageName(String packageName) {
        Query<TaskDBInfo> query = mDao.queryBuilder().where(TaskDBInfoDao.Properties.PackageName.eq(packageName)).build();
        if (query != null && query.list() != null && query.list().size() > 0) {
            return query.list();
        } else {
            return null;
        }
    }

    @Override
    public List<TaskDBInfo> selectByStatus(int status) {
        Query<TaskDBInfo> query = mDao.queryBuilder()
                .where(TaskDBInfoDao.Properties.CurrentStatus.eq(status))
                .orderDesc(TaskDBInfoDao.Properties.TimeMillis)
                .build();
        if (query != null && query.list() != null) {
            return query.list();
        }
        return new ArrayList<>();
    }
}
