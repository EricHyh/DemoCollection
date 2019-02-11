package com.hyh.video.sample;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.TextureView;

import com.hyh.video.lib.MediaSystem;

/**
 * @author Administrator
 * @description
 * @data 2019/2/11
 */

public class VideoActivity extends Activity {
    private MediaSystem mMediaSystem;
    private TextureView mTextureView;


    //http://kscdn.miaopai.com/stream/t~gB32Ha~0TyT3~Uju8bqQ___8.mp4?ssig=5e32f2ec5873a500ebeefd5d665c6486&time_stamp=1549879013833

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture);
        mTextureView = findViewById(R.id.TextureView);
        mMediaSystem = new MediaSystem("http://kscdn.miaopai.com/stream/t~gB32Ha~0TyT3~Uju8bqQ___8.mp4?ssig=5e32f2ec5873a500ebeefd5d665c6486&time_stamp=1549879013833");
        mMediaSystem.prepare(false);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                //mTextureView.setSurfaceTexture(surface);
                mMediaSystem.setSurface(new Surface(surface));
                mMediaSystem.start();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }
}
