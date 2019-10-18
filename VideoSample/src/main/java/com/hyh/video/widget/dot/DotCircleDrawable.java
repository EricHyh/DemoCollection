package com.hyh.video.widget.dot;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;

/**
 * 作者： zhengge
 * 创建日期：2018/4/27
 * 功能描述：
 * 1. 旋转的小点点
 * 2. 下面添加文字
 */
public class DotCircleDrawable extends Drawable implements Animatable, Drawable.Callback {

    private int mBaseColor;
    private Dot[] mDots;
    private Paint mPaint = new Paint();

    public DotCircleDrawable(int color) {
        this.mBaseColor = color;
        mDots = new Dot[12];
        for (int i = 0; i < mDots.length; i++) {
            mDots[i] = new Dot(mBaseColor);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mDots[i].setAnimationDelay(1200 / 12 * i);
            } else {
                mDots[i].setAnimationDelay(1200 / 12 * i + -1200);
            }
            mDots[i].setCallback(this);
        }

        initTextPaint();
    }

    private void initTextPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setColor(mBaseColor);
        mPaint.setTextSize(30);
    }

    /**
     * 1. 圆圈占 2/3
     * 2. 文字占 1/3
     */
    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        bounds = clipSquare(bounds);
        int radius = (int) (bounds.width() * Math.PI / 3.6f / getChildCount());
        int left = bounds.centerX() - radius;
        int right = bounds.centerX() + radius;
        for (int i = 0; i < getChildCount(); i++) {
            Dot dot = mDots[i];
            // 每个点的大小是固定的 所以 radius 是固定的
            dot.onBoundsChange(new Rect(left, bounds.top, right, bounds.top + radius * 2));
        }
    }

    public int getChildCount() {
        return mDots == null ? 0 : mDots.length;
    }

    @Override
    public void start() {
        for (Dot dot : mDots) {
            dot.start();
        }
    }

    @Override
    public void stop() {
        for (Dot dot : mDots) {
            dot.stop();
        }
    }

    @Override
    public boolean isRunning() {
        for (Dot dot : mDots) {
            if (dot.isRunning()) return true;
        }
        return false;
    }

    float ratio = 0.5f;

    @Override
    public void draw(Canvas canvas) {
        for (int i = 0; i < getChildCount(); i++) {
            Dot dot = mDots[i];
            int count = canvas.save();
            // 需要对每个点进行旋转
            canvas.rotate(i * 360 / getChildCount(),
                    getBounds().centerX(),
                    getBounds().centerY() * ratio);
            dot.draw(canvas);
            canvas.restoreToCount(count);
        }

        drawHint(canvas);
    }

    /**
     * @param canvas 绘制文本信息
     */
    private void drawHint(Canvas canvas) {
        String str = "资源更新中";
        mPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(str, 0, 5, getBounds().centerX(), getBounds().centerY() * 1.5f, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @SuppressLint("WrongConstant")
    @Override
    public int getOpacity() {
        return PixelFormat.RGBA_8888;
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        invalidateSelf();
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {

    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {

    }

    public Rect clipSquare(Rect rect) {
        int w = rect.width();
        int h = (int) (rect.height() * ratio + 0.5f);
//		int h = rect.height();
        int min = Math.min(w, h);
        int cx = rect.centerX();
        int cy = (int) (rect.centerY() * ratio + 0.5f);
//		int cy = rect.centerY();
        int r = min / 2;
        return new Rect(
                cx - r,
                cy - r,
                cx + r,
                cy + r
        );
    }
}