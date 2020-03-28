package com.hyh.common.condition;

import android.content.Context;
import android.util.Log;

/**
 * @author Administrator
 * @description
 * @data 2018/7/5
 */

public class NotCondition implements ICondition {

    private ICondition mCondition;

    public NotCondition(ICondition condition) {
        this.mCondition = condition;
    }

    @Override
    public boolean isQualified(Context context) {
        boolean isQualified = mCondition.isQualified(context);
        Log.d("ICondition", "real condition:" + mCondition.getClass().getSimpleName() + ", qualified:" + !isQualified);
        return !isQualified;
    }
}