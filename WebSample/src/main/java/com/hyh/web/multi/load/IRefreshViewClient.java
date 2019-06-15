package com.hyh.web.multi.load;

/**
 * @author Administrator
 * @description
 * @data 2017/11/25
 */

public interface IRefreshViewClient {

    void executeRefresh();

    void setOnRefreshListener(OnRefreshListener listener);

    /**
     * 在加载更多的过程中，需要禁止刷新数据
     *
     * @param enabled
     */
    void setTemporaryEnabled(boolean enabled);

    void refreshComplete();

    interface OnRefreshListener {

        void onRefresh();

    }
}
