package com.hyh.fyp.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import com.hyh.fyp.R;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("AppCompatCustomView")
public class PasswordView extends EditText implements TextWatcher {

    private static final String TAG = "PasswordView_";

    private static final int BOX_MEASURE_MODE_FILL = 0;
    private static final int BOX_MEASURE_MODE_BOUND = 1;
    private static final int BOX_MEASURE_MODE_FREE = 2;

    private static final int BOX_TYPE_RECT = 0;
    private static final int BOX_TYPE_OVAL = 1;
    private static final int BOX_TYPE_UNDERLINE = 2;

    public static final int PASSWORD_TYPE_STARS = 0;
    public static final int PASSWORD_TYPE_CIRCLE = 1;
    public static final int PASSWORD_TYPE_TEXT = 2;

    private final DrawCursorToggleTask mDrawCursorToggleTask = new DrawCursorToggleTask();

    private int mPasswordLength = 6;
    private int mPasswordType = PASSWORD_TYPE_CIRCLE;

    private int mBoxMeasureType;

    private float mBoxWidth, mBoxHeight;

    private float mBoxWidthPercent;
    private float mBoxHeightRatio = 1.0f;

    private int mBoxType = BOX_TYPE_RECT;
    private int mBoxColor;
    private float mOutBorderSize;
    private float mBoxBorderSize;
    private float mBoxMargin;
    private boolean mMergeRectBoxEnabled = true;

    private float mBoxCornerRadius;

    private float mCursorWidth;

    private float mCursorMarginTop;

    private float mCursorMarginBottom;

    private int mCursorColor = Color.BLUE;

    private boolean mCursorEnabled = true;


    private float mMeasureBoxWidth, mMeasureBoxHeight;

    private float mMeasureContentWidth, mMeasureContentHeight;

    private boolean mDrawCursor;

