package com.hyh.video.lib;

import android.graphics.Bitmap;

/**
 * @author Administrator
 * @description
 * @data 2019/3/8
 */

public interface IMediaInfo {

    void setup(DataSource source);

    void getDuration(Result<Long> result);

    void getVideoSize(Result<int[]> result);

    void getFrameAtTime(long timeUs, Result<Bitmap> result);

    interface Result<T> {
        void onResult(T t);
    }
}