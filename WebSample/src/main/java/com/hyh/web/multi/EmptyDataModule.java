package com.hyh.web.multi;

import android.view.View;

/**
 * @author Administrator
 * @description
 * @data 2017/5/22
 */

public class EmptyDataModule extends SingleDataModule<Object> {

    public EmptyDataModule() {
        super(new Object());
    }

    public EmptyDataModule(int layoutId) {
        super(new Object(), layoutId);
    }

    public EmptyDataModule(View view) {
        super(new Object(), view);
    }

    @Override
    protected void initView(View parent, View view) {

    }

    @Override
    protected void bindDataAndEvent(Object obj) {
    }
}
