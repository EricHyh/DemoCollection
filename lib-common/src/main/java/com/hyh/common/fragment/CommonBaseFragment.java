package com.hyh.common.fragment;


import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyh.common.lifecycle.ActivityLifecycleHelper;
import com.hyh.common.visible.IVisibleHandler;
import com.hyh.common.visible.ItemVisibleStrategy;
import com.hyh.common.visible.ViewVisibleDetector;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

public abstract class CommonBaseFragment extends Fragment {

    private Context mContext;

    private boolean mIsLoadedData = false;

    private boolean mCompelledLoading;

    protected boolean mIsSelected;

    protected View mContentView;

    private boolean isNeedLoadData;

    private Context mThemeContext;

    private ActivityLifecycleListener mLifecycleListener;

    private int mDefaultThemeResId;

    private boolean mIsRegisterActivityLifecycleCallbacks;

    public void setCompelledLoading() {
        mCompelledLoading = true;
    }

    public void setDefaultThemeResId(int themeResId) {
        this.mDefaultThemeResId = themeResId;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        if (!mIsRegisterActivityLifecycleCallbacks && isFollowActivityLifecycle()) {
            Activity activity = getActivity();
            if (activity == null) {
                if (context instanceof Activity) {
                    activity = (Activity) context;
                }
            }
            mLifecycleListener = new ActivityLifecycleListener(activity, this);
            ActivityLifecycleHelper.getInstance().listenerAllActivity(mLifecycleListener);
            mIsRegisterActivityLifecycleCallbacks = true;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int themeResId = getThemeResId();
        if (themeResId == 0) {
            if (inflater != null) {
                mThemeContext = inflater.getContext();
            } else {
                Context context = getContext();
                mThemeContext = context;
                inflater = LayoutInflater.from(context);
            }
        } else {
            mThemeContext = new ContextThemeWrapper(getContext().getApplicationContext(), themeResId);
            inflater = LayoutInflater.from(mThemeContext);
        }
        if (isNeedReCreateView()) {
            mContentView = getContentView(inflater, container);
            initView(mContentView);
            if (mCompelledLoading || !lazyLoadData() || (isNeedLoadData && mIsSelected)) {
                mIsLoadedData = true;
                isNeedLoadData = false;
                initData();
            }
        } else {
            if (mContentView != null) {
                return mContentView;
            }
            mContentView = getContentView(inflater, container);
            initView(mContentView);
            if (mCompelledLoading || !lazyLoadData() || (isNeedLoadData && mIsSelected)) {
                mIsLoadedData = true;
                isNeedLoadData = false;
                initData();
            }
        }
        return mContentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (lazyLoadData()) {
            ViewVisibleDetector.setVisibleHandler(view, new VisibleHandler());
        }
    }

    @Override
    public Context getContext() {
        Context context = super.getContext();
        if (context != null) return context;
        return mContext;
    }

    protected Context getThemeContext() {
        return mThemeContext;
    }

    protected int getThemeResId() {
        return mDefaultThemeResId;
    }

    protected abstract View getContentView(LayoutInflater inflater, ViewGroup container);

    protected abstract void initView(View contentView);

    protected abstract void initData();

    protected boolean lazyLoadData() {
        return false;
    }

    protected boolean isNeedReCreateView() {
        return false;
    }

    protected boolean isFollowActivityLifecycle() {
        return false;
    }

    protected boolean isActivityFinish() {
        Activity activity = getActivity();
        return activity == null || activity.isFinishing();
    }


    protected void finishActivity() {
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            activity.finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected void onActivityStarted() {
    }

    protected void onActivityResumed() {
    }

    protected void onActivityPaused() {
    }

    protected void onActivityStopped() {
    }

    protected void onActivityDestroyed() {
        ActivityLifecycleListener lifecycleListener = mLifecycleListener;
        if (lifecycleListener != null) {
            ActivityLifecycleHelper.getInstance().unListenerAllActivity(lifecycleListener);
        }
        mLifecycleListener = null;
        mIsRegisterActivityLifecycleCallbacks = false;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        ActivityLifecycleListener lifecycleListener = mLifecycleListener;
        if (lifecycleListener != null) {
            ActivityLifecycleHelper.getInstance().unListenerAllActivity(lifecycleListener);
        }
    }

    private class VisibleHandler implements IVisibleHandler {

        private final List<ItemVisibleStrategy> mVisibleStrategies = Collections.singletonList(
                new ItemVisibleStrategy(
                        ItemVisibleStrategy.VISIBLE_TYPE_HORIZONTAL,
                        1.0f,
                        100)
        );


        @Override
        public List<ItemVisibleStrategy> getItemVisibleStrategies() {
            return mVisibleStrategies;
        }

        @Override
        public void onViewInvisible(View view, ItemVisibleStrategy strategy) {

        }

        @Override
        public void onViewVisible(View view, ItemVisibleStrategy strategy) {
            if (!mIsLoadedData) {
                if (mContentView != null) {
                    mIsLoadedData = true;
                    isNeedLoadData = false;
                    initData();
                } else {
                    isNeedLoadData = true;
                }
            }
        }
    }

    private static class ActivityLifecycleListener implements Application.ActivityLifecycleCallbacks {

        private final WeakReference<Activity> mActivityReference;

        private final WeakReference<CommonBaseFragment> mFragmentReference;

        ActivityLifecycleListener(Activity activity, CommonBaseFragment fragment) {
            mActivityReference = new WeakReference<>(activity);
            mFragmentReference = new WeakReference<>(fragment);
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (getActivity() == activity) {
                CommonBaseFragment fragment = mFragmentReference.get();
                if (fragment != null) {
                    fragment.onActivityStarted();
                } else {
                    ActivityLifecycleHelper.getInstance().unListenerAllActivity(this);
                }
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
            if (getActivity() == activity) {
                CommonBaseFragment fragment = mFragmentReference.get();
                if (fragment != null) {
                    fragment.onActivityResumed();
                } else {
                    ActivityLifecycleHelper.getInstance().unListenerAllActivity(this);
                }
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
            if (getActivity() == activity) {
                CommonBaseFragment fragment = mFragmentReference.get();
                if (fragment != null) {
                    fragment.onActivityPaused();
                } else {
                    ActivityLifecycleHelper.getInstance().unListenerAllActivity(this);
                }
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (getActivity() == activity) {
                CommonBaseFragment fragment = mFragmentReference.get();
                if (fragment != null) {
                    fragment.onActivityStopped();
                } else {
                    ActivityLifecycleHelper.getInstance().unListenerAllActivity(this);
                }
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            if (getActivity() == activity) {
                CommonBaseFragment fragment = mFragmentReference.get();
                if (fragment != null) {
                    fragment.onActivityDestroyed();
                } else {
                    ActivityLifecycleHelper.getInstance().unListenerAllActivity(this);
                }
            }
        }

        private Activity getActivity() {
            Activity activity = mActivityReference.get();
            if (activity == null) {
                CommonBaseFragment commonBaseFragment = mFragmentReference.get();
                if (commonBaseFragment != null) {
                    activity = commonBaseFragment.getActivity();
                }
            }
            return activity;
        }
    }
}