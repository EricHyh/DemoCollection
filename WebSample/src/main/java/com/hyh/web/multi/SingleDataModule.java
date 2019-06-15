package com.hyh.web.multi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * @author Administrator
 * @description
 * @data 2017/5/22
 */

public abstract class SingleDataModule<T> extends MultiModule<T> {

    private int mLayoutId;
    private View mView;

    public SingleDataModule(T t) {
        initData(t);
    }

    public SingleDataModule(T t, int layoutId) {
        initData(t);
        this.mLayoutId = layoutId;
    }

    public SingleDataModule(T t, View view) {
        initData(t);
        this.mView = view;
    }

    public void setLayoutId(int layoutId) {
        mLayoutId = layoutId;
    }

    public void setView(View view) {
        mView = view;
    }

    private void initData(T t) {
        ArrayList<T> list = new ArrayList<>();
        list.add(t);
        setDataList(list);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ItemHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemHolder<T> itemHolder = null;
        if (viewType == this.hashCode()) {
            if (mView == null) {
                if (mLayoutId != 0) {
                    mView = LayoutInflater.from(getContext()).inflate(mLayoutId, parent, false);
                }
            }
            if (mView != null) {
                itemHolder = new ItemHolder<T>(mView) {

                    @Override
                    protected void bindDataAndEvent() {
                        SingleDataModule.this.bindDataAndEvent(getData());
                    }

                    @Override
                    protected boolean isFullSpan(int position) {
                        return true;
                    }
                };
            }
        }
        if (itemHolder == null) {
            itemHolder = new EmptyHolder(new View(getContext()));
        }
        initView(parent, itemHolder.itemView);
        return itemHolder;
    }

    protected abstract void initView(View parent, View view);


    protected abstract void bindDataAndEvent(T t);


    @Override
    protected int getItemViewType(int position) {
        return this.hashCode();
    }


    @Override
    protected int getSpanSize(int spanCount, int position) {
        return spanCount;
    }
}
