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
 * @data 2019/1/15
 */

public class OptionDrawable extends Drawable {

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    {
        mPaint.setStyle(Paint.Style.FILL);
    }

    private final float mDotRadius;

    public OptionDrawable(int color, float dotRadius) {
        mPaint.setColor(color);
        this.mDotRadius = dotRadius;
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        int width = bounds.right - bounds.left;
        int height = bounds.bottom - bounds.top;
        float dotSpace = Math.round((height - 6 * mDotRadius) * 0.5f);
        float cx = bounds.left + Math.round((width) * 0.5f);
        for (int index = 0; index < 3; index++) {
            float cy = mDotRadius + dotSpace * (index) + mDotRadius * 2 * index;
            canvas.drawCircle(cx, cy, mDotRadius, mPaint);
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
