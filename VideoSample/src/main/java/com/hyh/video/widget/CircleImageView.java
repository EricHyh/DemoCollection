package com.hyh.video.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * @author Administrator
 * @description
 * @data 2019/10/9
 */

public class CircleImageView extends AppCompatImageView {

    private Path mPath;

    private int mWidth;
    private int mHeight;
    private int mRadius;

    public CircleImageView(Context context) {
        super(context);
        init();
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        mPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        mPath.reset();
        mPath.addCircle(mWidth / 2, mHeight / 2, mRadius, Path.Direction.CCW);
        canvas.clipPath(mPath);
        super.onDraw(canvas);
        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mRadius = Math.min(mWidth, mHeight) / 2;
    }
}