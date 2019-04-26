package com.hyh.filedownloader.sample.image;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Movie;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import java.io.InputStream;

/**
 * @author Administrator
 * @description
 * @data 2019/4/15
 */

public class GifDrawable extends Drawable {

    private final Movie mMovie;

    public GifDrawable(InputStream inputStream) {
        mMovie = Movie.decodeStream(inputStream);
    }

    @Override
    public void draw(Canvas canvas) {
        mMovie.draw(canvas, 0, 0);
    }

    @Override
    public void setAlpha(int alpha) {
        //getCallback()
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        if (mMovie == null) return PixelFormat.UNKNOWN;
        return mMovie.isOpaque() ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT;
    }
}