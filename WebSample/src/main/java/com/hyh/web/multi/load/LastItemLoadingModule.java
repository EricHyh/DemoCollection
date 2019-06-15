package com.hyh.web.multi.load;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * @author Administrator
 * @description
 * @data 2017/11/24
 */

public class LastItemLoadingModule extends LoadingModule {

    private RecyclerView mRecyclerView;

    private ILastVisibleItemPositionFinder mLastVisibleItemPositionFinder;

    public LastItemLoadingModule(IFootView footView) {
        super(footView);
        setView(mFootContent);
    }

    public void bindScrollListener(RecyclerView recyclerView) {
        bindScrollListener(recyclerView, null);
    }

    public void bindScrollListener(RecyclerView recyclerView, ILastVisibleItemPositionFinder finder) {
        this.mRecyclerView = recyclerView;
        recyclerView.addOnScrollListener(new LoadMore(this));
        if (finder == null) {
            mLastVisibleItemPositionFinder = new ILastVisibleItemPositionFinder() {
                @Override
                public int find(RecyclerView recyclerView) {
                    int lastVisibleItemPosition;
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    if (layoutManager instanceof GridLayoutManager) {
                        lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                    } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                        int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                        ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
                        lastVisibleItemPosition = findMax(into);
                    } else {
                        lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    }
                    return lastVisibleItemPosition;
                }
            };
        } else {
            mLastVisibleItemPositionFinder = finder;
        }
    }

    @Override
    public void refreshComplete() {
        super.refreshComplete();
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                onScrollStateChanged(mRecyclerView, RecyclerView.SCROLL_STATE_IDLE);
            }
        });
    }

    @Override
    public void executeLoadMore() {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                onScrollStateChanged(mRecyclerView, RecyclerView.SCROLL_STATE_IDLE);
            }
        });
    }

    @Override
    protected void initView(View parent, View view) {
    }

    private void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (newState != RecyclerView.SCROLL_STATE_IDLE
                || mLoadingListener == null
                || isInLoadMore) {
            return;
        }
        final int lastVisibleItemPosition = mLastVisibleItemPositionFinder.find(recyclerView);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager.getChildCount() > 0
                && lastVisibleItemPosition >= layoutManager.getItemCount() - 1
                && layoutManager.getItemCount() >= layoutManager.getChildCount()) {
            if (isNoMore) {
                if (mOldLoadState != LoadState.LOAD_STATE_NO_MORE) {
                    if (mFootView != null) {
                        mFootView.onLoadStateChanged(mOldLoadState, LoadState.LOAD_STATE_NO_MORE);
                    }
                    mOldLoadState = LoadState.LOAD_STATE_NO_MORE;
                }
            } else {
                if (isInRefresh) {
                    if (mOldLoadState != LoadState.LOAD_STATE_REFRESHING) {
                        if (mFootView != null) {
                            mFootView.onLoadStateChanged(mOldLoadState, LoadState.LOAD_STATE_REFRESHING);
                        }
                        mOldLoadState = LoadState.LOAD_STATE_REFRESHING;
                    }
                } else {
                    isInLoadMore = true;
                    if (mOldLoadState != LoadState.LOAD_STATE_LOADING) {
                        if (mFootView != null) {
                            mFootView.onLoadStateChanged(mOldLoadState, LoadState.LOAD_STATE_LOADING);
                        }
                        mOldLoadState = LoadState.LOAD_STATE_LOADING;
                    }
                    if (mRefreshView != null) {//准备加载更多，禁止刷新数据
                        mRefreshView.setTemporaryEnabled(false);
                    }
                    mLoadingListener.onLoadMore();
                }
            }
        }
    }


    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }


    private static class LoadMore extends RecyclerView.OnScrollListener {

        WeakReference<LastItemLoadingModule> mLoadMoreModule;

        private LoadMore(LastItemLoadingModule lastItemLoadingModule) {
            mLoadMoreModule = new WeakReference<>(lastItemLoadingModule);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            LastItemLoadingModule lastItemLoadingModule = mLoadMoreModule.get();
            if (lastItemLoadingModule != null) {
                lastItemLoadingModule.onScrollStateChanged(recyclerView, newState);
            }
        }
    }

    public interface ILastVisibleItemPositionFinder {

        int find(RecyclerView recyclerView);

    }
}
