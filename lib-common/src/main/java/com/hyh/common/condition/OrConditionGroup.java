package com.hyh.common.condition;

import android.content.Context;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Administrator
 * @description
 * @data 2018/7/5
 */
public class OrConditionGroup implements ICondition {

    private List<ICondition> mOrConditions = new CopyOnWriteArrayList<>();

    public OrConditionGroup(List<? extends ICondition> orConditions) {
        if (orConditions != null && !orConditions.isEmpty()) {
            mOrConditions.addAll(orConditions);
        }
    }

    public OrConditionGroup(ICondition... orConditions) {
        if (orConditions != null && orConditions.length > 0) {
            mOrConditions.addAll(Arrays.asList(orConditions));
        }
    }

    public void add(ICondition condition) {
        if (condition == null) return;
        mOrConditions.add(condition);
    }

    @Override
    public boolean isQualified(Context context) {
        boolean isOrConditionsQualified = false;
        if (mOrConditions != null && !mOrConditions.isEmpty()) {
            for (ICondition orCondition : mOrConditions) {
                if (orCondition == null) {
                    continue;
                }
                if (orCondition.isQualified(context)) {
                    isOrConditionsQualified = true;
                    break;
                }
            }
        } else {
            isOrConditionsQualified = true;
        }
        return isOrConditionsQualified;
    }
}