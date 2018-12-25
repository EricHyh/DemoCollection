package com.hyh.download.sample.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.yly.mob.ssp.downloadsample.R;


/**
 * @author Administrator
 * @description
 * @data 2017/6/30
 */

public class ProgressButton extends View {

    private ColorConfig mColorConfig;

    private int mForegroundColorForBg;

    private int mBackgroundColorForBg;

    private int mForegroundColorForText;

    private int mBackgroundColorForText;

    private float mTextSize;

    private float mCorner;

    private int mProgress;

    private int mMax;

    private CharSequence mText;

    private Paint mPaintForBg;
    private Paint mPaintForText;
    private int mWidth;
    private int mHeight;
    private RectF mRectF;
    private float mTextStartX;
    private float mTextBaseY;
    private float mTextWidth;
    private Path mPath;

    public ProgressButton(Context context) {
        this(context, null);
    }

    public ProgressButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton, defStyleAttr, 0);
            ColorStateList foregroundColorStateList = typedArray.getColorStateList(R.styleable.ProgressButton_foregroundColor);
            ColorStateList backgroundColorStateList = typedArray.getColorStateList(R.styleable.ProgressButton_backgroundColor);
            ColorStateList foregroundTextColorStateList = typedArray.getColorStateList(R.styleable.ProgressButton_foregroundTextColor);
            ColorStateList backgroundTextColorStateList = typedArray.getColorStateList(R.styleable.ProgressButton_backgroundTextColor);

            mColorConfig = new ColorConfig(foregroundColorStateList,
                    backgroundColorStateList,
                    foregroundTextColorStateList,
                    backgroundTextColorStateList);

            mMax = typedArray.getInteger(R.styleable.ProgressButton_max, 100);
            mProgress = typedArray.getInteger(R.styleable.ProgressButton_progress, 0);
            mTextSize = typedArray.getDimension(R.styleable.ProgressButton_textSize, getResources().getDimension(R.dimen.default_progress_button_text_size));
            mText = typedArray.getText(R.styleable.ProgressButton_text);
            mCorner = typedArray.getDimension(R.styleable.ProgressButton_corner, 0);
            typedArray.recycle();
        }


        mPaintForBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintForBg.setStyle(Paint.Style.FILL);
        mPaintForBg.setAntiAlias(true);
        mPaintForBg.setStrokeWidth(10);


        mPaintForText = new Paint();
        mPaintForText.setAntiAlias(true);
        mPaintForText.setTextSize(mTextSize);


        mPaintForText.setTypeface(Typeface.MONOSPACE);


        mRectF = new RectF();
        mPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: 2017/6/30 画背景
        int foregroundWidth = (foregroundWidth = (int) ((mProgress * mWidth * 1.0f) / mMax)) >= mWidth ? mWidth : foregroundWidth;

        final float corner;
        if (2 * mCorner > mHeight) {
            corner = mHeight / 2;
        } else if (mCorner < 0) {
            corner = 0;
        } else {
            corner = mCorner;
        }

        mPaintForBg.setColor(mForegroundColorForBg);
        if (foregroundWidth > 0 && foregroundWidth <= mWidth - corner) {
            float progressHeight = computeProgressHeightWithCorner(foregroundWidth, corner);
            if (progressHeight < corner) {
                drawProgressStartPart(canvas, foregroundWidth, progressHeight, corner);
            } else if (progressHeight < 2 * corner) {
                drawProgressStartPart(canvas, corner, corner, corner);
                mRectF.set(corner, 0, foregroundWidth, mHeight);
                canvas.drawRect(mRectF, mPaintForBg);
            } else {
                mRectF.set(0, 0, foregroundWidth, mHeight);
                mPaintForBg.setColor(mForegroundColorForBg);
                canvas.drawRoundRect(mRectF, corner, corner, mPaintForBg);
            }
        } else if (foregroundWidth > mWidth - corner && foregroundWidth <= mWidth) {
            drawProgressStartPart(canvas, corner, corner, corner);

            mRectF.set(corner, 0, mWidth - corner, mHeight);
            mPaintForBg.setColor(mForegroundColorForBg);
            canvas.drawRect(mRectF, mPaintForBg);

            drawProgressEndPart(canvas, foregroundWidth, corner);
        } else if (foregroundWidth > mWidth) {
            mRectF.set(0, 0, foregroundWidth, mHeight);
            mPaintForBg.setColor(mForegroundColorForBg);
            canvas.drawRoundRect(mRectF, corner, corner, mPaintForBg);
        }


        // TODO: 2017/6/30 画文字
        drawText(canvas, foregroundWidth);
    }

    private void drawProgressEndPart(Canvas canvas, int foregroundWidth, float corner) {
        float progressHeight = computeProgressHeightWithCorner(foregroundWidth, corner);
        float progressWidth = corner - mWidth + foregroundWidth;

        float left, top, right, bottom;
        float startAngle, sweepAngle;
        left = mWidth - 2 * corner;
        top = 0;
        right = left + 2 * corner;
        bottom = top + 2 * corner;
        startAngle = 270;
        sweepAngle = (float) (Math.asin(progressWidth / corner) / Math.PI * 180);
        mRectF.set(left, top, right, bottom + 1f);
        canvas.drawArc(mRectF, startAngle, sweepAngle, true, mPaintForBg);

        float surplus = mHeight - (corner * 2);

        left = mWidth - 2 * corner;
        top = surplus;
        right = left + 2 * corner;
        bottom = top + 2 * corner;
        startAngle = 90;
        sweepAngle = (float) (Math.asin(progressWidth / corner) / Math.PI * 180);
        mRectF.set(left, top, right, bottom + 1f);
        canvas.drawArc(mRectF, startAngle, -sweepAngle, true, mPaintForBg);

        mPath.reset();
        mPath.moveTo(mWidth - corner, corner - 1);
        mPath.lineTo(foregroundWidth, (mHeight - surplus) / 2 - progressHeight - 1);
        mPath.lineTo(foregroundWidth, corner + surplus + progressHeight + 2);
        mPath.lineTo(mWidth - corner, mHeight - corner + 2);
        mPath.close();
        canvas.drawPath(mPath, mPaintForBg);
    }

    private void drawText(Canvas canvas, int foregroundWidth) {
        if (TextUtils.isEmpty(mText)) {
            return;
        }

        mPaintForText.setColor(mForegroundColorForText);
        canvas.drawText(mText, 0, mText.length(), mTextStartX, mTextBaseY, mPaintForText);


        if (foregroundWidth <= mTextStartX) {
            mPaintForText.setColor(mBackgroundColorForText);
            canvas.drawText(mText, 0, mText.length(), mTextStartX, mTextBaseY, mPaintForText);
        } else if (foregroundWidth > mTextStartX + mTextWidth) {
            mPaintForText.setColor(mForegroundColorForText);
            canvas.drawText(mText, 0, mText.length(), mTextStartX, mTextBaseY, mPaintForText);
        } else {

            mPaintForText.setColor(mForegroundColorForText);
            canvas.drawText(mText, 0, mText.length(), mTextStartX, mTextBaseY, mPaintForText);
            canvas.clipRect(foregroundWidth, 0, mTextStartX + mTextWidth, mHeight, Region.Op.REPLACE);

            mPaintForText.setColor(mBackgroundColorForText);
            canvas.drawText(mText, 0, mText.length(), mTextStartX, mTextBaseY, mPaintForText);
            canvas.clipRect(foregroundWidth, 0, mTextStartX + mTextWidth, mHeight, Region.Op.REPLACE);
        }
    }

    private void drawProgressStartPart(Canvas canvas, float foregroundWidth, float progressHeight, float corner) {
        float left, top, right, bottom;
        left = 0;
        top = corner - progressHeight;
        right = 2 * foregroundWidth;
        bottom = top + 2 * progressHeight;

        mRectF.set(left, top, right, bottom + 1f);
        canvas.drawArc(mRectF, 180, 90, true, mPaintForBg);

        float surplus = (surplus = mHeight - (corner * 2)) < 0 ? 0 : surplus;
        left = 0;
        top = corner + surplus - progressHeight;
        right = 2 * foregroundWidth;
        bottom = top + 2 * progressHeight;
        mRectF.set(left, top, right, bottom);
        canvas.drawArc(mRectF, 180, -90, true, mPaintForBg);

        left = 0;
        top = corner;
        right = foregroundWidth;
        bottom = top + surplus;
        mRectF.set(left, top - 1f, right, bottom);
        canvas.drawRect(mRectF, mPaintForBg);
    }

    private float computeProgressHeightWithCorner(int foregroundWidth, float corner) {
        if (foregroundWidth >= corner && foregroundWidth <= mWidth - corner) {
            return corner;
        }
        if (foregroundWidth > mWidth - corner) {
            foregroundWidth = mWidth - foregroundWidth;
        }
        return (float) Math.sqrt(Math.pow(corner, 2) - Math.pow((corner - foregroundWidth), 2));
    }


    public void setProgress(int progress) {
        if (progress < 0) {
            progress = 0;
        } else if (progress > mMax) {
            progress = mMax;
        }
        mProgress = progress;
        postInvalidate();
    }

    public void setText(CharSequence text) {
        this.mText = text;
        computeTextStartX();
        invalidate();
    }


    public void setTextSize(float textSize) {
        mPaintForText.setTextSize(textSize);
        computeTextBaseY();
        invalidate();
    }


    private void computeTextStartX() {
        if (TextUtils.isEmpty(mText)) {
            return;
        }
        mTextWidth = mPaintForText.measureText(mText, 0, mText.length());
        mTextStartX = (mWidth - mTextWidth) / 2.0f;
    }

    private void computeTextBaseY() {
        Paint.FontMetrics fm = mPaintForText.getFontMetrics();
        mTextBaseY = mHeight / 2 - fm.descent + (fm.descent - fm.ascent) / 2;
    }


    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        int[] drawableState = getDrawableState();


        mForegroundColorForBg = mColorConfig.getForegroundColorForBg(drawableState);
        mBackgroundColorForBg = mColorConfig.getBackgroundColorForBg(drawableState);
        mForegroundColorForText = mColorConfig.getForegroundColorForText(drawableState);
        mBackgroundColorForText = mColorConfig.getBackgroundColorForText(drawableState);


        // 外部矩形弧度
        float[] outerR = new float[]{mCorner, mCorner, mCorner, mCorner, mCorner, mCorner, mCorner, mCorner};
        // 内部矩形与外部矩形的距离
        RectF inset = new RectF(100, 100, 50, 50);
        // 内部矩形弧度
        float[] innerRadii = new float[]{20, 20, 20, 20, 20, 20, 20, 20};

        RoundRectShape rr = new RoundRectShape(outerR, null, null);
        ShapeDrawable drawable = new ShapeDrawable(rr);
        //指定填充颜色
        drawable.getPaint().setColor(mBackgroundColorForBg);
        // 指定填充模式
        drawable.getPaint().setStyle(Paint.Style.FILL);

        setBackgroundDrawable(drawable);

        invalidate();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

        computeTextStartX();
        computeTextBaseY();
    }


    private static class ColorConfig {


        ColorStateList foregroundColorList_bg;

        ColorStateList backgroundColorList_bg;

        ColorStateList foregroundColorList_text;

        ColorStateList backgroundColorList_text;


        int defaultForegroundColorForBg;

        int defaultBackgroundColorForBg;

        int defaultForegroundColorForText;

        int defaultBackgroundColorForText;

        ColorConfig(ColorStateList foregroundColorList_bg,
                    ColorStateList backgroundColorList_bg,
                    ColorStateList foregroundColorList_text,
                    ColorStateList backgroundColorList_text) {
            this.foregroundColorList_bg = foregroundColorList_bg;
            this.backgroundColorList_bg = backgroundColorList_bg;
            this.foregroundColorList_text = foregroundColorList_text;
            this.backgroundColorList_text = backgroundColorList_text;

            defaultForegroundColorForBg = foregroundColorList_bg.getDefaultColor();
            defaultBackgroundColorForBg = backgroundColorList_bg.getDefaultColor();
            defaultForegroundColorForText = foregroundColorList_text.getDefaultColor();
            defaultBackgroundColorForText = backgroundColorList_text.getDefaultColor();
        }

        int getForegroundColorForBg(int[] drawableState) {
            return foregroundColorList_bg.getColorForState(drawableState, defaultForegroundColorForBg);
        }

        int getBackgroundColorForBg(int[] drawableState) {
            return backgroundColorList_bg.getColorForState(drawableState, defaultBackgroundColorForBg);
        }


        int getForegroundColorForText(int[] drawableState) {
            return foregroundColorList_text.getColorForState(drawableState, defaultForegroundColorForText);
        }

        int getBackgroundColorForText(int[] drawableState) {
            return backgroundColorList_text.getColorForState(drawableState, defaultBackgroundColorForText);
        }
    }
}
