package com.hyh.fyp.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * @author Administrator
 * @description
 * @data 2019/1/16
 */

public class AnnulusRotateDrawable extends Drawable implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF annulusRectF = new RectF();

    private final ValueAnimator mValueAnimator;

    private float annulusSize = 10;

    private int annulusBackgroundColor;

    private int annulusForegroundColor;

    private float animStartRadian;

    private float animEndRadian;

    private volatile float animCurrentRadian;

    private int period;

    private float rotateSpeed;

    private float currentRotate;

    public AnnulusRotateDrawable(float animStartRadian, float animEndRadian, int duration) {
        this.animStartRadian = animStartRadian;
        this.animEndRadian = animEndRadian;
        this.mValueAnimator = ValueAnimator.ofFloat(animStartRadian, animEndRadian);

        mValueAnimator.addListener(this);
        mValueAnimator.addUpdateListener(this);
        mValueAnimator.setDuration(duration);
        mValueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    public void setAnnulusSize(float annulusSize) {
        this.annulusSize = annulusSize;
    }

    public void setAnnulusBackgroundColor(int color) {
        this.annulusBackgroundColor = color;
    }

    public void setAnnulusForegroundColor(int color) {
        this.annulusForegroundColor = color;
    }

    public void setRotateSpeed(float rotateSpeed) {
        this.rotateSpeed = rotateSpeed;
    }

    public void start() {
        mValueAnimator.start();
        invalidateSelf();
    }

    public void stop() {
        mValueAnimator.cancel();
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        int width = bounds.right - bounds.left;
        int height = bounds.bottom - bounds.top;
        drawAnnulusBackground(canvas, bounds);
        drawAnnulusForeground(canvas, bounds, width, height);
    }

    private void drawAnnulusBackground(Canvas canvas, Rect bounds) {
        mPaint.setColor(annulusBackgroundColor);
        mPaint.setStrokeWidth(annulusSize);
        mPaint.setStyle(Paint.Style.STROKE);
        annulusRectF.set(bounds.left + Math.round(annulusSize * 0.5f),
                bounds.top + Math.round(annulusSize * 0.5f),
                bounds.right - Math.round(annulusSize * 0.5f),
                bounds.bottom - Math.round(annulusSize * 0.5f));
        canvas.drawArc(annulusRectF, 0, 360, false, mPaint);
    }

    private void drawAnnulusForeground(Canvas canvas, Rect bounds, int width, int height) {
        canvas.save();
        canvas.rotate(currentRotate, Math.round(width * 0.5f), Math.round(height * 0.5f));

        mPaint.setColor(annulusForegroundColor);
        mPaint.setStrokeWidth(annulusSize);
        mPaint.setStyle(Paint.Style.STROKE);
        annulusRectF.set(bounds.left + Math.round(annulusSize * 0.5f),
                bounds.top + Math.round(annulusSize * 0.5f),
                bounds.right - Math.round(annulusSize * 0.5f),
                bounds.bottom - Math.round(annulusSize * 0.5f));

        canvas.drawArc(annulusRectF, calculateStartAngle(), calculateSweepAngle(), false, mPaint);

        canvas.restore();
    }

    private float calculateStartAngle() {
        if (period % 2 == 0) {
            return (animEndRadian - animStartRadian) * (period / 2);
        } else {
            return (animEndRadian - animStartRadian) * ((period - 1) / 2) + animEndRadian - animCurrentRadian;
        }
    }

    private float calculateSweepAngle() {
        if (period % 2 == 0) {
            return animCurrentRadian;
        } else {
            return animCurrentRadian;
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
        period++;
        period %= 72;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        animCurrentRadian = (float) animation.getAnimatedValue();
        currentRotate += rotateSpeed;
        currentRotate %= 360;
        invalidateSelf();
    }
}
