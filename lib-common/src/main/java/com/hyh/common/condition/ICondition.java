package com.hyh.common.condition;

import android.content.Context;

/**
 * @author Administrator
 * @description 判断是否符合条件的接口
 * @data 2017/12/15
 */
public interface ICondition {

    /**
     * 判断是否符合条件的接口
     *
     * @return 满足某条件时，返回true，否则返回false
     */
    boolean isQualified(Context context);

}