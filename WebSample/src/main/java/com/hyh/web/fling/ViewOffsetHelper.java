/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hyh.web.fling;

import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;

/**
 * Utility helper for moving a {@link View} around using
 * {@link View#offsetLeftAndRight(int)} and
 * {@link View#offsetTopAndBottom(int)}.
 * <p>
 * Also the setting of absolute offsets (similar to translationX/Y), rather than additive
 * offsets.
 */
class ViewOffsetHelper {

    private final View mView;

    private View mBottomView;
    private int mLayoutTop;
    private int mLayoutLeft;
    private int mOffsetTop;
    private int mOffsetLeft;

    private boolean mNeedHandleBoundaryOffset = true;

    public ViewOffsetHelper(View view) {
        mView = view;
    }

    public void setNeedHandleBoundaryOffset(boolean needHandleBoundaryOffset) {
        mNeedHandleBoundaryOffset = needHandleBoundaryOffset;
    }

    public void onViewLayout() {
        // Now grab the intended top
        mLayoutTop = mView.getTop();
        mLayoutLeft = mView.getLeft();

        // And offset it as needed
        updateOffsets();
    }

    private void updateOffsets() {
        ViewCompat.offsetTopAndBottom(mView, mOffsetTop - (mView.getTop() - mLayoutTop));
        ViewCompat.offsetLeftAndRight(mView, mOffsetLeft - (mView.getLeft() - mLayoutLeft));
    }

    /**
     * Set the top and bottom offset for this {@link ViewOffsetHelper}'s view.
     *
     * @param offset the offset in px.
     * @return true if the offset has changed
     */
    public boolean setTopAndBottomOffset(int offset) {
        if (mOffsetTop != offset) {
            offset = setToBoundaryOffsetIfNecessary(offset);
            mOffsetTop = offset;
            updateOffsets();
            return true;
        }
        return false;
    }

    private int setToBoundaryOffsetIfNecessary(int offset) {
        if (mNeedHandleBoundaryOffset) {
            int childHeight = mView.getMeasuredHeight();
            View parent = (View) mView.getParent();
            int parentHeight = parent == null ? 0 : parent.getMeasuredHeight();
            if (childHeight < parentHeight) {
                return 0;
            }
            int minOffset = parentHeight - childHeight;
            if (offset < minOffset) {
                offset = minOffset;
            }
            return offset;
        } else {
            ViewGroup parent = (ViewGroup) mView.getParent();
            if (parent != null) {
                View bottomView = findBottomView(parent);
                if (bottomView != null) {
                    int childHeight = mView.getMeasuredHeight();
                    int parentHeight = parent.getMeasuredHeight();
                    int bottomHeight = bottomView.getMeasuredHeight();
                    int minOffset = parentHeight - childHeight - bottomHeight;
                    if (offset < minOffset) {
                        offset = minOffset;
                    }
                }
            }
            return offset;
        }

    }

    private View findBottomView(ViewGroup parent) {
        if (mBottomView != null) return mBottomView;
        int childCount = parent.getChildCount();
        if (childCount <= 1) {
            return null;
        }
        for (int index = 1; index < childCount; index++) {
            View childAt = parent.getChildAt(index);
            ViewGroup.LayoutParams layoutParams = childAt.getLayoutParams();
            if (layoutParams != null && layoutParams instanceof CoordinatorLayout.LayoutParams) {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) layoutParams;
                CoordinatorLayout.Behavior behavior = params.getBehavior();
                if (behavior instanceof AppBarLayout.ScrollingViewBehavior) {
                    mBottomView = childAt;
                    return childAt;
                }
            }
        }
        return null;
    }

    /**
     * Set the left and right offset for this {@link ViewOffsetHelper}'s view.
     *
     * @param offset the offset in px.
     * @return true if the offset has changed
     */
    public boolean setLeftAndRightOffset(int offset) {
        if (mOffsetLeft != offset) {
            mOffsetLeft = offset;
            updateOffsets();
            return true;
        }
        return false;
    }

    public int getTopAndBottomOffset() {
        return mOffsetTop;
    }

    public int getLeftAndRightOffset() {
        return mOffsetLeft;
    }

    public int getLayoutTop() {
        return mLayoutTop;
    }

    public int getLayoutLeft() {
        return mLayoutLeft;
    }
}