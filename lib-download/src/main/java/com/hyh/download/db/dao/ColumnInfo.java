package com.hyh.download.db.dao;

import java.lang.reflect.Field;

/**
 * @author Administrator
 * @description
 * @data 2018/10/24
 */

public class ColumnInfo {

    Field field;

    Class<?> type;

    String columnName;

    boolean primaryKey;

    boolean unique;

    boolean notNull;

    public ColumnInfo(Field field, Class<?> type, String columnName, boolean primaryKey, boolean notNull, boolean unique) {
        this.field = field;
        this.type = type;
        this.columnName = columnName;
        this.primaryKey = primaryKey;
        this.notNull = notNull;
        this.unique = unique;
    }

    public Field getField() {
        return field;
    }

    public Class<?> getType() {
        return type;
    }

    public String getColumnName() {
        return columnName;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public boolean isUnique() {
        return unique;
    }
}
