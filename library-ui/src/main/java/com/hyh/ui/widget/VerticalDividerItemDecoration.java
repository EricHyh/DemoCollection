package com.hyh.ui.widget;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class VerticalDividerItemDecoration extends RecyclerView.ItemDecoration {

    private final Drawable mDivider;

    private final int mDividerHeight;

    private final Rect mPadding = new Rect();

    private final Rect mBounds = new Rect();

    public VerticalDividerItemDecoration(@NonNull Drawable drawable, int dividerHeight) {
        mDivider = drawable;
        mDividerHeight = dividerHeight;
    }


    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (parent.getLayoutManager() == null) {
            return;
        }
        drawVertical(c, parent);
    }

    private void drawVertical(Canvas canvas, RecyclerView parent) {
        canvas.save();
        final int left;
        final int right;
        if (parent.getClipToPadding()) {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
            canvas.clipRect(left, parent.getPaddingTop(), right,
                    parent.getHeight() - parent.getPaddingBottom());
        } else {
            left = 0;
            right = parent.getWidth();
        }

        final LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();

        final int childCount = parent.getChildCount();
        for (int index = 0; index < childCount; index++) {
            final View child = parent.getChildAt(index);
            int childLayoutPosition = parent.getChildLayoutPosition(child);
            int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
            if (childLayoutPosition >= lastVisibleItemPosition) {
                continue;
            }

            parent.getDecoratedBoundsWithMargins(child, mBounds);
            final int bottom = mBounds.bottom + Math.round(ViewCompat.getTranslationY(child));
            final int top = bottom - getDividerHeight();
            mDivider.setBounds(
                    left + mPadding.left,
                    top,
                    right - mPadding.right,
                    bottom);
            mDivider.setCallback(parent);
            mDivider.draw(canvas);
        }
        canvas.restore();
    }

    private int getDividerHeight() {
        if (mDividerHeight > 0) return mDividerHeight;
        return mDivider.getIntrinsicHeight();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int childLayoutPosition = parent.getChildLayoutPosition(view);
        int itemCount = state.getItemCount();
        if (childLayoutPosition < itemCount - 1) {
            int left = 0;
            int top = 0;
            int right = 0;
            int bottom = getDividerHeight();
            outRect.set(left, top, right, bottom);
        }
    }

    public void setPadding(int left, int top, int right, int bottom) {
        mPadding.set(left, top, right, bottom);
    }

    public void setPadding(Rect padding) {
        mPadding.set(padding);
    }
}