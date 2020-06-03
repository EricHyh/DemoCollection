package com.hyh.fyp.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

@SuppressLint("AppCompatCustomView")
public class PasswordView extends EditText implements TextWatcher {

    private int mPasswordLength = 6;

    private int mBoxColor = Color.RED;

    private float mBoxMargin = 10;

    private float mBoxRadius = 5;

    private float mBoxBorderSize = 6;

    private float mCursorWidth = 4;

    private float mCursorHeight;

    private RectF mRectF = new RectF();

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int mWidth;
    private float mBoxSize;

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
        if (attrs != null) {

        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        removeTextChangedListener(this);
        addTextChangedListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeTextChangedListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBox(canvas);
        drawCursor(canvas);
    }

    private void drawBox(Canvas canvas) {
        mPaint.setColor(mBoxColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mBoxBorderSize);
        for (int index = 0; index < mPasswordLength; index++) {
            float left = index * mBoxSize + index * mBoxMargin + mBoxBorderSize * 0.5f;
            float top = mBoxBorderSize * 0.5f;
            float right = left + mBoxSize - mBoxBorderSize;
            float bottom = top + mBoxSize - mBoxBorderSize;
            mRectF.set(left, top, right, bottom);
            canvas.drawRoundRect(mRectF, mBoxRadius, mBoxRadius, mPaint);
        }
    }

    private void drawCursor(Canvas canvas) {
        Editable text = getText();
        int textSize = text == null ? 0 : text.length();
        if (textSize == mPasswordLength) return;

        int cursorPosition = textSize;

        /*float startX = cursorPosition * mBoxSize + cursorPosition * mBoxMargin + mBoxBorderSize * 0.5f + mBoxSize * 0.5f;
        float startY,
        float stopX,
        float stopY

        canvas.drawLine();*/
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getMeasuredWidth();
        mBoxSize = (mWidth - (mPasswordLength - 1) * mBoxMargin) * 1.0f / mPasswordLength;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}