package com.hyh.fyp.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.hyh.fyp.R;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("AppCompatCustomView")
public class PasswordView extends EditText implements TextWatcher {

    private static final String TAG = "PasswordView_";

    public static final int PASSWORD_TYPE_STARS = 0;
    public static final int PASSWORD_TYPE_CIRCLE = 1;
    public static final int PASSWORD_TYPE_TEXT = 2;

    private static final int BOX_MEASURE_MODE_FILL = 0;
    private static final int BOX_MEASURE_MODE_BOUND = 1;
    private static final int BOX_MEASURE_MODE_FREE = 2;

    private static final int BOX_TYPE_RECT = 0;
    private static final int BOX_TYPE_OVAL = 1;
    private static final int BOX_TYPE_UNDERLINE = 2;

    private static final int BOX_CHAIN_STYLE_FREE = 0;
    private static final int BOX_CHAIN_STYLE_SPREAD = 1;
    private static final int BOX_CHAIN_STYLE_SPREAD_INSIDE = 2;
    private static final int BOX_CHAIN_STYLE_PACKET = 3;

    private final DrawCursorToggleTask mDrawCursorToggleTask = new DrawCursorToggleTask();

    private int mPasswordLength = 6;
    private int mPasswordType = PASSWORD_TYPE_STARS;

    private int mBoxMeasureType = BOX_MEASURE_MODE_FILL;

    private float mBoxWidth, mBoxHeight;

    private float mBoxWidthPercent;
    private float mBoxHeightRatio = 1.0f;

    private int mBoxType = BOX_TYPE_RECT;
    private int mBoxBackgroundColor;
    private int mBoxBorderColor;
    private float mBoxBorderSize;
    private float mBoxSpace;
    private float mBoxSpacePercent;
    private int mBoxChainStyle = BOX_CHAIN_STYLE_FREE;

    private boolean mMergeRectBoxEnabled = true;
    private float mMergedRectBoxDividerWidth;

    private float mRectBoxRadius;

    private float mCursorWidth;

    private float mCursorMarginTop;

    private float mCursorMarginBottom;

    private int mCursorColor = Color.BLUE;

    private boolean mCursorEnabled = true;


    private float mMeasureBoxWidth, mMeasureBoxHeight;
    private float mMeasureContentWidth, mMeasureContentHeight;
    private float mMeasureBoxSpace;
    private float mMeasureBoxChainMargin;

    private boolean mDrawCursor;

