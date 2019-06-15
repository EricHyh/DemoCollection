package com.hyh.web.multi.load;

import android.support.v4.widget.SwipeRefreshLayout;

/**
 * @author Administrator
 * @description
 * @data 2017/11/25
 */

public class SwipeRefreshClient implements IRefreshViewClient, SwipeRefreshLayout.OnRefreshListener {


    private SwipeRefreshLayout mRefreshLayout;

    private OnRefreshListener mListener;

    private boolean isEnabled = true;

    public void setAbsoluteEnabled(boolean enabled) {
        isEnabled = enabled;
        mRefreshLayout.setEnabled(isEnabled);
    }

    public SwipeRefreshClient(SwipeRefreshLayout refreshLayout) {
        mRefreshLayout = refreshLayout;
        mRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void executeRefresh() {
        if (isEnabled) {
            mRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mRefreshLayout.setRefreshing(true);
                    onRefresh();
                }
            });
        }
    }

    @Override
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mListener = listener;
    }

    @Override
    public void setTemporaryEnabled(boolean enabled) {
        if (isEnabled) {
            mRefreshLayout.setEnabled(enabled);
        }
    }

    @Override
    public void refreshComplete() {
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        if (mListener != null) {
            mListener.onRefresh();
        }
    }
}
