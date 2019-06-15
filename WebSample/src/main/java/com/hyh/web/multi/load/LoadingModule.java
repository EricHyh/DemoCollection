package com.hyh.web.multi.load;

import android.view.View;

import com.hyh.web.multi.EmptyDataModule;


/**
 * @author Administrator
 * @description
 * @data 2017/11/24
 */

public abstract class LoadingModule extends EmptyDataModule {

    protected boolean isInRefresh;

    protected boolean isInLoadMore;

    protected boolean isNoMore;

    protected LoadingListener mLoadingListener;

    protected IFootView mFootView;

    protected View mFootContent;

    protected IRefreshViewClient mRefreshView;

    protected int mOldLoadState = LoadState.LOAD_STATE_IDLE;


    public LoadingModule(IFootView footView) {
        super();
        this.mFootView = footView;
        this.mFootContent = footView.onCreateView(this);
    }

    public void setLoadingListener(LoadingListener loadingListener) {
        mLoadingListener = loadingListener;
    }

    public void bindRefreshListener(IRefreshViewClient refreshView) {
        this.mRefreshView = refreshView;
        refreshView.setOnRefreshListener(new IRefreshViewClient.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mLoadingListener != null) {
                    isInRefresh = true;
                    mLoadingListener.onRefresh();
                }
            }
        });
    }

    public void setNoMore(boolean noMore) {
        isNoMore = noMore;
    }

    public void loadMoreComplete(boolean isSuccess) {
        if (isSuccess) {
            if (mFootView != null) {
                if (mOldLoadState != LoadState.LOAD_STATE_LOAD_SUCCESS) {
                    mFootView.onLoadStateChanged(mOldLoadState, LoadState.LOAD_STATE_LOAD_SUCCESS);
                }
                mOldLoadState = LoadState.LOAD_STATE_LOAD_SUCCESS;
            }
        } else {
            if (mFootView != null) {
                if (mOldLoadState != LoadState.LOAD_STATE_LOAD_FAILURE) {
                    mFootView.onLoadStateChanged(mOldLoadState, LoadState.LOAD_STATE_LOAD_FAILURE);
                }
                mOldLoadState = LoadState.LOAD_STATE_LOAD_FAILURE;
            }
        }
        isInLoadMore = false;
        if (mRefreshView != null) {
            mRefreshView.setTemporaryEnabled(true);
        }
    }

    public void refreshComplete() {
        if (mRefreshView != null) {
            mRefreshView.refreshComplete();
        }
        isInRefresh = false;
    }

    public abstract void executeLoadMore();

    public interface LoadingListener {
        void onRefresh();

        void onLoadMore();
    }
}