    private RectF mRectF = new RectF();

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private List<RectF> mBoxRectFs = new ArrayList<>();

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
        float density = getResources().getDisplayMetrics().density;
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PasswordView);
            mPasswordLength = typedArray.getInteger(R.styleable.PasswordView_passwordLength, 6);
            mPasswordType = typedArray.getInt(R.styleable.PasswordView_passwordType, PASSWORD_TYPE_STARS);

            mBoxMeasureType = typedArray.getInt(R.styleable.PasswordView_boxMeasureType, BOX_MEASURE_MODE_FILL);
            mBoxWidth = typedArray.getDimension(R.styleable.PasswordView_boxWidth, 0);
            mBoxHeight = typedArray.getDimension(R.styleable.PasswordView_boxHeight, 0);
            mBoxWidthPercent = typedArray.getFloat(R.styleable.PasswordView_boxWidthPercent, 0);
            mBoxHeightRatio = typedArray.getFloat(R.styleable.PasswordView_boxHeightRatio, 1.0f);
            mBoxType = typedArray.getInt(R.styleable.PasswordView_boxType, BOX_TYPE_RECT);
            mBoxColor = typedArray.getColor(R.styleable.PasswordView_boxColor, Color.BLACK);

            mOutBorderSize = typedArray.getDimension(R.styleable.PasswordView_outBorderSize, density * 2);
            mBoxBorderSize = typedArray.getDimension(R.styleable.PasswordView_boxBorderSize, density * 1);
            mBoxMargin = typedArray.getDimension(R.styleable.PasswordView_boxMargin, 0);
            mMergeRectBoxEnabled = typedArray.getBoolean(R.styleable.PasswordView_mergeRectBoxEnabled, true);
            mBoxCornerRadius = typedArray.getDimension(R.styleable.PasswordView_boxCornerRadius, 0);

            mCursorWidth = typedArray.getDimension(R.styleable.PasswordView_cursorWidth, density * 2);
            mCursorMarginTop = typedArray.getDimension(R.styleable.PasswordView_cursorMarginTop, density * 8);
            mCursorMarginBottom = typedArray.getDimension(R.styleable.PasswordView_cursorMarginBottom, density * 8);
            mCursorColor = typedArray.getColor(R.styleable.PasswordView_cursorColor, Color.BLACK);
            mCursorEnabled = typedArray.getBoolean(R.styleable.PasswordView_cursorEnabled, true);

            typedArray.recycle();
        } else {
            mOutBorderSize = density * 2;
            mBoxBorderSize = density * 1;
            mCursorWidth = density * 2;
            mCursorMarginTop = mCursorMarginBottom = density * 8;

            setBackground(null);
        }

        InputFilter[] filters = {new InputFilter.LengthFilter(mPasswordLength)};
        setFilters(filters);

        setLongClickable(false);
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        boolean mergeRectBox = mMergeRectBoxEnabled && mBoxType == BOX_TYPE_RECT && mBoxMargin == 0;

        float width = 0;
        float height = 0;

        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED: {
                float expectedBoxWidth = mBoxWidth;
                if (mBoxWidthPercent > 0) {
                    expectedBoxWidth = width * mBoxWidthPercent;
                }
                width = expectedBoxWidth * mPasswordLength
                        + mBoxBorderSize * 2 * mPasswordLength
                        + (mergeRectBox ? (mOutBorderSize - mBoxBorderSize) * 2 - mBoxBorderSize * (mPasswordLength - 1) : 0)
                        + mBoxMargin * (mPasswordLength - 1)
                        + getPaddingLeft() + getPaddingRight();
                width = Math.max(0, width);
                mMeasureBoxWidth = expectedBoxWidth;
                break;
            }
            case MeasureSpec.EXACTLY: {
                width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
                switch (mBoxMeasureType) {
                    case BOX_MEASURE_MODE_FILL: {
                        float measureBoxWidth = (width
                                - mBoxBorderSize * 2 * mPasswordLength
                                - (mergeRectBox ? (mOutBorderSize - mBoxBorderSize) * 2 - mBoxBorderSize * (mPasswordLength - 1) : 0)
                                - mBoxMargin * (mPasswordLength - 1)
                                - (getPaddingLeft() + getPaddingRight()))
                                / mPasswordLength;
                        mMeasureBoxWidth = Math.max(0, measureBoxWidth);
                        break;
                    }
                    case BOX_MEASURE_MODE_BOUND: {
                        float measureBoxWidth = mMeasureBoxWidth = (width
                                - mBoxBorderSize * 2 * mPasswordLength
                                - (mergeRectBox ? (mOutBorderSize - mBoxBorderSize) * 2 - mBoxBorderSize * (mPasswordLength - 1) : 0)
                                - mBoxMargin * (mPasswordLength - 1)
                                - (getPaddingLeft() + getPaddingRight()))
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
                int maxWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
                switch (mBoxMeasureType) {
                    case BOX_MEASURE_MODE_FILL: {
                        float measureBoxWidth = (maxWidth
                                - mBoxBorderSize * 2 * mPasswordLength
                                - (mergeRectBox ? (mOutBorderSize - mBoxBorderSize) * 2 - mBoxBorderSize * (mPasswordLength - 1) : 0)
                                - mBoxMargin * (mPasswordLength - 1)
                                - (getPaddingLeft() + getPaddingRight()))
                                / mPasswordLength;
                        mMeasureBoxWidth = Math.max(0, measureBoxWidth);
                        width = maxWidth;
                        break;
                    }
                    case BOX_MEASURE_MODE_BOUND: {
                        float maxBoxWidth = (maxWidth
                                - mBoxBorderSize * 2 * mPasswordLength
                                - (mergeRectBox ? (mOutBorderSize - mBoxBorderSize) * 2 - mBoxBorderSize * (mPasswordLength - 1) : 0)
                                - mBoxMargin * (mPasswordLength - 1)
                                - (getPaddingLeft() + getPaddingRight()))
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
                            width = getPaddingLeft() + getPaddingRight()
                                    + mMeasureBoxWidth * mPasswordLength
                                    + mBoxBorderSize * 2 * mPasswordLength
                                    + (mergeRectBox ? (mOutBorderSize - mBoxBorderSize) * 2 - mBoxBorderSize * (mPasswordLength - 1) : 0)
                                    + mBoxMargin * (mPasswordLength - 1);
                        }
                        break;
                    }
                    case BOX_MEASURE_MODE_FREE: {
                        float expectedBoxWidth = mBoxWidth;
                        if (mBoxWidthPercent > 0) {
                            expectedBoxWidth = maxWidth * mBoxWidthPercent;
                        }
                        mMeasureBoxWidth = expectedBoxWidth;

                        float expectedWidth = getPaddingLeft() + getPaddingRight()
                                + mMeasureBoxWidth * mPasswordLength
                                + mBoxBorderSize * 2 * mPasswordLength
                                + (mergeRectBox ? (mOutBorderSize - mBoxBorderSize) * 2 - mBoxBorderSize * (mPasswordLength - 1) : 0)
                                + mBoxMargin * (mPasswordLength - 1);

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
                height = getPaddingTop() + getPaddingBottom()
                        + expectedBoxHeight
                        + 2 * mBoxBorderSize
                        + (mergeRectBox ? (mOutBorderSize - mBoxBorderSize) * 2 : 0);
                mMeasureBoxHeight = expectedBoxHeight;
                break;
            }
            case MeasureSpec.EXACTLY: {
                height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
                switch (mBoxMeasureType) {
                    case BOX_MEASURE_MODE_FILL: {
                        float maxBoxHeight = height
                                - getPaddingTop() - getPaddingBottom()
                                - 2 * mBoxBorderSize
                                - (mergeRectBox ? (mOutBorderSize - mBoxBorderSize) * 2 : 0);
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
                                - getPaddingTop() - getPaddingBottom()
                                - 2 * mBoxBorderSize
                                - (mergeRectBox ? (mOutBorderSize - mBoxBorderSize) * 2 : 0);
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
                                - getPaddingTop() - getPaddingBottom()
                                - 2 * mBoxBorderSize
                                - (mergeRectBox ? (mOutBorderSize - mBoxBorderSize) * 2 : 0);

                        float expectedBoxHeight = mBoxHeight;
                        if (mBoxHeightRatio > 0) {
                            expectedBoxHeight = mMeasureBoxWidth * mBoxHeightRatio;
                        }
                        mMeasureBoxHeight = Math.min(maxBoxHeight, expectedBoxHeight);
                        height = getPaddingTop() + getPaddingBottom()
                                + mMeasureBoxHeight
                                + 2 * mBoxBorderSize
                                + (mergeRectBox ? (mOutBorderSize - mBoxBorderSize) * 2 : 0);
                        break;
                    }
                    case BOX_MEASURE_MODE_BOUND: {
                        float maxBoxHeight = maxHeight
                                - getPaddingTop() - getPaddingBottom()
                                - 2 * mBoxBorderSize
                                - (mergeRectBox ? (mOutBorderSize - mBoxBorderSize) * 2 : 0);
                        if (maxBoxHeight <= 0) {
                            mMeasureBoxHeight = 0;
                            height = maxHeight;
                        } else {
                            float expectedBoxHeight = mBoxHeight;
                            if (mBoxHeightRatio > 0) {
                                expectedBoxHeight = mMeasureBoxWidth * mBoxHeightRatio;
                            }
                            mMeasureBoxHeight = Math.min(maxBoxHeight, expectedBoxHeight);
                            height = getPaddingTop() + getPaddingBottom()
                                    + mMeasureBoxHeight
                                    + 2 * mBoxBorderSize
                                    + (mergeRectBox ? (mOutBorderSize - mBoxBorderSize) * 2 : 0);
                        }
                        break;
                    }
                    case BOX_MEASURE_MODE_FREE: {
                        float expectedBoxHeight = mBoxHeight;
                        if (mBoxHeightRatio > 0) {
                            expectedBoxHeight = mMeasureBoxWidth * mBoxHeightRatio;
                        }
                        mMeasureBoxHeight = expectedBoxHeight;

                        float expectedHeight = getPaddingTop() + getPaddingBottom()
                                + mMeasureBoxHeight
                                + 2 * mBoxBorderSize
                                + (mergeRectBox ? (mOutBorderSize - mBoxBorderSize) * 2 : 0);
                        height = Math.min(maxHeight, expectedHeight);

                        break;
                    }
                }
                break;
            }
        }

        mMeasureContentWidth = getPaddingLeft() + getPaddingRight()
                + mMeasureBoxWidth * mPasswordLength
                + mBoxBorderSize * 2 * mPasswordLength
                + (mergeRectBox ? (mOutBorderSize - mBoxBorderSize) * 2 - mBoxBorderSize * (mPasswordLength - 1) : 0)
                + mBoxMargin * (mPasswordLength - 1);

        mMeasureContentHeight = getPaddingTop() + getPaddingBottom()
                + mMeasureBoxHeight
                + 2 * mBoxBorderSize
                + (mergeRectBox ? (mOutBorderSize - mBoxBorderSize) * 2 : 0);

        setMeasuredDimension(Math.round(width), Math.round(height));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        canvas.save();
        float dx = (getMeasuredWidth() - mMeasureContentWidth) * 0.5f + getPaddingLeft();
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

        boolean mergeRectBox = mMergeRectBoxEnabled && mBoxMargin == 0;

        if (mergeRectBox) {
            mPaint.setColor(mBoxColor);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mOutBorderSize);

            float left = mOutBorderSize * 0.5f;
            float top = mOutBorderSize * 0.5f;
            float right = mMeasureContentWidth - (getPaddingLeft() + getPaddingRight()) - mOutBorderSize * 0.5f;
            float bottom = mMeasureContentHeight - (getPaddingTop() + getPaddingBottom()) - mOutBorderSize * 0.5f;
            mRectF.set(left, top, right, bottom);
            canvas.drawRoundRect(mRectF, mBoxCornerRadius, mBoxCornerRadius, mPaint);

            mPaint.setStrokeWidth(mBoxBorderSize);
            RectF lastBoxRectF = null;
            for (int index = 0; index < mPasswordLength; index++) {
                if (index == 0) {
                    left = mOutBorderSize;
                    top = mOutBorderSize;
                    right = left + mMeasureBoxWidth;
                    bottom = top + mMeasureBoxHeight;
                } else {
                    left = lastBoxRectF.right + mBoxBorderSize;
                    top = mOutBorderSize;
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
                if (index < mPasswordLength - 1) {
                    float startX = boxRectF.right + mBoxBorderSize * 0.5f;
                    float startY = boxRectF.top;
                    float stopX = startX;
                    float stopY = boxRectF.bottom;
                    canvas.drawLine(startX, startY, stopX, stopY, mPaint);
                }
            }

        } else {
            mPaint.setColor(mBoxColor);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mBoxBorderSize);

            for (int index = 0; index < mPasswordLength; index++) {

                float left = mMeasureBoxWidth * index + mBoxBorderSize * 2 * index + mBoxMargin * index + mBoxBorderSize * 0.5f;
                float top = mBoxBorderSize * 0.5f;
                float right = left + mMeasureBoxWidth + mBoxBorderSize;
                float bottom = top + mMeasureBoxHeight + mBoxBorderSize;

                mRectF.set(left, top, right, bottom);
                canvas.drawRoundRect(mRectF, mBoxCornerRadius, mBoxCornerRadius, mPaint);

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
            }
        }

        return mBoxRectFs;
    }

    private List<RectF> drawOvalBox(Canvas canvas) {
        mPaint.setColor(mBoxColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mBoxBorderSize);

        for (int index = 0; index < mPasswordLength; index++) {

            float left = mMeasureBoxWidth * index + mBoxBorderSize * 2 * index + mBoxMargin * index + mBoxBorderSize * 0.5f;
            float top = mBoxBorderSize * 0.5f;
            float right = left + mMeasureBoxWidth + mBoxBorderSize;
            float bottom = top + mMeasureBoxHeight + mBoxBorderSize;

            mRectF.set(left, top, right, bottom);

            canvas.drawOval(mRectF, mPaint);


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
        }

        return mBoxRectFs;
    }

    private List<RectF> drawUnderlineBox(Canvas canvas) {
        mPaint.setColor(mBoxColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(mBoxBorderSize);

        for (int index = 0; index < mPasswordLength; index++) {

            float startX = mMeasureBoxWidth * index + mBoxBorderSize * 2 * index + mBoxMargin * index;
            float startY = mMeasureBoxHeight + mBoxBorderSize * 1.5f;
            float stopX = startX + mMeasureBoxWidth + mBoxBorderSize * 2;
            float stopY = startY;

            canvas.drawLine(startX, startY, stopX, stopY, mPaint);

            RectF boxRect;
            if (mBoxRectFs.size() > index) {
                boxRect = mBoxRectFs.get(index);
            } else {
                boxRect = new RectF();
                mBoxRectFs.add(boxRect);
            }

            float left = mMeasureBoxWidth * index + mBoxBorderSize * 2 * index + mBoxMargin * index + mBoxBorderSize;
            float top = mBoxBorderSize;
            float right = left + mMeasureBoxWidth;
            float bottom = top + mMeasureBoxHeight;

            boxRect.set(left, top, right, bottom);
        }

        return mBoxRectFs;
    }

    private void drawCursor(Canvas canvas, List<RectF> boxRectFs) {
        if (!isFocused()) return;
        if (mDrawCursor) {
            mPaint.setColor(mCursorColor);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setStrokeWidth(mCursorWidth);

            Editable text = getText();
            int textLength = text == null ? 0 : text.length();
            if (textLength >= mPasswordLength) return;

            RectF rectF = boxRectFs.get(textLength);


            float startX = rectF.centerX() - mCursorWidth * 0.5f;
            float startY = rectF.top + mCursorMarginTop;
            float stopX = startX;
            float stopY = rectF.bottom - mCursorMarginBottom;

            canvas.drawLine(startX, startY, stopX, stopY, mPaint);
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

        mPaint.setTextSize(textSize);
        mPaint.setColor(textColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();

        for (int index = 0; index < textLength; index++) {
            RectF rectF = boxRectFs.get(index);
            switch (mPasswordType) {
                case PASSWORD_TYPE_STARS: {
                    float fontWidth = mPaint.measureText("*");
                    float baseX = rectF.centerX();
                    float baseY = (rectF.bottom + rectF.top - fontMetrics.bottom - fontMetrics.top) * 0.5f;
                    baseY += fontWidth * 0.26f;
                    canvas.drawText("*", baseX, baseY, mPaint);
                    break;
                }
                case PASSWORD_TYPE_CIRCLE: {
                    float cx = rectF.centerX();
                    float cy = rectF.centerY();
                    float radius = textSize * 0.5f;
                    canvas.drawCircle(cx, cy, radius, mPaint);
                    break;
                }
                case PASSWORD_TYPE_TEXT: {
                    char charAt = str.charAt(index);
                    String strAt = String.valueOf(charAt);
                    float baseX = rectF.centerX();
                    float baseY = (rectF.bottom + rectF.top - fontMetrics.bottom - fontMetrics.top) * 0.5f;
                    canvas.drawText(strAt, baseX, baseY, mPaint);
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
}