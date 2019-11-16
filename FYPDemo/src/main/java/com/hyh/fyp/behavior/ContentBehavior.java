package com.hyh.fyp.behavior;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2019/11/15
 */
public class ContentBehavior extends BaseBehavior<View> {

    private static final String TAG = "ContentBehavior";

    public ContentBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onMeasureChild(@NonNull CoordinatorLayout parent,
                                  @NonNull View child,
                                  int parentWidthMeasureSpec, int widthUsed,
                                  int parentHeightMeasureSpec, int heightUsed) {
        BaseBehavior behavior = NestedScrollHelper.getBaseBehavior(child);
        if (behavior instanceof ContentBehavior) {
            List<View> dependencies = parent.getDependencies(child);
            if (dependencies.isEmpty()) {
                return false;
            }
            int fixedViewHeight = 0;
            for (View dependency : dependencies) {
                BaseBehavior dependencyBehavior = NestedScrollHelper.getBaseBehavior(dependency);
                if (dependencyBehavior instanceof FixedBehavior) {
                    fixedViewHeight += dependency.getMeasuredHeight();
                    break;
                }
            }
            if (fixedViewHeight > 0) {
                int size = View.MeasureSpec.getSize(parentHeightMeasureSpec);
                int childHeight = size - fixedViewHeight;
                int childHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(childHeight, View.MeasureSpec.EXACTLY);
                child.measure(parentWidthMeasureSpec, childHeightMeasureSpec);
                return true;
            }
        }
        return super.onMeasureChild(parent, child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }
}