package com.hyh.web.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hyh.web.utils.DisplayUtil;


/**
 * @author Administrator
 * @description
 * @data 2019/8/28
 */

public class ViewMoreDrawable extends Drawable {

    private final String mText = "点击查看全文";
    private final Paint mBackgroundPaint;
    private final Paint mTextPaint;
    private final Paint mArrowPaint;
    private final Path mFirstArrowPath;
    private final Path mSecondArrowPath;

    private final float ARROW_OFFSET_Y_START;
    private final float ARROW_OFFSET_Y_END;
    private float mCurrentArrowOffsetY;
    private float mArrowOffsetDy = 0.8f;
    private final Rect mContentBounds;

    public ViewMoreDrawable(Context context, Rect bounds) {
        setBounds(bounds);
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int[] colors = {0x99FFFFF, 0xFFFFFFFF, 0xFFFFFFFF};
        float[] positions = {0.0f, 0.2f, 1.0f};
        mBackgroundPaint.setShader(new LinearGradient(0, bounds.top, 0, bounds.bottom, colors, positions, Shader.TileMode.CLAMP));

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(0xFF346AEB);

        int textSize = DisplayUtil.dip2px(context, 17);
        mTextPaint.setTextSize(textSize);

        mTextPaint.setTextAlign(Paint.Align.CENTER);

        ARROW_OFFSET_Y_START = -DisplayUtil.dip2px(context, 6);
        ARROW_OFFSET_Y_END = -ARROW_OFFSET_Y_START;
        mCurrentArrowOffsetY = ARROW_OFFSET_Y_START;

        mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPaint.setColor(0xFF4679EE);
        mArrowPaint.setStrokeWidth(DisplayUtil.dip2px(context, 1.5f));
        mArrowPaint.setStyle(Paint.Style.STROKE);


        int arrowWidth = DisplayUtil.dip2px(context, 18);
        int arrowHeight = DisplayUtil.dip2px(context, 9);
        int arrowMargin = DisplayUtil.dip2px(context, 2);

        int arrowMarginVerticalCenter = Math.round((bounds.bottom - bounds.top) / 4.0f) - DisplayUtil.dip2px(context, 10);

        {
            mFirstArrowPath = new Path();
            float pathX = bounds.left + (bounds.right - bounds.left) / 2.0f - arrowWidth / 2.0f;
            float pathY = bounds.top + (bounds.bottom - bounds.top) / 2.0f + arrowMarginVerticalCenter;
            mFirstArrowPath.moveTo(pathX, pathY);
            pathX = bounds.left + (bounds.right - bounds.left) / 2.0f;
            pathY = bounds.top + (bounds.bottom - bounds.top) / 2.0f + arrowHeight + arrowMarginVerticalCenter;
            mFirstArrowPath.lineTo(pathX, pathY);
            pathX = bounds.left + (bounds.right - bounds.left) / 2.0f + arrowWidth / 2.0f;
            pathY = bounds.top + (bounds.bottom - bounds.top) / 2.0f + arrowMarginVerticalCenter;
            mFirstArrowPath.lineTo(pathX, pathY);
        }
        {
            mSecondArrowPath = new Path();
            float pathX = bounds.left + (bounds.right - bounds.left) / 2.0f - arrowWidth / 2.0f;
            float pathY = bounds.top + (bounds.bottom - bounds.top) / 2.0f + arrowMarginVerticalCenter + arrowHeight + arrowMargin;
            mSecondArrowPath.moveTo(pathX, pathY);
            pathX = bounds.left + (bounds.right - bounds.left) / 2.0f;
            pathY = bounds.top + (bounds.bottom - bounds.top) / 2.0f + arrowHeight + arrowMarginVerticalCenter + arrowHeight + arrowMargin;
            mSecondArrowPath.lineTo(pathX, pathY);
            pathX = bounds.left + (bounds.right - bounds.left) / 2.0f + arrowWidth / 2.0f;
            pathY = bounds.top + (bounds.bottom - bounds.top) / 2.0f + arrowMarginVerticalCenter + arrowHeight + arrowMargin;
            mSecondArrowPath.lineTo(pathX, pathY);
        }

        {
            float textLength = mTextPaint.measureText(mText);
            mContentBounds = new Rect();
            mContentBounds.left = Math.round(bounds.left + (bounds.right - bounds.left) / 2.0f - textLength / 2.0f);
            mContentBounds.top = Math.round(bounds.top + (bounds.bottom - bounds.top) / 2.0f - textSize);
            mContentBounds.right = Math.round(bounds.left + (bounds.right - bounds.left) / 2.0f + textLength / 2.0f);
            mContentBounds.bottom = Math.round(bounds.top + (bounds.bottom - bounds.top) / 2.0f + arrowMarginVerticalCenter + arrowMargin + arrowHeight * 2);
        }
    }

    public Rect getContentBounds() {
        return mContentBounds;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        canvas.drawRect(bounds, mBackgroundPaint);

        float x = bounds.left + (bounds.right - bounds.left) / 2.0f;
        float y = bounds.top + (bounds.bottom - bounds.top) / 2.0f;
        canvas.drawText(mText, x, y, mTextPaint);

        canvas.save();
        canvas.translate(0, mCurrentArrowOffsetY);
        canvas.drawPath(mFirstArrowPath, mArrowPaint);
        canvas.drawPath(mSecondArrowPath, mArrowPaint);
        mCurrentArrowOffsetY += mArrowOffsetDy;
        if (mCurrentArrowOffsetY > ARROW_OFFSET_Y_END) {
            mCurrentArrowOffsetY = ARROW_OFFSET_Y_END;
            mArrowOffsetDy = -mArrowOffsetDy;
        } else if (mCurrentArrowOffsetY < ARROW_OFFSET_Y_START) {
            mCurrentArrowOffsetY = ARROW_OFFSET_Y_START;
            mArrowOffsetDy = -mArrowOffsetDy;
        }
        invalidateSelf();
        canvas.restore();
    }


    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }
}