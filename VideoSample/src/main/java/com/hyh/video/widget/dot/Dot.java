package com.hyh.video.widget.dot;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;

/**
 * 作者： zhengge
 * 创建日期：2018/4/27
 * 功能描述：做一个渐变动画
 */
public class Dot extends Drawable implements Animatable, ValueAnimator.AnimatorUpdateListener {

    private Paint mPaint;
    private int mUseColor;
    private int mBaseColor;
    private ValueAnimator valueAnimator;

    public int getAnimationDelay() {
        return animationDelay;
    }

    public void setAnimationDelay(int animationDelay) {
        this.animationDelay = animationDelay;
    }

    private int animationDelay;

    public Rect getDrawBounds() {
        return drawBounds;
    }

    private Rect drawBounds = new Rect();


    private int alpha = 255;

    public Dot(int color) {
        setColor(color);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mUseColor);
    }

    public void setColor(int color) {
        mBaseColor = color;
        updateUseColor();
    }

    private void updateUseColor() {
        int alpha = getAlpha();
        alpha += alpha >> 7;
        final int baseAlpha = mBaseColor >>> 24;
        final int useAlpha = baseAlpha * alpha >> 8;
        mUseColor = (mBaseColor << 8 >>> 8) | (useAlpha << 24);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        drawBounds.set(bounds);
        if (isRunning()) stop();
        final float fractions[] = new float[]{0f, 0.39f, 0.4f, 1f};
        PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofInt("alpha", 0, 0, 255, 0);
        valueAnimator = ObjectAnimator.ofPropertyValuesHolder(this, alphaHolder);
        valueAnimator.setDuration(1200);
        valueAnimator.addUpdateListener(this);
        valueAnimator.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float input) {
                for (int i = 0; i < fractions.length - 1; i++) {
                    float start = fractions[i];
                    float end = fractions[i + 1];
                    float duration = end - start;
                    if (input >= start && input <= end) {
                        input = (input - start) / duration;
                        return start + input * duration;
                    }
                }
                return 0;
            }
        });
        valueAnimator.setStartDelay(getAnimationDelay());
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);

        start();

    }


    @Override
    public void start() {
        valueAnimator.start();
    }

    @Override
    public void stop() {
        valueAnimator.removeAllUpdateListeners();
        valueAnimator.end();
    }

    @Override
    public boolean isRunning() {
        return valueAnimator != null && valueAnimator.isRunning();
    }

    @Override
    public void draw(Canvas canvas) {
        if (getDrawBounds() != null) {
            mPaint.setAlpha(alpha);
            int radius = Math.min(getDrawBounds().width(), getDrawBounds().height()) / 2;
            canvas.drawCircle(getDrawBounds().centerX(),
                    getDrawBounds().centerY(),
                    radius, mPaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    @Override
    public int getAlpha() {
        return alpha;
    }


    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @SuppressLint("WrongConstant")
    @Override
    public int getOpacity() {
        return PixelFormat.RGBA_8888;
    }


    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        invalidateSelf();
    }
}
