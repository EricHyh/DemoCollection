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

public class AndConditionGroup implements ICondition {

    private List<ICondition> mAndConditions = new CopyOnWriteArrayList<>();

    public AndConditionGroup(List<ICondition> andConditions) {
        if (andConditions != null && !andConditions.isEmpty()) {
            mAndConditions.addAll(andConditions);
        }
    }

    public AndConditionGroup(ICondition... andConditions) {
        if (andConditions != null && andConditions.length > 0) {
            mAndConditions.addAll(Arrays.asList(andConditions));
        }
    }

    public void add(ICondition condition) {
        if (condition == null) return;
        mAndConditions.add(condition);
    }

    @Override
    public boolean isQualified(Context context) {
        boolean isAndConditionsQualified = true;
        if (mAndConditions != null && !mAndConditions.isEmpty()) {
            for (ICondition andCondition : mAndConditions) {
                if (andCondition == null) {
                    continue;
                }
                if (!andCondition.isQualified(context)) {
                    isAndConditionsQualified = false;
                    break;
                }
            }
        }
        return isAndConditionsQualified;
    }
}