package com.hyh.fyp.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * @author Administrator
 * @description
 * @data 2020/8/5
 */
public class ExpandableLayout extends FrameLayout {

    private final ImageView arrowView;

    private boolean expanded;
    private int collapseHeight;


    public ExpandableLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandableLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);

        arrowView = new ImageView(context);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        addView(arrowView, layoutParams);
        arrowView.setOnClickListener(v -> expand());

        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        collapseHeight = dm.heightPixels;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        arrowView.bringToFront();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredHeight = getMeasuredHeight();
        if (!expanded) {
            if (measuredHeight > collapseHeight) {
                setMeasuredDimension(getMeasuredWidth(), collapseHeight);
            }
        }
    }

    public void expand() {
        if (!expanded) {
            expanded = true;
            arrowView.setVisibility(GONE);
            requestLayout();
        }
    }

    public void collapse() {
        if (expanded) {
            expanded = false;
            arrowView.setVisibility(VISIBLE);
            requestLayout();
        }
    }

    public void toggle() {
        expanded = !expanded;
        requestLayout();
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setCollapseHeight(int collapseHeight) {
        this.collapseHeight = collapseHeight;
        requestLayout();
    }

    public void setArrowDrawable(Drawable drawable) {
        arrowView.setImageDrawable(drawable);
    }
}