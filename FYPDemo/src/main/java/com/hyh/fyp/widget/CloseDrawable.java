package com.hyh.fyp.widget;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * @author Administrator
 * @description
 * @data 2019/1/4
 */

public class CloseDrawable extends Drawable {

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CloseDrawable(int color, float lineWidth) {
        mPaint.setColor(color);
        mPaint.setStrokeWidth(lineWidth);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        {
            float startX = bounds.left;
            float startY = bounds.top;
            float stopX = bounds.right;
            float stopY = bounds.bottom;
            canvas.drawLine(startX, startY, stopX, stopY, mPaint);
        }
        {
            float startX = bounds.left;
            float startY = bounds.bottom;
            float stopX = bounds.right;
            float stopY = bounds.top;
            canvas.drawLine(startX, startY, stopX, stopY, mPaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
