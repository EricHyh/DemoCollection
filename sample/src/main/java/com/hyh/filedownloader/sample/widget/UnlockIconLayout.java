package com.hyh.filedownloader.sample.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.eric.filedownloader_master.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2018/9/10
 */

public class UnlockIconLayout extends ViewGroup {


    private Adapter mAdapter;

    private boolean inLayout;

    private int mWidth;
    private int mHeight;

    private int mCollapseRadius;

    private Paint mBackgroundPaint;
    private RectF mBackgroundRect;
    private Drawable mDividerDrawable;
    private int mDividerWidth;
    private int mDividerHeight;
    private List<Integer> mDividerLefts = new ArrayList<>();
    private int mIconAreaWidth;
    private int mIconAreaHeight;

    private List<View> mIconList = new ArrayList<>();

    private CollapseInfo mCollapseInfo = new CollapseInfo();

    private CollapseAnimator mCollapseAnimator;

    private AdapterDataSetObserver mDataSetObserver = new AdapterDataSetObserver();

    public UnlockIconLayout(Context context) {
        super(context);
        init(context, null);
    }

    public UnlockIconLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setWillNotDraw(false);
        mCollapseAnimator = new CollapseAnimator(this);
        mCollapseAnimator.setDuration(300);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setColor(Color.WHITE);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundRect = new RectF();
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.UnlockIconLayout);
            mDividerDrawable = typedArray.getDrawable(R.styleable.UnlockIconLayout_divider);
            mDividerWidth = typedArray.getDimensionPixelSize(R.styleable.UnlockIconLayout_divider_width, 0);
            mDividerHeight = typedArray.getDimensionPixelSize(R.styleable.UnlockIconLayout_divider_height, 0);
            mCollapseRadius = typedArray.getDimensionPixelSize(R.styleable.UnlockIconLayout_collapse_radius, 0);
            typedArray.recycle();
        }
    }

    public void setAdapter(Adapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        removeAllViews();
        this.mAdapter = adapter;
        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(mDataSetObserver);
            int itemCount = adapter.getIconCount();
            for (int index = 0; index < itemCount; index++) {
                addView(mAdapter.onCreateIcon(this, index));
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public void addView(View child) {
        mIconList.add(getChildCount(), child);
        super.addView(child);
        mAdapter.onBindIcon(child, getChildCount() - 1);
    }

    @Override
    public void addView(View child, int index) {
        if (index == -1) {
            index = getChildCount();
        }
        mIconList.add(index, child);
        super.addView(child, index);
        mAdapter.onBindIcon(child, index);
    }

    @Override
    public void addView(View child, int width, int height) {
        mIconList.add(getChildCount(), child);
        super.addView(child, width, height);
        mAdapter.onBindIcon(child, getChildCount() - 1);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        mIconList.add(getChildCount(), child);
        super.addView(child, params);
        mAdapter.onBindIcon(child, getChildCount() - 1);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (index == -1) {
            index = getChildCount();
        }
        mIconList.add(index, child);
        super.addView(child, index, params);
        mAdapter.onBindIcon(child, index);
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params) {
        if (index == -1) {
            index = getChildCount();
        }
        mIconList.add(index, child);
        boolean result = super.addViewInLayout(child, index, params);
        mAdapter.onBindIcon(child, index);
        return result;
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params, boolean preventRequestLayout) {
        if (index == -1) {
            index = getChildCount();
        }
        mIconList.add(index, child);
        boolean result = super.addViewInLayout(child, index, params, preventRequestLayout);
        mAdapter.onBindIcon(child, index);
        return result;
    }

    @Override
    public void removeView(View view) {
        mIconList.remove(view);
        super.removeView(view);
    }

    @Override
    public void removeViewInLayout(View view) {
        mIconList.remove(view);
        super.removeViewInLayout(view);
    }

    @Override
    public void removeViewsInLayout(int start, int count) {
        for (int index = 0; index < count; index++) {
            mIconList.remove(start);
        }
        super.removeViewsInLayout(start, count);
    }

    @Override
    public void removeViewAt(int index) {
        mIconList.remove(index);
        super.removeViewAt(index);
    }

    @Override
    public void removeViews(int start, int count) {
        for (int index = 0; index < count; index++) {
            mIconList.remove(start);
        }
        super.removeViews(start, count);
    }

    @Override
    public void removeAllViews() {
        mIconList.clear();
        super.removeAllViews();
    }

    @Override
    public void removeAllViewsInLayout() {
        super.removeAllViewsInLayout();
    }

    public void collapseIcons(int index, float collapseScale) {
        collapseIcons(index, collapseScale, false);
    }


    public void collapseIcons(int index, float collapseScale, boolean center) {
        View view = mIconList.get(index);
        if (mCollapseInfo.isRunning() && !mCollapseInfo.isTarget(view)) {
            return;
        }
        if (!mCollapseInfo.isTarget(view)) {
            mCollapseInfo.target = view;
            if (center) {
                mCollapseInfo.collapsePivotX = mWidth / 2;
                mCollapseInfo.collapsePivotY = getCollapsePivotY();
            } else {
                mCollapseInfo.collapsePivotX = getCollapsePivotX(index);
                mCollapseInfo.collapsePivotY = getCollapsePivotY();
            }
        }
        mCollapseInfo.updateCollapseScale(collapseScale);
        layoutChildren();
        invalidate();
    }

    private int getCollapsePivotX(int childIndex) {
        int childCount = getChildCount();
        int useWidth = (mWidth - mIconAreaWidth) / 2;
        int collapsePivotX = 0;
        for (int index = 0; index < childCount; index++) {
            View child = mIconList.get(index);
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            collapsePivotX = useWidth + layoutParams.leftMargin + childWidth / 2;
            useWidth += layoutParams.leftMargin + childWidth + layoutParams.rightMargin + mDividerWidth;
            if (childIndex == index) {
                break;
            }
        }
        return collapsePivotX;
    }

    public int getCollapsePivotY() {
        return mHeight - getPaddingBottom() - mIconAreaHeight / 2;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        if (childCount == 0) {
            return;
        }
        mIconAreaWidth = 0;
        mIconAreaHeight = 0;
        for (int index = 0; index < childCount; index++) {
            View child = mIconList.get(index);
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
            int childHeight = child.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
            mIconAreaWidth += childWidth;
            if (index < childCount - 1) {
                mIconAreaWidth += mDividerWidth;
            }
            if (mIconAreaHeight < childHeight) {
                mIconAreaHeight = childHeight;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        inLayout = true;
        layoutChildren();
        inLayout = false;
    }

    private void layoutChildren() {
        mDividerLefts.clear();
        int childCount = getChildCount();
        if (childCount > 0) {
            int layoutWith = 0;
            for (int index = 0; index < childCount; index++) {
                View child = mIconList.get(index);
                LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                int left, top, right, bottom;

                if (index == 0) {
                    left = (mWidth - mIconAreaWidth) / 2 + layoutParams.leftMargin;
                } else {
                    left = layoutWith + mDividerWidth + layoutParams.leftMargin;
                }

                top = mHeight - getPaddingBottom() - mIconAreaHeight + (mIconAreaHeight - childHeight) / 2;
                right = left + childWidth;
                bottom = top + childHeight;
                layoutWith = right + layoutParams.rightMargin;
                if (index < childCount - 1) {
                    mDividerLefts.add(layoutWith);
                }
                int moveX = 0;
                int moveY = 0;
                float collapseScale = mCollapseInfo.collapseScale;
                int collapsePivotX = mCollapseInfo.collapsePivotX;
                int collapsePivotY = mCollapseInfo.collapsePivotY;
                View target = mCollapseInfo.target;
                if (collapseScale > 0.0f) {
                    int iconCenterX = left + childWidth / 2;
                    int iconCenterY = top + childHeight / 2;
                    int p_x_df = collapsePivotX - iconCenterX;
                    moveX = (int) (p_x_df * collapseScale + 0.5f);
                    int p_y_df = collapsePivotY - iconCenterY;
                    moveY = (int) (p_y_df * collapseScale + 0.5f);
                }
                if (child != target) {
                    child.setAlpha(1 - collapseScale);
                }
                child.layout(left + moveX, top + moveY, right + moveX, bottom + moveY);
            }
        }
    }

    @Override
    public void requestLayout() {
        if (inLayout) {
            return;
        }
        super.requestLayout();
    }

    @Override
    public boolean isInLayout() {
        return inLayout;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate((mWidth - mIconAreaWidth) / 2, mHeight - mIconAreaHeight - getPaddingBottom());
        int left, top, right, bottom;
        left = 0;
        top = 0;
        right = left + mIconAreaWidth;
        bottom = top + mIconAreaHeight;
        float rx = mIconAreaHeight / 2;
        float ry = rx;

        View target = mCollapseInfo.target;
        int collapsePivotX = mCollapseInfo.collapsePivotX - (mWidth - mIconAreaWidth) / 2;
        int collapsePivotY = mCollapseInfo.collapsePivotY - (mHeight - mIconAreaHeight - getPaddingBottom());
        float collapseScale = mCollapseInfo.collapseScale;
        if (collapseScale > 0) {
            LayoutParams layoutParams = (LayoutParams) target.getLayoutParams();
            {
                int leftLimit = collapsePivotX - (layoutParams.leftMargin + target.getMeasuredWidth() / 2);
                int rightLimit = collapsePivotX + (layoutParams.leftMargin + target.getMeasuredWidth() / 2);
                int collapseX = (int) ((mIconAreaWidth - (layoutParams.leftMargin + target.getMeasuredWidth() + layoutParams.rightMargin))
                        * collapseScale + 0.5f);

                int expect_left_collapseX = collapseX / 2;
                int expect_right_collapseX = collapseX / 2;

                int expect_left = left + expect_left_collapseX;

                if (leftLimit < expect_left) {
                    expect_left_collapseX = leftLimit - left;
                    expect_right_collapseX = collapseX - expect_left_collapseX;
                }
                int expect_right = right - expect_right_collapseX;
                if (rightLimit > expect_right) {
                    expect_right_collapseX = right - rightLimit;
                    expect_left_collapseX = collapseX - expect_right_collapseX;
                }
                expect_left = left + expect_left_collapseX;
                if (leftLimit < expect_left) {
                    expect_left_collapseX = leftLimit - left;
                }
                left += expect_left_collapseX;
                right -= expect_right_collapseX;
            }
            {
                int collapseEndDiameter = Math.max(mCollapseRadius * 2,
                        Math.max(layoutParams.leftMargin + target.getMeasuredWidth() + layoutParams.rightMargin,
                                layoutParams.topMargin + target.getMeasuredHeight() + layoutParams.bottomMargin));
                int dx = collapseEndDiameter - (layoutParams.leftMargin + target.getMeasuredWidth() + layoutParams.rightMargin);
                int dy = collapseEndDiameter - (layoutParams.topMargin + target.getMeasuredHeight() + layoutParams.bottomMargin);

                left -= dx * collapseScale / 2;
                right += dx * collapseScale / 2;
                top -= dy * collapseScale / 2;
                bottom += dy * collapseScale / 2;

                float collapseEndRadius = collapseEndDiameter / 2;
                float radius_df = collapseEndRadius - rx;
                rx = ry = rx + (radius_df) * collapseScale;
            }
        }

        mBackgroundRect.set(left, top, right, bottom);


        Paint paint = mBackgroundPaint;
        canvas.drawRoundRect(mBackgroundRect, rx, ry, paint);

        canvas.restore();
        if (mDividerWidth != 0 && mDividerHeight != 0 && !mDividerLefts.isEmpty()) {
            for (Integer dividerLeft : mDividerLefts) {
                canvas.save();
                canvas.translate(dividerLeft, (mHeight - mIconAreaHeight - getPaddingBottom()) + (mIconAreaHeight - mDividerHeight) / 2);

                int endCenterX = mCollapseInfo.collapsePivotX - dividerLeft;

                mDividerDrawable.setAlpha((int) ((1.0f - collapseScale) * 255 + 0.5f));

                int dividerTop, dividerRight, dividerBottom;
                dividerLeft = 0;
                dividerTop = 0;
                dividerRight = dividerLeft + mDividerWidth;
                dividerBottom = dividerTop + mDividerHeight;

                int dx = endCenterX - (mDividerWidth / 2);

                dividerLeft += (int) (dx * collapseScale + 0.5f);
                dividerRight += (int) (dx * collapseScale + 0.5f);

                mDividerDrawable.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
                mDividerDrawable.draw(canvas);

                canvas.restore();
            }
        }
    }


    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
    }

    @Override
    public void childDrawableStateChanged(View child) {
        super.childDrawableStateChanged(child);
        boolean isChildPressed = false;
        int[] drawableState = child.getDrawableState();
        if (drawableState != null) {
            for (int state : drawableState) {
                if (state == android.R.attr.state_pressed) {
                    isChildPressed = true;
                }
            }
        }
        if (isChildPressed) {
            collapseIconsAnimation(child);
        } else {
            expandIconsAnimation(child);
        }
    }

    private void collapseIconsAnimation(View child) {
        if (mCollapseAnimator.isRunning()) {
            mCollapseAnimator.cancel();
        }
        int index = mIconList.indexOf(child);
        float collapseScale = mCollapseInfo.collapseScale;
        mCollapseAnimator.setFloatValues(collapseScale, 1.0f);
        mCollapseAnimator.setChildIndex(index);
        mCollapseAnimator.start();
    }

    private void expandIconsAnimation(View child) {
        if (mCollapseAnimator.isRunning()) {
            mCollapseAnimator.cancel();
        }
        int index = mIconList.indexOf(child);
        float collapseScale = mCollapseInfo.collapseScale;
        mCollapseAnimator.setFloatValues(collapseScale, 0.0f);
        mCollapseAnimator.setChildIndex(index);
        mCollapseAnimator.start();
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {

        public int gravity;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            final TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.UnlockIconLayout_Layout);
            gravity = a.getInt(R.styleable.UnlockIconLayout_Layout_layout_gravity, -1);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }


    private class AdapterDataSetObserver extends DataSetObserver {

        @Override
        public void onChanged() {
            if (mAdapter == null) {
                return;
            }
            int oldChild = getChildCount();
            int tagCount = mAdapter.getIconCount();
            if (oldChild < tagCount) {
                for (int index = 0; index < tagCount; index++) {
                    if (index < oldChild) {
                        mAdapter.onBindIcon(mIconList.get(index), index);
                    } else {
                        addView(mAdapter.onCreateIcon(UnlockIconLayout.this, index));
                    }
                }
            } else if (oldChild == tagCount) {
                for (int index = 0; index < oldChild; index++) {
                    mAdapter.onBindIcon(mIconList.get(index), index);
                }
            } else {
                for (int index = 0; index < tagCount; index++) {
                    if (index < tagCount) {
                        mAdapter.onBindIcon(mIconList.get(index), index);
                    }
                }
                removeViews(tagCount, tagCount - oldChild);
            }
        }

        @Override
        public void onInvalidated() {

        }
    }


    private class CollapseInfo {

        static final int COLLAPSE_STATE_IDLE = 0;
        static final int COLLAPSE_STATE_PROCESSING = 1;
        static final int COLLAPSE_STATE_FINISH = 2;

        View target;

        float collapseScale = 0.0f;

        int collapsePivotX;

        int collapsePivotY;

        int collapseState = COLLAPSE_STATE_IDLE;

        boolean isRunning() {
            return target != null && collapseScale > 0.0f;
        }

        boolean isTarget(View childAt) {
            return target != null && target == childAt;
        }

        void updateCollapseScale(float collapseScale) {
            this.collapseScale = collapseScale;
            int oldCollapseState = collapseState;
            int currentCollapseState;
            if (collapseScale == 0.0f) {
                currentCollapseState = COLLAPSE_STATE_IDLE;
            } else if (collapseScale > 0.0f && collapseScale < 1.0f) {
                currentCollapseState = COLLAPSE_STATE_PROCESSING;
            } else {
                currentCollapseState = COLLAPSE_STATE_FINISH;
            }
            if (oldCollapseState != currentCollapseState) {
                collapseState = currentCollapseState;
                onCollapseStateChanged(oldCollapseState, currentCollapseState);
            }
        }

        private void onCollapseStateChanged(int oldCollapseState, int currentCollapseState) {
            int childCount = getChildCount();
            switch (currentCollapseState) {
                case COLLAPSE_STATE_IDLE: {
                    for (int index = 0; index < childCount; index++) {
                        View view = mIconList.get(index);
                        view.setClickable(true);
                    }
                    break;
                }
                case COLLAPSE_STATE_PROCESSING: {
                    for (int index = 0; index < childCount; index++) {
                        View view = mIconList.get(index);
                        if (view == target) {
                            view.setClickable(true);
                        } else {
                            view.setClickable(false);
                        }
                    }
                    break;
                }
                case COLLAPSE_STATE_FINISH: {
                    for (int index = 0; index < childCount; index++) {
                        View view = mIconList.get(index);
                        if (view == target) {
                            view.setClickable(true);
                        } else {
                            view.setClickable(false);
                        }
                    }
                    break;
                }
            }
        }
    }


    private static class CollapseAnimator extends ValueAnimator implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

        private WeakReference<UnlockIconLayout> mReference;

        private int childIndex;

        public CollapseAnimator(UnlockIconLayout layout) {
            mReference = new WeakReference<>(layout);
            addListener(this);
            addUpdateListener(this);
        }

        public void setChildIndex(int childIndex) {
            this.childIndex = childIndex;
        }


        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            UnlockIconLayout unlockIconLayout = mReference.get();
            if (unlockIconLayout == null) {
                return;
            }
            Float value = (Float) animation.getAnimatedValue();
            unlockIconLayout.collapseIcons(childIndex, value);
        }
    }


    public static abstract class Adapter {

        private final DataSetObservable mDataSetObservable = new DataSetObservable();

        public abstract int getIconCount();

        public abstract View onCreateIcon(ViewGroup parent, int position);

        public abstract void onBindIcon(View icon, int position);

        public void notifyDataSetChanged() {
            mDataSetObservable.notifyChanged();
        }

        void registerDataSetObserver(DataSetObserver observer) {
            mDataSetObservable.registerObserver(observer);
        }

        void unregisterDataSetObserver(DataSetObserver observer) {
            mDataSetObservable.unregisterObserver(observer);
        }
    }
}
