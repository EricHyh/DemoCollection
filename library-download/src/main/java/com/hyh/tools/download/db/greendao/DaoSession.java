package com.hyh.tools.download.db.greendao;

import com.hyh.tools.download.db.bean.TaskDBInfo;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import java.util.Map;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig taskDBInfoDaoConfig;

    private final TaskDBInfoDao taskDBInfoDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        taskDBInfoDaoConfig = daoConfigMap.get(TaskDBInfoDao.class).clone();
        taskDBInfoDaoConfig.initIdentityScope(type);

        taskDBInfoDao = new TaskDBInfoDao(taskDBInfoDaoConfig, this);

        registerDao(TaskDBInfo.class, taskDBInfoDao);
    }
    
    public void clear() {
        taskDBInfoDaoConfig.clearIdentityScope();
    }

    public TaskDBInfoDao getTaskDBInfoDao() {
        return taskDBInfoDao;
    }

}