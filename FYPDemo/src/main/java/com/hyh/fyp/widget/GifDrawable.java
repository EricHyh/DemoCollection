package com.hyh.fyp.widget;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.Log;

import java.io.InputStream;

/**
 * @author Administrator
 * @description
 * @data 2020/7/21
 */
public class GifDrawable extends Drawable {

    private static final String TAG = "GifDrawable";

    public static Drawable createFromPath(String pathName) {
        return new GifDrawable(Movie.decodeFile(pathName));
    }

    public static Drawable createFromStream(InputStream is, String srcName) {
        return new GifDrawable(Movie.decodeStream(is));
    }

    private final Movie movie;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private long startTimeMillis;

    private int relativeMilliseconds;

    private boolean start;

    private GifDrawable(Movie movie) {
        this.movie = movie;
        int width = movie.width();
        int height = movie.height();
        setBounds(0, 0, width, height);
    }

    @Override
    public void draw(Canvas canvas) {
        if (!start) {
            movie.setTime(relativeMilliseconds);
            movie.draw(canvas, 0, 0, paint);
            return;
        }
        long timeMillis = SystemClock.uptimeMillis();
        relativeMilliseconds += (timeMillis - startTimeMillis);
        startTimeMillis = timeMillis;
        relativeMilliseconds %= movie.duration();
        Log.d(TAG, "draw: " + relativeMilliseconds);
        movie.setTime(relativeMilliseconds);
        movie.draw(canvas, 0, 0, paint);
        invalidateSelf();
    }

    public void start() {
        this.start = true;
        this.startTimeMillis = SystemClock.uptimeMillis();
        invalidateSelf();
    }

    public void stop() {
        this.start = false;
    }

    @Override
    public int getIntrinsicWidth() {
        return movie.width();
    }

    @Override
    public int getIntrinsicHeight() {
        return movie.height();
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }
}