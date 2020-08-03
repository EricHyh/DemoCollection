package com.hyh.web.multi.load;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.hyh.web.multi.EmptyDataItemFactory;

import java.lang.ref.WeakReference;

/**
 * @author Administrator
 * @description
 * @data 2017/11/24
 */

public class LoadingItemFactory extends EmptyDataItemFactory {

    private static final String TAG = "LoadingItemFactory";

    private RecyclerView mRecyclerView;

    private ScrollViewClient mScrollViewClient;

    private LastVisibleItemFinder mLastVisibleItemFinder;

    private Boolean mPullToRefreshEnabled;

    private boolean mLoadMoreEnabled;

    private boolean mInLoadMore;

    private boolean mNoMore;

    private LoadingListener mLoadingListener;

    private IFootView mFootView;

    private int mAdvanceItemCount;

    private RefreshViewClient mRefreshView;

    private int mOldLoadState = LoadState.LOAD_STATE_IDLE;

    private boolean mLoadMoreItemVisible;

    private long mLastLoadMoreTimeMillis;

    public LoadingItemFactory(IFootView footView) {
        this(footView, 0);
    }

    public LoadingItemFactory(IFootView footView, int advanceItemCount) {
        super();
        this.mFootView = footView;
        this.mAdvanceItemCount = advanceItemCount < 0 ? 0 : advanceItemCount;
    }

    public void bindScrollListener(RecyclerView recyclerView) {
        bindScrollListener(recyclerView, new RecyclerScrollViewClient(recyclerView), new DefaultLastVisibleItemFinder());
    }

    public void bindScrollListener(RecyclerView recyclerView, ScrollViewClient scrollViewClient) {
        bindScrollListener(recyclerView, scrollViewClient, new DefaultLastVisibleItemFinder());
    }

    public void bindScrollListener(RecyclerView recyclerView, LastVisibleItemFinder finder) {
        bindScrollListener(recyclerView, new RecyclerScrollViewClient(recyclerView), finder);
    }

    public void bindScrollListener(RecyclerView recyclerView, ScrollViewClient scrollViewClient, LastVisibleItemFinder finder) {
        this.mRecyclerView = recyclerView;
        this.mScrollViewClient = scrollViewClient;
        this.mLastVisibleItemFinder = finder;

        mScrollViewClient.setScrollListener(new LoadMore(this));
    }

    public void setLoadingListener(LoadingListener loadingListener) {
        mLoadingListener = loadingListener;
    }

