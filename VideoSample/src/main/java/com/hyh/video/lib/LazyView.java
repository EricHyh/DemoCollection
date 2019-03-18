package com.hyh.video.lib;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Eric_He on 2019/3/15.
 */

public abstract class LazyView<V extends View> {

    private V mView;
    private Map<String, List<LazyAction<V>>> mLazyActionMap;
    private ViewGroup mParent;
    private ViewGroup.LayoutParams mLayoutParams;

    public void addToParent(ViewGroup parent, ViewGroup.LayoutParams params) {
        this.mParent = parent;
        this.mLayoutParams = params;
        if (isCreated()) {
            parent.addView(mView, mLayoutParams);
        }
    }

    public void setVisibility(int visibility) {
        if (mView == null && visibility == View.VISIBLE) {
            executeCreate();
        }
        if (mView != null && mView.getVisibility() != visibility) {
            mView.setVisibility(visibility);
        }
    }

    public int getVisibility() {
        if (mView == null) return View.GONE;
        return mView.getVisibility();
    }

    public abstract V create();

    public boolean isCreated() {
        return mView != null;
    }

    public V get() {
        if (mView != null) {
            return mView;
        }
        executeCreate();
        return mView;
    }

    private void executeCreate() {
        mView = create();
        if (mView == null) throw new IllegalArgumentException("LazyView create result can't be null!");
        if (mParent != null) {
            mParent.addView(mView, mLayoutParams);
        }
        doLazyActions(mView);
    }

    public void saveLazyAction(String actionKey, LazyAction<V> action) {
        saveLazyAction(actionKey, action, false);
    }

    public void saveLazyAction(String actionKey, LazyAction<V> action, boolean unique) {
        if (action == null) return;
        if (mView != null) {
            action.doAction(mView);
            return;
        }
        if (mLazyActionMap == null) {
            mLazyActionMap = new LinkedHashMap<>();
            ArrayList<LazyAction<V>> actions = new ArrayList<>();
            actions.add(action);
            mLazyActionMap.put(actionKey, actions);
        } else {
            List<LazyAction<V>> actions = mLazyActionMap.get(actionKey);
            if (actions == null) {
                actions = new ArrayList<>();
                mLazyActionMap.put(actionKey, actions);
            }
            if (unique) {
                actions.clear();
            }
            actions.add(action);
        }
    }

    private void doLazyActions(V v) {
        if (mLazyActionMap != null && !mLazyActionMap.isEmpty()) {
            Collection<List<LazyAction<V>>> values = mLazyActionMap.values();
            for (List<LazyAction<V>> actions : values) {
                if (actions != null && !actions.isEmpty()) {
                    for (LazyAction<V> action : actions) {
                        action.doAction(v);
                    }
                }
            }
            mLazyActionMap.clear();
        }
    }

    public interface LazyAction<T> {

        void doAction(T t);

    }
}