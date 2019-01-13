package com.hyh.download.db.dao;

import java.lang.reflect.Field;

/**
 * @author Administrator
 * @description
 * @data 2018/10/24
 */
class ColumnInfo {

    Field field;

    Class<?> type;

    int index;

    String columnName;

    boolean primaryKey;

    boolean unique;

    boolean notNull;

    ColumnInfo(Field field, Class<?> type, int index, String columnName, boolean primaryKey, boolean notNull, boolean unique) {
        this.field = field;
        this.type = type;
        this.index = index;
        this.columnName = columnName;
        this.primaryKey = primaryKey;
        this.notNull = notNull;
        this.unique = unique;
    }

}