    public void bindRefreshListener(RefreshViewClient refreshView) {
        this.mRefreshView = refreshView;
        if (mRefreshView != null && mPullToRefreshEnabled != null) {
            mRefreshView.setPullToRefreshEnabled(mPullToRefreshEnabled);
        }
        refreshView.setOnRefreshListener(new RefreshViewClient.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mLoadingListener != null) {
                    mLoadingListener.onRefresh();
                }
            }
        });
    }


    public void setPullToRefreshEnabled(boolean enabled) {
        mPullToRefreshEnabled = enabled;
        if (mRefreshView != null) {
            mRefreshView.setPullToRefreshEnabled(enabled);
        }
    }

    public void setLoadMoreEnabled(boolean enabled) {
        mLoadMoreEnabled = enabled;
        if (enabled) {
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    onScrolled(mRecyclerView, mRecyclerView.getScrollState(), false);
                }
            });
        }
    }

    public void setNoMore(boolean noMore) {
        mNoMore = noMore;
        if (mNoMore) {
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    if (mNoMore) {
                        if (mOldLoadState != LoadState.LOAD_STATE_NO_MORE) {
                            if (mFootView != null) {
                                mFootView.onLoadStateChanged(mOldLoadState, LoadState.LOAD_STATE_NO_MORE);
                            }
                            mOldLoadState = LoadState.LOAD_STATE_NO_MORE;
                        }
                    }
                }
            });
        }
    }

    public void loadMoreComplete(boolean isSuccess) {
        mLastLoadMoreTimeMillis = System.currentTimeMillis();
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
        mInLoadMore = false;
        if (mRefreshView != null) {
            mRefreshView.setTemporaryEnabled(true);
        }
    }

    public void refreshComplete(boolean success) {
        if (success) {
            mOldLoadState = LoadState.LOAD_STATE_IDLE;
        }
        if (mRefreshView != null) {
            mRefreshView.refreshComplete(success);
        }
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                onScrolled(mRecyclerView, mRecyclerView.getScrollState(), false);
            }
        });
    }

    public boolean executeRefresh() {
        return mRefreshView != null && mRefreshView.executeRefresh();
    }

    public void executeLoadMore() {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                onScrolled(mRecyclerView, RecyclerView.SCROLL_STATE_IDLE, true);
            }
        });
    }

    @NonNull
    @Override
    protected View createItemView(ViewGroup parent) {
        return mFootView.onCreateView(this);
    }

    @Override
    protected void initView(View parent, View view) {
    }

    private void onScrolled(RecyclerView recyclerView, int scrollState, boolean performLoadMore) {
        if (!mLoadMoreEnabled) {
            return;
        }
        if (mLoadingListener == null || mInLoadMore) {
            return;
        }
        if (mOldLoadState == LoadState.LOAD_STATE_LOAD_FAILURE && scrollState != RecyclerView.SCROLL_STATE_IDLE) {
            return;
        }

        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter == null) return;

        final int lastVisibleItemPosition = mLastVisibleItemFinder.find(recyclerView);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        int loadMoreItemPosition = layoutManager.getItemCount() - 1 - mAdvanceItemCount;
        loadMoreItemPosition = Math.max(loadMoreItemPosition, 0);

        if (layoutManager.getChildCount() > 0
                && lastVisibleItemPosition >= loadMoreItemPosition
                && layoutManager.getItemCount() >= layoutManager.getChildCount()) {
            if (mNoMore) {
                if (mOldLoadState != LoadState.LOAD_STATE_NO_MORE) {
                    if (mFootView != null) {
                        mFootView.onLoadStateChanged(mOldLoadState, LoadState.LOAD_STATE_NO_MORE);
                    }
                    mOldLoadState = LoadState.LOAD_STATE_NO_MORE;
                }
            } else {
                if (isRefreshing()) {
                    if (mOldLoadState != LoadState.LOAD_STATE_REFRESHING) {
                        if (mFootView != null) {
                            mFootView.onLoadStateChanged(mOldLoadState, LoadState.LOAD_STATE_REFRESHING);
                        }
                        mOldLoadState = LoadState.LOAD_STATE_REFRESHING;
                    }
                } else {
                    boolean allowLoadMore;
                    if (!performLoadMore && mOldLoadState == LoadState.LOAD_STATE_LOAD_FAILURE) {
                        if (mLoadMoreItemVisible) {
                            long currentTimeMillis = System.currentTimeMillis();
                            long loadMoreInterval = Math.abs(currentTimeMillis - mLastLoadMoreTimeMillis);
                            allowLoadMore = loadMoreInterval > 3000;
                        } else {
                            allowLoadMore = true;
                        }
                    } else {
                        allowLoadMore = true;
                    }
                    if (allowLoadMore) {
                        mInLoadMore = true;
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
            mLoadMoreItemVisible = true;
        } else {
            mLoadMoreItemVisible = false;
        }
    }

    public boolean isRefreshing() {
        return mRefreshView != null && mRefreshView.isRefreshing();
    }

    public boolean isLoadingMore() {
        return mInLoadMore;
    }

    private static class LoadMore extends RecyclerView.OnScrollListener {

        WeakReference<LoadingItemFactory> mLoadMoreModule;

        long mLastScrolledTimeMillis;

        private LoadMore(LoadingItemFactory loadingModule) {
            mLoadMoreModule = new WeakReference<>(loadingModule);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            LoadingItemFactory loadingModule = mLoadMoreModule.get();
            if (loadingModule != null) {
                loadingModule.onScrolled(recyclerView, newState, false);
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            long currentTimeMillis = System.currentTimeMillis();
            long timeInterval = Math.abs(currentTimeMillis - mLastScrolledTimeMillis);
            if (timeInterval < 100) return;
            mLastScrolledTimeMillis = currentTimeMillis;
            LoadingItemFactory loadingModule = mLoadMoreModule.get();
            if (loadingModule != null) {
                loadingModule.onScrolled(recyclerView, recyclerView.getScrollState(), false);
            }
        }
    }

    public interface LoadingListener {

        void onRefresh();

        void onLoadMore();

    }

    public interface LastVisibleItemFinder {

        int find(RecyclerView recyclerView);

    }

    public static class DefaultLastVisibleItemFinder implements LastVisibleItemFinder {

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

        private int findMax(int[] lastPositions) {
            int max = lastPositions[0];
            for (int value : lastPositions) {
                if (value > max) {
                    max = value;
                }
            }
            return max;
        }
    }
}