    private final Paint mBoxBoardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mBoxBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mCursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final PorterDuffXfermode mXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);

    private RectF mTempRectF = new RectF();
    private Path mTempPath = new Path();
    private float[] mBoxRadii = new float[8];
    private List<RectF> mBoxRectFs = new ArrayList<>();

    private float mDensity;


    private PasswordListener mPasswordListener;

    public PasswordView(Context context) {
        super(context);
        init(null);
    }

    public PasswordView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PasswordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mDensity = getResources().getDisplayMetrics().density;
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PasswordView);
            mPasswordLength = typedArray.getInteger(R.styleable.PasswordView_passwordLength, 6);
            mPasswordType = typedArray.getInt(R.styleable.PasswordView_passwordType, PASSWORD_TYPE_STARS);

            mBoxMeasureType = typedArray.getInt(R.styleable.PasswordView_boxMeasureType, BOX_MEASURE_MODE_FILL);
            mBoxWidth = typedArray.getDimension(R.styleable.PasswordView_boxWidth, 0);
            mBoxHeight = typedArray.getDimension(R.styleable.PasswordView_boxHeight, 0);
            mBoxWidthPercent = typedArray.getFloat(R.styleable.PasswordView_boxWidthPercent, 0);
            mBoxHeightRatio = typedArray.getFloat(R.styleable.PasswordView_boxHeightRatio, 1.0f);
            mBoxBackgroundColor = typedArray.getColor(R.styleable.PasswordView_boxBackgroundColor, Color.TRANSPARENT);
            mBoxBorderColor = typedArray.getColor(R.styleable.PasswordView_boxBordColor, Color.BLACK);
            mBoxType = typedArray.getInt(R.styleable.PasswordView_boxType, BOX_TYPE_RECT);

            mBoxBorderSize = typedArray.getDimension(R.styleable.PasswordView_boxBorderSize, mDensity * 1);
            mBoxSpace = typedArray.getDimension(R.styleable.PasswordView_boxSpace, 0);
            mMergeRectBoxEnabled = typedArray.getBoolean(R.styleable.PasswordView_mergeRectBoxEnabled, true);
            mMergedRectBoxDividerWidth = typedArray.getDimension(R.styleable.PasswordView_mergedRectBoxDividerWidth, mDensity * 1);

            mRectBoxRadius = typedArray.getDimension(R.styleable.PasswordView_rectBoxRadius, 0);

            mCursorWidth = typedArray.getDimension(R.styleable.PasswordView_cursorWidth, mDensity * 2);
            mCursorMarginTop = typedArray.getDimension(R.styleable.PasswordView_cursorMarginTop, mDensity * 8);
            mCursorMarginBottom = typedArray.getDimension(R.styleable.PasswordView_cursorMarginBottom, mDensity * 8);
            mCursorColor = typedArray.getColor(R.styleable.PasswordView_cursorColor, Color.BLACK);
            mCursorEnabled = typedArray.getBoolean(R.styleable.PasswordView_cursorEnabled, true);

            typedArray.recycle();
        } else {
            mBoxBorderSize = mDensity * 1;
            mCursorWidth = mDensity * 2;
            mMergedRectBoxDividerWidth = mDensity * 1;
            mCursorMarginTop = mCursorMarginBottom = mDensity * 8;

            setBackground(null);
        }

        InputFilter[] filters = {new InputFilter.LengthFilter(mPasswordLength)};
        setFilters(filters);

        setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
        setLongClickable(false);
        setCursorVisible(false);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setCursorEnabled(boolean enabled) {
        mCursorEnabled = enabled;
        if (enabled) {
            if (getWindowToken() != null) {
                Handler handler = getHandler();
                if (handler != null) {
                    handler.post(mDrawCursorToggleTask);
                }
            }
        } else {
            Handler handler = getHandler();
            if (handler != null) {
                handler.removeCallbacks(mDrawCursorToggleTask);
            }
        }
    }

    public void setPasswordListener(PasswordListener passwordListener) {
        mPasswordListener = passwordListener;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        removeTextChangedListener(this);
        addTextChangedListener(this);
        if (mCursorEnabled) {
            Handler handler = getHandler();
            if (handler != null) {
                handler.post(mDrawCursorToggleTask);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeTextChangedListener(this);
        Handler handler = getHandler();
        if (handler != null) {
            handler.removeCallbacks(mDrawCursorToggleTask);
        }
    }


    /*private float computeWidth() {

    }*/


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int horizontalPadding = getPaddingLeft() + getPaddingRight();
        int verticalPadding = getPaddingTop() + getPaddingBottom();


        boolean mergeRectBox = mMergeRectBoxEnabled && mBoxType == BOX_TYPE_RECT && mBoxSpace == 0 && mBoxSpacePercent == 0;

        float width = 0;
        float height = 0;

        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED: {
                /*Log.d(TAG, "onMeasure: UNSPECIFIED");
                //int defaultWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
                float expectedBoxWidth = mBoxWidth;
                if (mBoxWidthPercent > 0) {
                    expectedBoxWidth = width * mBoxWidthPercent;
                }
                width = expectedBoxWidth * mPasswordLength
                        + mBoxBorderSize * 2 * mPasswordLength
                        + (mergeRectBox ? (mMergedRectBoxDividerWidth - 2 * mBoxBorderSize) * (mPasswordLength - 1) : 0)
                        + mBoxSpace * (mPasswordLength - 1)
                        + horizontalPadding;
                width = Math.max(0, width);
                mMeasureBoxWidth = expectedBoxWidth;*/
                break;
            }
            case MeasureSpec.EXACTLY: {
                Log.d(TAG, "onMeasure: EXACTLY");
                width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);

                //mBoxChainStyle


                switch (mBoxMeasureType) {
                    case BOX_MEASURE_MODE_FILL: {

                        switch (mBoxChainStyle) {
                            case BOX_CHAIN_STYLE_FREE:
                            case BOX_CHAIN_STYLE_SPREAD_INSIDE: {
                                mMeasureBoxSpace = mBoxSpace;
                                if (mBoxSpacePercent > 0) {
                                    mMeasureBoxSpace = mBoxSpacePercent * width;
                                }
                                mMeasureBoxChainMargin = 0;
                                break;
                            }
                            case BOX_CHAIN_STYLE_SPREAD: {
                                mMeasureBoxSpace = mBoxSpace;
                                if (mBoxSpacePercent > 0) {
                                    mMeasureBoxSpace = mBoxSpacePercent * width;
                                }
                                mMeasureBoxChainMargin = mMeasureBoxSpace;
                                break;
                            }
                            case BOX_CHAIN_STYLE_PACKET: {
                                mMeasureBoxSpace = 0;
                                mMeasureBoxChainMargin = 0;
                                break;
                            }
                        }

                        float measureBoxWidth = (width
                                - mBoxBorderSize * 2 * mPasswordLength
                                - (mergeRectBox ? (mMergedRectBoxDividerWidth - 2 * mBoxBorderSize) * (mPasswordLength - 1) : 0)
                                - mMeasureBoxSpace * (mPasswordLength - 1)
                                - horizontalPadding)
                                / mPasswordLength;
                        mMeasureBoxWidth = Math.max(0, measureBoxWidth);
                        break;
                    }
                    case BOX_MEASURE_MODE_BOUND: {
                        float measureBoxWidth = mMeasureBoxWidth = (width
                                - mBoxBorderSize * 2 * mPasswordLength
                                - (mergeRectBox ? (mMergedRectBoxDividerWidth - 2 * mBoxBorderSize) * (mPasswordLength - 1) : 0)
                                - mMeasureBoxSpace * (mPasswordLength - 1)
                                - horizontalPadding)
                                / mPasswordLength;
                        measureBoxWidth = Math.max(0, measureBoxWidth);

                        float expectedBoxWidth = mBoxWidth;
                        if (mBoxWidthPercent > 0) {
                            expectedBoxWidth = width * mBoxWidthPercent;
                        }

                        mMeasureBoxWidth = Math.min(measureBoxWidth, expectedBoxWidth);
                        break;
                    }
                    case BOX_MEASURE_MODE_FREE: {
                        float expectedBoxWidth = mBoxWidth;
                        if (mBoxWidthPercent > 0) {
                            expectedBoxWidth = width * mBoxWidthPercent;
                        }
                        mMeasureBoxWidth = expectedBoxWidth;
                        break;
                    }
                }

                break;
            }
            case MeasureSpec.AT_MOST: {
                Log.d(TAG, "onMeasure: AT_MOST");
                int maxWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
                switch (mBoxMeasureType) {
                    case BOX_MEASURE_MODE_FILL: {
                        float measureBoxWidth = (maxWidth
                                - mBoxBorderSize * 2 * mPasswordLength
                                - (mergeRectBox ? (mMergedRectBoxDividerWidth - 2 * mBoxBorderSize) * (mPasswordLength - 1) : 0)
                                - mBoxSpace * (mPasswordLength - 1)
                                - horizontalPadding)
                                / mPasswordLength;
                        mMeasureBoxWidth = Math.max(0, measureBoxWidth);
                        width = maxWidth;
                        break;
                    }
                    case BOX_MEASURE_MODE_BOUND: {
                        float maxBoxWidth = (maxWidth
                                - mBoxBorderSize * 2 * mPasswordLength
                                - (mergeRectBox ? (mMergedRectBoxDividerWidth - 2 * mBoxBorderSize) * (mPasswordLength - 1) : 0)
                                - mBoxSpace * (mPasswordLength - 1)
                                - horizontalPadding)
                                / mPasswordLength;
                        if (maxBoxWidth <= 0) {
                            mMeasureBoxWidth = 0;
                            width = maxWidth;
                        } else {

                            float expectedBoxWidth = mBoxWidth;
                            if (mBoxWidthPercent > 0) {
                                expectedBoxWidth = maxWidth * mBoxWidthPercent;
                            }

                            mMeasureBoxWidth = Math.min(maxBoxWidth, expectedBoxWidth);
                            width = horizontalPadding
                                    + mMeasureBoxWidth * mPasswordLength
                                    + mBoxBorderSize * 2 * mPasswordLength
                                    + (mergeRectBox ? (mMergedRectBoxDividerWidth - 2 * mBoxBorderSize) * (mPasswordLength - 1) : 0)
                                    + mBoxSpace * (mPasswordLength - 1);
                        }
                        break;
                    }
                    case BOX_MEASURE_MODE_FREE: {
                        float expectedBoxWidth = mBoxWidth;
                        if (mBoxWidthPercent > 0) {
                            expectedBoxWidth = maxWidth * mBoxWidthPercent;
                        }
                        mMeasureBoxWidth = expectedBoxWidth;

                        float expectedWidth = horizontalPadding
                                + mMeasureBoxWidth * mPasswordLength
                                + mBoxBorderSize * 2 * mPasswordLength
                                + (mergeRectBox ? (mMergedRectBoxDividerWidth - 2 * mBoxBorderSize) * (mPasswordLength - 1) : 0)
                                + mBoxSpace * (mPasswordLength - 1);

                        width = Math.min(expectedWidth, maxWidth);
                        break;
                    }
                }
                break;
            }
        }

        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED: {
                float expectedBoxHeight = mBoxHeight;
                if (mBoxHeightRatio > 0) {
                    expectedBoxHeight = mMeasureBoxWidth * mBoxHeightRatio;
                }
                height = verticalPadding
                        + expectedBoxHeight
                        + 2 * mBoxBorderSize;
                mMeasureBoxHeight = expectedBoxHeight;
                break;
            }
            case MeasureSpec.EXACTLY: {
                height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
                switch (mBoxMeasureType) {
                    case BOX_MEASURE_MODE_FILL: {
                        float maxBoxHeight = height
                                - verticalPadding
                                - 2 * mBoxBorderSize;
                        maxBoxHeight = Math.max(0, maxBoxHeight);

                        float expectedBoxHeight = mBoxHeight;
                        if (mBoxHeightRatio > 0) {
                            expectedBoxHeight = mMeasureBoxWidth * mBoxHeightRatio;
                        }
                        mMeasureBoxHeight = Math.min(maxBoxHeight, expectedBoxHeight);
                        break;
                    }
                    case BOX_MEASURE_MODE_BOUND: {
                        float measureBoxHeight = height
                                - verticalPadding
                                - 2 * mBoxBorderSize;
                        measureBoxHeight = Math.max(0, measureBoxHeight);

                        float expectedBoxHeight = mBoxHeight;
                        if (mBoxHeightRatio > 0) {
                            expectedBoxHeight = mMeasureBoxWidth * mBoxHeightRatio;
                        }
                        mMeasureBoxHeight = Math.min(measureBoxHeight, expectedBoxHeight);
                        break;
                    }
                    case BOX_MEASURE_MODE_FREE: {
                        float expectedBoxHeight = mBoxHeight;
                        if (mBoxHeightRatio > 0) {
                            expectedBoxHeight = mMeasureBoxWidth * mBoxHeightRatio;
                        }
                        mMeasureBoxHeight = expectedBoxHeight;
                        break;
                    }
                }
                break;
            }
            case MeasureSpec.AT_MOST: {
                int maxHeight = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
                switch (mBoxMeasureType) {
                    case BOX_MEASURE_MODE_FILL: {
                        float maxBoxHeight = maxHeight
                                - verticalPadding
                                - 2 * mBoxBorderSize;

                        float expectedBoxHeight = mBoxHeight;
                        if (mBoxHeightRatio > 0) {
                            expectedBoxHeight = mMeasureBoxWidth * mBoxHeightRatio;
                        }
                        mMeasureBoxHeight = Math.min(maxBoxHeight, expectedBoxHeight);
                        height = verticalPadding
                                + mMeasureBoxHeight
                                + 2 * mBoxBorderSize;
                        break;
                    }
                    case BOX_MEASURE_MODE_BOUND: {
                        float maxBoxHeight = maxHeight
                                - verticalPadding
                                - 2 * mBoxBorderSize;
                        if (maxBoxHeight <= 0) {
                            mMeasureBoxHeight = 0;
                            height = maxHeight;
                        } else {
                            float expectedBoxHeight = mBoxHeight;
                            if (mBoxHeightRatio > 0) {
                                expectedBoxHeight = mMeasureBoxWidth * mBoxHeightRatio;
                            }
                            mMeasureBoxHeight = Math.min(maxBoxHeight, expectedBoxHeight);
                            height = verticalPadding
                                    + mMeasureBoxHeight
                                    + 2 * mBoxBorderSize;
                        }
                        break;
                    }
                    case BOX_MEASURE_MODE_FREE: {
                        float expectedBoxHeight = mBoxHeight;
                        if (mBoxHeightRatio > 0) {
                            expectedBoxHeight = mMeasureBoxWidth * mBoxHeightRatio;
                        }
                        mMeasureBoxHeight = expectedBoxHeight;

                        float expectedHeight = verticalPadding
                                + mMeasureBoxHeight
                                + 2 * mBoxBorderSize;
                        height = Math.min(maxHeight, expectedHeight);

                        break;
                    }
                }
                break;
            }
        }


        mMeasureContentWidth = horizontalPadding
                + mMeasureBoxWidth * mPasswordLength
                + mBoxBorderSize * 2 * mPasswordLength
                + (mergeRectBox ? (mMergedRectBoxDividerWidth - 2 * mBoxBorderSize) * (mPasswordLength - 1) : 0)
                + mBoxSpace * (mPasswordLength - 1);

        mMeasureContentHeight = verticalPadding
                + mMeasureBoxHeight
                + 2 * mBoxBorderSize;

        setMeasuredDimension(Math.round(width), Math.round(height));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);不绘制EditText本身的文字
        canvas.save();
        float dx = (getMeasuredWidth() - mMeasureContentWidth) * 0.5f + getPaddingLeft() + mMeasureBoxChainMargin;
        float dy = (getMeasuredHeight() - mMeasureContentHeight) * 0.5f + getPaddingTop();
        canvas.translate(dx, dy);
        List<RectF> boxRectFs = drawBox(canvas);
        if (boxRectFs == null) return;
        drawCursor(canvas, boxRectFs);
        drawText(canvas, boxRectFs);
        canvas.restore();
    }

    private List<RectF> drawBox(Canvas canvas) {
        switch (mBoxType) {
            case BOX_TYPE_RECT: {
                return drawRectBox(canvas);
            }
            case BOX_TYPE_OVAL: {
                return drawOvalBox(canvas);
            }
            case BOX_TYPE_UNDERLINE: {
                return drawUnderlineBox(canvas);
            }
        }
        return null;
    }

    private List<RectF> drawRectBox(Canvas canvas) {
        mBoxBoardPaint.setColor(mBoxBorderColor);
        mBoxBoardPaint.setStyle(Paint.Style.STROKE);

        mBoxBackgroundPaint.setColor(mBoxBackgroundColor);
        mBoxBackgroundPaint.setStyle(Paint.Style.FILL);


        boolean mergedRectBox = mMergeRectBoxEnabled && mBoxSpace == 0;

        if (mergedRectBox) {
            mBoxBoardPaint.setStrokeWidth(mBoxBorderSize);

            float left = mBoxBorderSize * 0.5f;
            float top = mBoxBorderSize * 0.5f;
            float right = mMeasureContentWidth - (getPaddingLeft() + getPaddingRight()) - mBoxBorderSize * 0.5f;
            float bottom = mMeasureContentHeight - (getPaddingTop() + getPaddingBottom()) - mBoxBorderSize * 0.5f;
            mTempRectF.set(left, top, right, bottom);

            canvas.drawRoundRect(mTempRectF, mRectBoxRadius, mRectBoxRadius, mBoxBoardPaint);

            RectF lastBoxRectF = null;

            mBoxBoardPaint.setStrokeWidth(mMergedRectBoxDividerWidth);
            mBoxBoardPaint.setColor(mBoxBorderColor);
            mBoxBoardPaint.setStyle(Paint.Style.STROKE);
            for (int index = 0; index < mPasswordLength; index++) {
                if (index == 0) {
                    left = mBoxBorderSize;
                    top = mBoxBorderSize;
                    right = left + mMeasureBoxWidth;
                    bottom = top + mMeasureBoxHeight;
                } else {
                    left = lastBoxRectF.right + mMergedRectBoxDividerWidth;
                    top = mBoxBorderSize;
                    right = left + mMeasureBoxWidth;
                    bottom = top + mMeasureBoxHeight;
                }
                RectF boxRectF;
                if (mBoxRectFs.size() > index) {
                    boxRectF = mBoxRectFs.get(index);
                } else {
                    boxRectF = new RectF();
                    mBoxRectFs.add(boxRectF);
                }
                boxRectF.set(left, top, right, bottom);
                lastBoxRectF = boxRectF;

                if (mBoxBorderSize > 0) {
                    if (index < mPasswordLength - 1) {
                        float startX = boxRectF.right + mMergedRectBoxDividerWidth * 0.5f;
                        float startY = boxRectF.top;
                        float stopX = startX;
                        float stopY = boxRectF.bottom;

                        canvas.drawLine(startX, startY, stopX, stopY, mBoxBoardPaint);
                    }
                }

                drawMergedRectBoxBackground(canvas, index, boxRectF);
            }

        } else {
            mBoxBoardPaint.setStrokeWidth(mBoxBorderSize);

            for (int index = 0; index < mPasswordLength; index++) {

                float left = mMeasureBoxWidth * index + mBoxBorderSize * 2 * index + mBoxSpace * index + mBoxBorderSize * 0.5f;
                float top = mBoxBorderSize * 0.5f;
                float right = left + mMeasureBoxWidth + mBoxBorderSize;
                float bottom = top + mMeasureBoxHeight + mBoxBorderSize;

                mTempRectF.set(left, top, right, bottom);

                canvas.save();

                canvas.drawRoundRect(mTempRectF, mRectBoxRadius, mRectBoxRadius, mBoxBoardPaint);

                RectF boxRectF;
                if (mBoxRectFs.size() > index) {
                    boxRectF = mBoxRectFs.get(index);
                } else {
                    boxRectF = new RectF();
                    mBoxRectFs.add(boxRectF);
                }
                left += mBoxBorderSize * 0.5f;
                top += mBoxBorderSize * 0.5f;
                right -= mBoxBorderSize * 0.5f;
                bottom -= mBoxBorderSize * 0.5f;
                boxRectF.set(left, top, right, bottom);


                mBoxBackgroundPaint.setXfermode(mXfermode);
                canvas.drawRoundRect(mTempRectF, mRectBoxRadius, mRectBoxRadius, mBoxBackgroundPaint);
                mBoxBackgroundPaint.setXfermode(null);

                canvas.restore();
            }
        }

        return mBoxRectFs;
    }

    private void drawMergedRectBoxBackground(Canvas canvas, int index, RectF boxRectF) {
        float boxBackgroundRadius = getBoxBackgroundRadius();
        mTempPath.reset();
        if (index == 0) {
            mBoxRadii[0] = boxBackgroundRadius;
            mBoxRadii[1] = boxBackgroundRadius;

            mBoxRadii[2] = 0;
            mBoxRadii[3] = 0;

            mBoxRadii[4] = 0;
            mBoxRadii[5] = 0;

            mBoxRadii[6] = boxBackgroundRadius;
            mBoxRadii[7] = boxBackgroundRadius;

            mTempPath.addRoundRect(boxRectF, mBoxRadii, Path.Direction.CW);
        } else if (index == mPasswordLength - 1) {
            mBoxRadii[0] = 0;
            mBoxRadii[1] = 0;

            mBoxRadii[2] = boxBackgroundRadius;
            mBoxRadii[3] = boxBackgroundRadius;

            mBoxRadii[4] = boxBackgroundRadius;
            mBoxRadii[5] = boxBackgroundRadius;

            mBoxRadii[6] = 0;
            mBoxRadii[7] = 0;

            mTempPath.addRoundRect(boxRectF, mBoxRadii, Path.Direction.CW);
        } else {
            mBoxRadii[0] = 0;
            mBoxRadii[1] = 0;

            mBoxRadii[2] = 0;
            mBoxRadii[3] = 0;

            mBoxRadii[4] = 0;
            mBoxRadii[5] = 0;

            mBoxRadii[6] = 0;
            mBoxRadii[7] = 0;

            mTempPath.addRoundRect(boxRectF, mBoxRadii, Path.Direction.CW);
        }
        canvas.drawPath(mTempPath, mBoxBackgroundPaint);
    }

    private float getBoxBackgroundRadius() {
        if (mRectBoxRadius == 0) return 0;
        if (mBoxBorderSize == 0) return 0;
        float radius = 1.5f * (mBoxBorderSize / mDensity - 1) + 0.5f;
        return mRectBoxRadius - Math.max(0, radius);
    }

    private List<RectF> drawOvalBox(Canvas canvas) {
        mBoxBoardPaint.setColor(mBoxBorderColor);
        mBoxBoardPaint.setStyle(Paint.Style.STROKE);
        mBoxBoardPaint.setStrokeWidth(mBoxBorderSize);

        mBoxBackgroundPaint.setColor(mBoxBackgroundColor);
        mBoxBackgroundPaint.setStyle(Paint.Style.FILL);

        for (int index = 0; index < mPasswordLength; index++) {

            float left = mMeasureBoxWidth * index + mBoxBorderSize * 2 * index + mBoxSpace * index + mBoxBorderSize * 0.5f;
            float top = mBoxBorderSize * 0.5f;
            float right = left + mMeasureBoxWidth + mBoxBorderSize;
            float bottom = top + mMeasureBoxHeight + mBoxBorderSize;

            mTempRectF.set(left, top, right, bottom);

            canvas.save();

            canvas.drawOval(mTempRectF, mBoxBoardPaint);

            RectF boxRect;
            if (mBoxRectFs.size() > index) {
                boxRect = mBoxRectFs.get(index);
            } else {
                boxRect = new RectF();
                mBoxRectFs.add(boxRect);
            }
            left += mBoxBorderSize * 0.5f;
            top += mBoxBorderSize * 0.5f;
            right -= mBoxBorderSize * 0.5f;
            bottom -= mBoxBorderSize * 0.5f;
            boxRect.set(left, top, right, bottom);

            mBoxBackgroundPaint.setXfermode(mXfermode);
            canvas.drawOval(mTempRectF, mBoxBackgroundPaint);
            mBoxBackgroundPaint.setXfermode(null);

            canvas.restore();
        }

        return mBoxRectFs;
    }

    private List<RectF> drawUnderlineBox(Canvas canvas) {
        mBoxBoardPaint.setColor(mBoxBorderColor);
        mBoxBoardPaint.setStyle(Paint.Style.FILL);
        mBoxBoardPaint.setStrokeWidth(mBoxBorderSize);

        mBoxBackgroundPaint.setColor(mBoxBackgroundColor);
        mBoxBackgroundPaint.setStyle(Paint.Style.FILL);

        for (int index = 0; index < mPasswordLength; index++) {

            float startX = mMeasureBoxWidth * index + mBoxBorderSize * 2 * index + mBoxSpace * index;
            float startY = mMeasureBoxHeight + mBoxBorderSize * 1.5f;
            float stopX = startX + mMeasureBoxWidth + mBoxBorderSize * 2;
            float stopY = startY;

            canvas.drawLine(startX, startY, stopX, stopY, mBoxBoardPaint);

            RectF boxRect;
            if (mBoxRectFs.size() > index) {
                boxRect = mBoxRectFs.get(index);
            } else {
                boxRect = new RectF();
                mBoxRectFs.add(boxRect);
            }

            float left = mMeasureBoxWidth * index + mBoxBorderSize * 2 * index + mBoxSpace * index;
            float top = mBoxBorderSize;
            float right = left + mMeasureBoxWidth + mBoxBorderSize * 2;
            float bottom = top + mMeasureBoxHeight;

            boxRect.set(left, top, right, bottom);

            canvas.drawRect(boxRect, mBoxBackgroundPaint);
        }

        return mBoxRectFs;
    }

    private void drawCursor(Canvas canvas, List<RectF> boxRectFs) {
        if (!isFocused()) return;
        if (mDrawCursor) {
            mCursorPaint.setColor(mCursorColor);
            mCursorPaint.setStyle(Paint.Style.FILL);
            mCursorPaint.setStrokeWidth(mCursorWidth);

            Editable text = getText();
            int textLength = text == null ? 0 : text.length();
            if (textLength >= mPasswordLength) return;

            RectF rectF = boxRectFs.get(textLength);


            float startX = rectF.centerX() - mCursorWidth * 0.5f;
            float startY = rectF.top + mCursorMarginTop;
            float stopX = startX;
            float stopY = rectF.bottom - mCursorMarginBottom;

            canvas.drawLine(startX, startY, stopX, stopY, mCursorPaint);
        }
    }

    private void drawText(Canvas canvas, List<RectF> boxRectFs) {
        Editable text = getText();
        int textLength = text == null ? 0 : text.length();
        if (textLength == 0) return;

        String str = text.toString();

        textLength = Math.min(mPasswordLength, textLength);
        float textSize = getTextSize();
        int textColor = getTextColors().getDefaultColor();

        if (mPasswordType == PASSWORD_TYPE_STARS) textSize *= 1.5f;

        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(textColor);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();

        for (int index = 0; index < textLength; index++) {
            RectF rectF = boxRectFs.get(index);
            switch (mPasswordType) {
                case PASSWORD_TYPE_STARS: {
                    float fontWidth = mTextPaint.measureText("*");
                    float baseX = rectF.centerX();
                    float baseY = (rectF.bottom + rectF.top - fontMetrics.bottom - fontMetrics.top) * 0.5f;
                    baseY += fontWidth * 0.26f;
                    canvas.drawText("*", baseX, baseY, mTextPaint);
                    break;
                }
                case PASSWORD_TYPE_CIRCLE: {
                    float cx = rectF.centerX();
                    float cy = rectF.centerY();
                    float radius = textSize * 0.5f;
                    canvas.drawCircle(cx, cy, radius, mTextPaint);
                    break;
                }
                case PASSWORD_TYPE_TEXT: {
                    char charAt = str.charAt(index);
                    String strAt = String.valueOf(charAt);
                    float baseX = rectF.centerX();
                    float baseY = (rectF.bottom + rectF.top - fontMetrics.bottom - fontMetrics.top) * 0.5f;
                    canvas.drawText(strAt, baseX, baseY, mTextPaint);
                    break;
                }
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        PasswordListener passwordListener = mPasswordListener;
        if (passwordListener != null) {
            int length = s == null ? 0 : s.length();
            String str = s == null ? null : s.toString();
            passwordListener.onChanged(str);
            if (length == 0) {
                passwordListener.onCleared();
            } else if (length == mPasswordLength) {
                passwordListener.onFinished(str);
            }
        }
    }

    private class DrawCursorToggleTask implements Runnable {

        @Override
        public void run() {
            if (!mCursorEnabled) return;
            mDrawCursor = !mDrawCursor;
            postInvalidate();
            Handler handler = getHandler();
            if (handler != null) {
                handler.postDelayed(this, 500);
            }
        }
    }

    public interface PasswordListener {

        void onCleared();

        void onChanged(String password);

        void onFinished(String password);

    }

    public interface IMeasurer {

        void measure(PasswordView passwordView, MeasureInfo measureInfo, MeasureResult result);

    }

    @SuppressWarnings("all")
    public static class MeasureInfo implements Cloneable {

        public int widthMeasureSpec, heightMeasureSpec;

        public int passwordLength;

        public int boxType;
        public int boxMeasureMode;
        public int boxChainStyle;

        public float boxWidth, boxHeight;
        public float boxWidthPercent;
        public float boxHeightRatio;

        public float boxBorderSize;
        public float boxSpace;
        public float boxSpacePercent;

        public boolean mergeRectBoxEnabled;
        public float mergedRectBoxDividerWidth;

        @Override
        public MeasureInfo clone() {
            try {
                return (MeasureInfo) super.clone();
            } catch (Exception e) {
                e.printStackTrace();
            }
            MeasureInfo measureInfo = new MeasureInfo();
            measureInfo.widthMeasureSpec = this.widthMeasureSpec;
            measureInfo.heightMeasureSpec = this.heightMeasureSpec;
            measureInfo.passwordLength = this.passwordLength;
            measureInfo.boxType = this.boxType;
            measureInfo.boxMeasureMode = this.boxMeasureMode;
            measureInfo.boxChainStyle = this.boxChainStyle;
            measureInfo.boxWidth = this.boxWidth;
            measureInfo.boxHeight = this.boxHeight;
            measureInfo.boxWidthPercent = this.boxWidthPercent;
            measureInfo.boxHeightRatio = this.boxHeightRatio;
            measureInfo.boxBorderSize = this.boxBorderSize;
            measureInfo.boxSpace = this.boxSpace;
            measureInfo.boxSpacePercent = this.boxSpacePercent;
            measureInfo.mergeRectBoxEnabled = this.mergeRectBoxEnabled;
            measureInfo.mergedRectBoxDividerWidth = this.mergedRectBoxDividerWidth;
            return measureInfo;
        }
    }

    public static class MeasureResult {

        public float measureWidth, measureHeight;
        public float measureBoxWidth, measureBoxHeight;
        public float measureBoxSpace;
        public float measureBoxChainMargin;

        public boolean mergeRectBox;
    }


    public static class FillMeasurer implements IMeasurer {

        @Override
        public void measure(PasswordView passwordView, MeasureInfo measureInfo, MeasureResult result) {
            measureWidth(passwordView, measureInfo, result);
            measureHeight(passwordView, measureInfo, result);
        }

        private void measureWidth(PasswordView view, MeasureInfo info, MeasureResult result) {

            int horizontalPadding = view.getPaddingLeft() + view.getPaddingRight();

            float width = getDefaultSize(view.getSuggestedMinimumWidth(), info.widthMeasureSpec);
            result.measureWidth = width;
            measureBoxChain(width, info, result);

            boolean mergeRectBox = info.mergeRectBoxEnabled
                    && info.boxType == BOX_TYPE_RECT
                    && result.measureBoxSpace == 0;

            result.mergeRectBox = mergeRectBox;

            float measureBoxWidth = (width
                    - info.boxBorderSize * 2 * info.passwordLength
                    - (mergeRectBox ? (info.mergedRectBoxDividerWidth - 2 * info.boxBorderSize) * (info.passwordLength - 1) : 0)
                    - result.measureBoxSpace * (info.passwordLength - 1)
                    - horizontalPadding
                    - result.measureBoxChainMargin * 2)
                    / info.passwordLength;
            result.measureBoxWidth = Math.max(0, measureBoxWidth);

        }

        private void measureBoxChain(float width, MeasureInfo info, MeasureResult result) {
            switch (info.boxChainStyle) {
                case BOX_CHAIN_STYLE_FREE:
                case BOX_CHAIN_STYLE_SPREAD_INSIDE: {
                    float boxSpace = info.boxSpace;
                    if (info.boxSpacePercent > 0) {
                        boxSpace = width * info.boxSpacePercent;
                    }
                    result.measureBoxSpace = boxSpace;
                    result.measureBoxChainMargin = 0;
                    break;
                }
                case BOX_CHAIN_STYLE_SPREAD: {
                    float boxSpace = info.boxSpace;
                    if (info.boxSpacePercent > 0) {
                        boxSpace = width * info.boxSpacePercent;
                    }
                    result.measureBoxSpace = boxSpace;
                    result.measureBoxChainMargin = boxSpace;
                    break;
                }
                case BOX_CHAIN_STYLE_PACKET: {
                    result.measureBoxSpace = 0;
                    result.measureBoxChainMargin = 0;
                    break;
                }
            }
        }

        private void measureHeight(PasswordView view, MeasureInfo info, MeasureResult result) {
            int heightMode = MeasureSpec.getMode(info.heightMeasureSpec);
            int verticalPadding = view.getPaddingTop() + view.getPaddingBottom();
            switch (heightMode) {
                case MeasureSpec.UNSPECIFIED: {
                    float boxHeight = info.boxHeight;
                    if (info.boxHeightRatio > 0) {
                        boxHeight = result.measureBoxWidth * info.boxHeightRatio;
                    }
                    if (boxHeight == 0) {
                        float height = getDefaultSize(view.getSuggestedMinimumHeight(), info.heightMeasureSpec);
                        boxHeight = height
                                - verticalPadding
                                - 2 * info.boxBorderSize;
                        boxHeight = Math.max(0, boxHeight);
                    }
                    result.measureBoxHeight = boxHeight;
                    result.measureHeight = boxHeight + verticalPadding + 2 * info.boxBorderSize;
                    break;
                }
                case MeasureSpec.EXACTLY: {
                    float height = getDefaultSize(view.getSuggestedMinimumHeight(), info.heightMeasureSpec);
                    result.measureHeight = height;
                    float maxBoxHeight = height
                            - verticalPadding
                            - 2 * info.boxBorderSize;
                    maxBoxHeight = Math.max(0, maxBoxHeight);

                    float boxHeight = info.boxHeight;
                    if (info.boxHeightRatio > 0) {
                        boxHeight = result.measureBoxWidth * info.boxHeightRatio;
                    }
                    if (boxHeight == 0) {
                        boxHeight = maxBoxHeight;
                    } else {
                        boxHeight = Math.min(maxBoxHeight, boxHeight);
                    }
                    result.measureBoxHeight = boxHeight;
                    break;
                }
                case MeasureSpec.AT_MOST: {
                    float maxHeight = getDefaultSize(view.getSuggestedMinimumHeight(), info.heightMeasureSpec);
                    float maxBoxHeight = maxHeight
                            - verticalPadding
                            - 2 * info.boxBorderSize;
                    maxBoxHeight = Math.max(0, maxBoxHeight);

                    float boxHeight = info.boxHeight;
                    if (info.boxHeightRatio > 0) {
                        boxHeight = result.measureBoxWidth * info.boxHeightRatio;
                    }
                    if (boxHeight == 0) {
                        boxHeight = maxBoxHeight;
                    } else {
                        boxHeight = Math.min(maxBoxHeight, boxHeight);
                    }
                    result.measureBoxHeight = boxHeight;
                    result.measureHeight = boxHeight + verticalPadding + 2 * info.boxBorderSize;
                    break;
                }
            }
        }
    }

    public static class BoundMeasurer implements IMeasurer {

        @Override
        public void measure(PasswordView passwordView, MeasureInfo measureInfo, MeasureResult result) {
            measureWidth(passwordView, measureInfo, result);
            measureHeight(passwordView, measureInfo, result);
        }


        private float xxx(MeasureInfo info) {
            float boxWidth = info.boxWidth;
            float boxWidthPercent = info.boxWidthPercent;

            float boxSpace = info.boxSpace;
            float boxSpacePercent = info.boxSpacePercent;



        }


        private void measureWidth(PasswordView view, MeasureInfo info, MeasureResult result) {
            int widthMode = MeasureSpec.getMode(info.widthMeasureSpec);
            int horizontalPadding = view.getPaddingLeft() + view.getPaddingRight();

            switch (widthMode) {
                case MeasureSpec.UNSPECIFIED: {
                    float defaultWidth = getDefaultSize(view.getSuggestedMinimumWidth(), info.widthMeasureSpec);
                    if (info.boxWidthPercent <= 0) {
                        float boxWidth = info.boxWidth;

                    } else {

                    }
                    break;
                }
                case MeasureSpec.EXACTLY: {
                    float width = getDefaultSize(view.getSuggestedMinimumWidth(), info.widthMeasureSpec);
                    result.measureWidth = width;
                    float expectedBoxWidth = info.boxWidth;
                    if (info.boxWidthPercent > 0) {
                        expectedBoxWidth = width * info.boxWidthPercent;
                    }
                    measureBoxChainExactly(width, expectedBoxWidth, horizontalPadding, info, result);

                    boolean mergeRectBox = info.mergeRectBoxEnabled
                            && info.boxType == BOX_TYPE_RECT
                            && result.measureBoxSpace == 0;

                    result.mergeRectBox = mergeRectBox;

                    float maxBoxWidth = (width
                            - info.boxBorderSize * 2 * info.passwordLength
                            - (mergeRectBox ? (info.mergedRectBoxDividerWidth - 2 * info.boxBorderSize) * (info.passwordLength - 1) : 0)
                            - result.measureBoxSpace * (info.passwordLength - 1)
                            - horizontalPadding
                            - result.measureBoxChainMargin * 2)
                            / info.passwordLength;

                    result.measureBoxWidth = Math.min(expectedBoxWidth, maxBoxWidth);

                    break;
                }
                case MeasureSpec.AT_MOST: {
                    float maxWidth = getDefaultSize(view.getSuggestedMinimumWidth(), info.widthMeasureSpec);

                    float expectedBoxWidth = info.boxWidth;
                    if (info.boxWidthPercent > 0) {
                        expectedBoxWidth = maxWidth * info.boxWidthPercent;
                    }

                    measureBoxChainAtMost(maxWidth, expectedBoxWidth, horizontalPadding, info, result);

                    boolean mergeRectBox = info.mergeRectBoxEnabled
                            && info.boxType == BOX_TYPE_RECT
                            && result.measureBoxSpace == 0;

                    result.mergeRectBox = mergeRectBox;

                    float maxBoxWidth = (maxWidth
                            - info.boxBorderSize * 2 * info.passwordLength
                            - (mergeRectBox ? (info.mergedRectBoxDividerWidth - 2 * info.boxBorderSize) * (info.passwordLength - 1) : 0)
                            - result.measureBoxSpace * (info.passwordLength - 1)
                            - horizontalPadding
                            - result.measureBoxChainMargin * 2)
                            / info.passwordLength;

                    maxBoxWidth = Math.max(0, maxBoxWidth);

                    result.measureBoxWidth = Math.min(maxBoxWidth, expectedBoxWidth);

                    float expectedWidth = result.measureBoxWidth * info.passwordLength
                            + info.boxBorderSize * 2 * info.passwordLength
                            + (mergeRectBox ? (info.mergedRectBoxDividerWidth - 2 * info.boxBorderSize) * (info.passwordLength - 1) : 0)
                            + result.measureBoxSpace * (info.passwordLength - 1)
                            + horizontalPadding
                            + result.measureBoxChainMargin * 2;

                    result.measureWidth = Math.min(expectedWidth, maxWidth);
                    break;
                }
            }
        }

        private void measureBoxChainExactly(float width, float expectedBoxWidth, int horizontalPadding, MeasureInfo info, MeasureResult result) {
            switch (info.boxChainStyle) {
                case BOX_CHAIN_STYLE_FREE: {
                    float boxSpace = info.boxSpace;
                    if (info.boxSpacePercent > 0) {
                        boxSpace = width * info.boxSpacePercent;
                    }
                    result.measureBoxSpace = boxSpace;
                    result.measureBoxChainMargin = 0;
                    break;
                }
                case BOX_CHAIN_STYLE_SPREAD: {
                    float surplusWidth = width
                            - horizontalPadding
                            - expectedBoxWidth * info.passwordLength
                            - info.boxBorderSize * 2 * info.passwordLength;
                    if (surplusWidth <= 0) {
                        result.measureBoxSpace = result.measureBoxChainMargin = 0;
                        boolean mergeRectBox = info.mergeRectBoxEnabled && info.boxType == BOX_TYPE_RECT;
                        if (mergeRectBox) {
                            surplusWidth += (2 * info.boxBorderSize + info.mergedRectBoxDividerWidth) * (info.passwordLength - 1);
                            if (surplusWidth > 0) {
                                result.measureBoxChainMargin = surplusWidth / 2;
                            }
                        }
                    } else {
                        result.measureBoxSpace = result.measureBoxChainMargin = surplusWidth / (info.passwordLength + 1);
                    }
                    break;
                }
                case BOX_CHAIN_STYLE_SPREAD_INSIDE: {
                    if (info.passwordLength <= 1) {
                        result.measureBoxSpace = result.measureBoxChainMargin = 0;
                    } else {
                        float surplusWidth = width
                                - horizontalPadding
                                - expectedBoxWidth * info.passwordLength
                                - info.boxBorderSize * 2 * info.passwordLength;
                        if (surplusWidth <= 0) {
                            result.measureBoxSpace = result.measureBoxChainMargin = 0;
                        } else {
                            result.measureBoxSpace = surplusWidth / (info.passwordLength - 1);
                        }
                    }
                    break;
                }
                case BOX_CHAIN_STYLE_PACKET: {
                    float surplusWidth = width
                            - horizontalPadding
                            - expectedBoxWidth * info.passwordLength
                            - info.boxBorderSize * 2 * info.passwordLength;
                    if (surplusWidth <= 0) {
                        result.measureBoxSpace = result.measureBoxChainMargin = 0;
                    } else {
                        result.measureBoxSpace = 0;
                        result.measureBoxChainMargin = surplusWidth / 2;
                    }
                }
            }
        }

        private void measureBoxChainAtMost(float maxWidth, float expectedBoxWidth, int horizontalPadding, MeasureInfo info, MeasureResult result) {
            switch (info.boxChainStyle) {
                case BOX_CHAIN_STYLE_FREE: {
                    if (info.passwordLength <= 1) {
                        result.measureBoxSpace = result.measureBoxChainMargin = 0;
                        break;
                    }
                    float boxSpace = info.boxSpace;
                    float boxSpacePercent = info.boxSpacePercent;
                    if (boxSpacePercent <= 0) {
                        result.measureBoxSpace = boxSpace;
                        result.measureBoxChainMargin = 0;
                    } else {
                        if (boxSpacePercent >= 1.0f / (info.passwordLength - 1)) {
                            result.measureBoxSpace = maxWidth * boxSpacePercent;
                            result.measureBoxChainMargin = 0;
                        } else {
                            float expectedWidth = (horizontalPadding
                                    + expectedBoxWidth * info.passwordLength
                                    + info.boxBorderSize * info.passwordLength * 2)
                                    / (1 - boxSpacePercent * (info.passwordLength - 1));
                            expectedWidth = Math.max(expectedWidth, 0);

                            float width = Math.min(expectedWidth, maxWidth);

                            result.measureBoxSpace = width * boxSpacePercent;
                            result.measureBoxChainMargin = 0;
                        }
                    }
                    break;
                }
                case BOX_CHAIN_STYLE_SPREAD: {
                    float surplusWidth = maxWidth
                            - horizontalPadding
                            - expectedBoxWidth * info.passwordLength
                            - info.boxBorderSize * 2 * info.passwordLength;
                    if (surplusWidth <= 0) {
                        result.measureBoxSpace = result.measureBoxChainMargin = 0;
                        boolean mergeRectBox = info.mergeRectBoxEnabled && info.boxType == BOX_TYPE_RECT;
                        if (mergeRectBox) {
                            surplusWidth += (2 * info.boxBorderSize + info.mergedRectBoxDividerWidth) * (info.passwordLength - 1);
                            if (surplusWidth > 0) {
                                result.measureBoxChainMargin = surplusWidth / 2;
                            }
                        }
                    } else {
                        result.measureBoxSpace = result.measureBoxChainMargin = surplusWidth / (info.passwordLength + 1);
                    }
                    break;
                }
                case BOX_CHAIN_STYLE_SPREAD_INSIDE: {
                    if (info.passwordLength <= 1) {
                        result.measureBoxSpace = result.measureBoxChainMargin = 0;
                    } else {
                        float surplusWidth = maxWidth
                                - horizontalPadding
                                - expectedBoxWidth * info.passwordLength
                                - info.boxBorderSize * 2 * info.passwordLength;
                        if (surplusWidth <= 0) {
                            result.measureBoxSpace = result.measureBoxChainMargin = 0;
                        } else {
                            result.measureBoxSpace = surplusWidth / (info.passwordLength - 1);
                        }
                    }
                    break;
                }
                case BOX_CHAIN_STYLE_PACKET: {
                    float surplusWidth = maxWidth
                            - horizontalPadding
                            - expectedBoxWidth * info.passwordLength
                            - info.boxBorderSize * 2 * info.passwordLength;
                    if (surplusWidth <= 0) {
                        result.measureBoxSpace = result.measureBoxChainMargin = 0;
                    } else {
                        result.measureBoxSpace = 0;
                        result.measureBoxChainMargin = surplusWidth / 2;
                    }
                    break;
                }
            }
        }

        private void measureHeight(PasswordView view, MeasureInfo info, MeasureResult result) {

        }
    }

    public static class FreeMeasurer implements IMeasurer {

        @Override
        public void measure(PasswordView passwordView, MeasureInfo measureInfo, MeasureResult result) {

        }
    }
}