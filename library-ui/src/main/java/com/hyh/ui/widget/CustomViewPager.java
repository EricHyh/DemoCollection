package com.hyh.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setChildrenDrawingOrderEnabled(true);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        ArrayList<View> drawingOrderedChildren = getDrawingOrderedChildren();
        if (drawingOrderedChildren == null || drawingOrderedChildren.isEmpty()) return i;
        int size = drawingOrderedChildren.size();
        if (i >= size) {
            return i;
        }


        int childDrawingOrder = super.getChildDrawingOrder(childCount, i);

        TextView textView = (TextView) getChildAt(i);
        PagerAdapter adapter = getAdapter();
        if (adapter == null) return childDrawingOrder;
        int itemPosition = adapter.getItemPosition(textView);

        int currentItem = getCurrentItem();


        int afterCurrentItemCount = 0;
        for (int index = 0; index < childCount; index++) {
            View view = getChildAt(index);
            if (adapter.getItemPosition(view) > currentItem) {
                afterCurrentItemCount++;
            }
        }

        int realDrawingOrder;
        if (itemPosition < currentItem) {
            //realDrawingOrder = childCount / 2 - (currentItem - itemPosition - 1);
            //realDrawingOrder = childDrawingOrder == childCount - 1 ? childDrawingOrder - 1 : childDrawingOrder;
            realDrawingOrder = childCount - afterCurrentItemCount - 2 - (currentItem - itemPosition - 1);
        } else if (itemPosition == currentItem) {
            realDrawingOrder = childCount - 1;
        } else {
            //realDrawingOrder = childDrawingOrder - 1;
            realDrawingOrder = childCount - 1 - (itemPosition - currentItem);
        }

        if (i >= currentItem) {
            realDrawingOrder = childCount - 1 - i + currentItem;
        } else {
            realDrawingOrder = childDrawingOrder;
        }

        Log.d(TAG, "getChildDrawingOrder: "
                + "childDrawingOrder = " + childDrawingOrder
                + ", i = " + i
                + ", text = " + textView.getText()
                + ", position = " + textView.getTag()
                + ", childCount = " + childCount
                + ", currentItem = " + currentItem
                + ", itemPosition = " + itemPosition
                + ", realDrawingOrder = " + realDrawingOrder
                + ", afterCurrentItemCount = " + afterCurrentItemCount
        );


        return childDrawingOrder;
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
}