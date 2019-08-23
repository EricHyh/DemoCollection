package com.hyh.web.utils;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * @author Administrator
 * @description
 * @data 2019/6/4
 */

public class ViewUtil {

    public static void setVisibility(View view, int visibility) {
        if (view == null || view.getVisibility() == visibility) return;
        view.setVisibility(visibility);
    }

    public static boolean isViewInScreen(View view) {
        if (view == null
                || view.getVisibility() != View.VISIBLE
                || view.getWindowToken() == null) {
            return false;
        }
        if (!view.getLocalVisibleRect(new Rect())) {
            return false;
        }
        ViewParent parent = view.getParent();
        if (parent == null) {
            return view == view.getRootView();
        } else {
            return isParentAlive(parent);
        }
    }

    public static boolean isParentAlive(ViewParent parent) {
        if (parent instanceof View) {
            View parentView = (View) parent;
            if (parentView.getWindowToken() == null
                    || parentView.getVisibility() != View.VISIBLE) {
                return false;
            } else {
                ViewParent grandParent = parentView.getParent();
                if (grandParent == null) {
                    return parentView == parentView.getRootView();
                } else {
                    return isParentAlive(grandParent);
                }
            }
        } else {
            return true;
        }
    }

    public static float getVisibleScale(View view) {
        if (!isViewInScreen(view)) return 0.0f;
        Rect rect = new Rect();
        if (!view.getGlobalVisibleRect(rect)) return 0.0f;
        long rectArea = (long) rect.height() * (long) rect.width();
        long viewArea = (long) view.getHeight() * (long) view.getWidth();
        if (rectArea == 0 || viewArea == 0) return 0.0f;
        return rectArea * 1.0f / viewArea;
    }

    public static boolean isParent(View child, ViewGroup parent) {
        ViewParent curParent = child.getParent();
        if (curParent == null || !(curParent instanceof ViewGroup)) {
            return false;
        }
        if (curParent == parent) {
            return true;
        }
        child = (View) curParent;
        return isParent(child, parent);
    }
}