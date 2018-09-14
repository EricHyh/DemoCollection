package com.hyh.tools.download.db;

import com.hyh.tools.download.db.bean.TaskDBInfo;

import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2018/2/26
 */

public interface Database {

    void insertOrReplace(TaskDBInfo taskDBInfo);

    void delete(TaskDBInfo taskDBInfo);

    List<TaskDBInfo> selectAll();

    List<TaskDBInfo> selectByResKey(String resKey);

    List<TaskDBInfo> selectByPackageName(String packageName);

    List<TaskDBInfo> selectByStatus(int status);

}
