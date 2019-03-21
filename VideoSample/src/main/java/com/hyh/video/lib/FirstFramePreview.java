package com.hyh.video.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * @author Administrator
 * @description
 * @data 2019/2/25
 */

public class FirstFramePreview extends FrameLayout implements IVideoPreview {

    private final ImageView mPreviewImage;
    private final VideoPreviewHelper mVideoPreviewHelper;

    private ISurfaceMeasurer mSurfaceMeasurer;

    private DataSource mDataSource;
    private int mVideoWidth;
    private int mVideoHeight;


    public FirstFramePreview(Context context) {
        super(context);
        {
            mPreviewImage = new ImageView(context);
            FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(0, 0);
            mPreviewImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageParams.gravity = Gravity.CENTER;
            addView(mPreviewImage, imageParams);
        }
        mVideoPreviewHelper = new VideoPreviewHelper(new VideoPreviewHelper.PreviewAction() {
            @Override
            public void showPreview() {
                if (FirstFramePreview.this.getVisibility() == INVISIBLE || FirstFramePreview.this.getVisibility() == GONE) {
                    FirstFramePreview.this.setVisibility(VISIBLE);
                }
            }

            @Override
            public void hidePreview() {
                if (FirstFramePreview.this.getVisibility() == VISIBLE) {
                    FirstFramePreview.this.setVisibility(GONE);
                }
            }
        });
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setSurfaceMeasurer(ISurfaceMeasurer surfaceMeasurer) {
        this.mSurfaceMeasurer = surfaceMeasurer;
    }

    @Override
    public void setUp(VideoDelegate videoDelegate, IMediaInfo mediaInfo) {
        DataSource dataSource = videoDelegate.getDataSource();
        if (mDataSource != null && !mDataSource.equals(dataSource)) {
            mPreviewImage.setImageBitmap(null);
        }
        mDataSource = dataSource;
        this.setBackgroundColor(0xFFE8E8E8);
        requestFirstFrame(mediaInfo);
        mVideoPreviewHelper.setUp(videoDelegate);
    }

    private void requestFirstFrame(IMediaInfo mediaInfo) {
        mediaInfo.getFrameAtTime(0, new IMediaInfo.Result<Bitmap>() {
            @Override
            public void onResult(Bitmap bitmap) {
                if (bitmap != null) {
                    mVideoWidth = bitmap.getWidth();
                    mVideoHeight = bitmap.getHeight();
                    mPreviewImage.setImageBitmap(bitmap);
                    resetImageSize();
                    FirstFramePreview.this.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        resetImageSize();
    }

    private void resetImageSize() {
        if (mSurfaceMeasurer != null) {
            int width = getMeasuredWidth();
            int height = getMeasuredHeight();
            if (width != 0 && height != 0) {
                ViewGroup.LayoutParams layoutParams = mPreviewImage.getLayoutParams();
                int[] size = mSurfaceMeasurer.onMeasure(width, height, mVideoWidth, mVideoHeight);
                layoutParams.width = size[0];
                layoutParams.height = size[1];
                post(new Runnable() {
                    @Override
                    public void run() {
                        requestLayout();
                    }
                });
            }
        }
    }

    @Override
    public void onSurfaceCreate(Surface surface) {
        mVideoPreviewHelper.onSurfaceCreate();
    }

    @Override
    public void onSurfaceSizeChanged(Surface surface, int width, int height) {
        ViewGroup.LayoutParams layoutParams = mPreviewImage.getLayoutParams();
        boolean isSizeChanged = false;
        if (layoutParams.width != width) {
            layoutParams.width = width;
            isSizeChanged = true;
        }
        if (layoutParams.height != height) {
            layoutParams.height = height;
            isSizeChanged = true;
        }
        if (isSizeChanged) {
            mPreviewImage.requestLayout();
        }
    }

    @Override
    public void onSurfaceDestroyed(Surface surface) {
        mVideoPreviewHelper.onSurfaceDestroyed();
    }
}