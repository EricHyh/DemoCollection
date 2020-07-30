package com.hyh.ui.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import com.hyh.common.reflect.Reflect;

import java.util.ArrayList;

/**
 * @author Administrator
 * @description
 * @data 2019/9/26
 */

public class CustomViewPager extends ViewPager {

    private static final String TAG = "CustomViewPager";

    private ArrayList<View> mDrawingOrderedChildren;

    private final DrawingOrderInfo mDrawingOrderInfo = new DrawingOrderInfo();

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (!isChildrenDrawingOrderEnabled()) {
            return getDefaultChildDrawingOrder(childCount, i);
        }

        if (i == 0) mDrawingOrderInfo.clear();
        PagerAdapter adapter = getAdapter();
        if (adapter == null || childCount == 0) return getDefaultChildDrawingOrder(childCount, i);

        if (adapter.getCount() == getOffscreenPageLimit() - 1) {
            int currentItem = getCurrentItem();
            if (i >= currentItem) {
                return childCount - 1 - i + currentItem;
            }
            return super.getChildDrawingOrder(childCount, i);
        }

        if (i == 0 || i == childCount - 1) {
            mDrawingOrderInfo.currentItemPosition = getCurrentItem();
            mDrawingOrderInfo.childCount = childCount;
            mDrawingOrderInfo.firstItemPosition = adapter.getCount() - 1;
            mDrawingOrderInfo.lastItemPosition = 0;
            for (int index = 0; index < childCount; index++) {
                View view = getChildAt(index);
                int itemPosition = adapter.getItemPosition(view);
                mDrawingOrderInfo.firstItemPosition = Math.min(mDrawingOrderInfo.firstItemPosition, itemPosition);
                mDrawingOrderInfo.lastItemPosition = Math.max(mDrawingOrderInfo.lastItemPosition, itemPosition);
                mDrawingOrderInfo.itemPositionViewArray.put(itemPosition, view);
            }
        }

        int predictedItemPosition = mDrawingOrderInfo.firstItemPosition + i;
        int realItemPosition;

        if (predictedItemPosition < mDrawingOrderInfo.currentItemPosition) {
            realItemPosition = predictedItemPosition;
        } else if (i < childCount - 1) {
            realItemPosition = mDrawingOrderInfo.currentItemPosition + (childCount - 1 - i);
        } else {
            realItemPosition = mDrawingOrderInfo.currentItemPosition;
        }
        View view = mDrawingOrderInfo.itemPositionViewArray.get(realItemPosition);
        return indexOfChild(view);
    }

    protected int getDefaultChildDrawingOrder(int childCount, int i) {
        ArrayList<View> drawingOrderedChildren = getDrawingOrderedChildren();
        if (drawingOrderedChildren == null || drawingOrderedChildren.isEmpty()) return i;
        int size = drawingOrderedChildren.size();
        if (i >= size) {
            return i;
        }
        return super.getChildDrawingOrder(childCount, i);
    }

    @SuppressWarnings("unchecked")
    private ArrayList<View> getDrawingOrderedChildren() {
        if (mDrawingOrderedChildren != null) return mDrawingOrderedChildren;
        try {
            mDrawingOrderedChildren = Reflect.from(ViewPager.class).filed("mDrawingOrderedChildren", ArrayList.class).get(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mDrawingOrderedChildren;
    }


    private static class DrawingOrderInfo {

        int childCount;

        SparseArray<View> itemPositionViewArray = new SparseArray<>();

        int firstItemPosition;

        int currentItemPosition;

        int lastItemPosition;

        void clear() {
            childCount = 0;
            itemPositionViewArray.clear();
            firstItemPosition = 0;
            currentItemPosition = 0;
            lastItemPosition = 0;
        }
    }
}