package com.hyh.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
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

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
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
}