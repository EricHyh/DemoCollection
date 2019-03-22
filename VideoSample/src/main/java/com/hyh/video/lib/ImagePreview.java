package com.hyh.video.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;

/**
 * @author Administrator
 * @description
 * @data 2019/2/25
 */

@SuppressLint("AppCompatCustomView")
public class ImagePreview extends ImageView implements IVideoPreview {

    private final VideoPreviewHelper mVideoPreviewHelper;

    private DataSource mDataSource;

    public ImagePreview(Context context) {
        super(context);
        setScaleType(ScaleType.CENTER_CROP);

        mVideoPreviewHelper = new VideoPreviewHelper(new VideoPreviewHelper.PreviewAction() {
            @Override
            public void showPreview() {
                if (ImagePreview.this.getVisibility() == INVISIBLE || ImagePreview.this.getVisibility() == GONE) {
                    ImagePreview.this.setVisibility(VISIBLE);
                }
            }

            @Override
            public void hidePreview() {
                if (ImagePreview.this.getVisibility() == VISIBLE) {
                    ImagePreview.this.setVisibility(GONE);
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
    }

    @Override
    public void setUp(VideoDelegate videoDelegate, IMediaInfo mediaInfo) {
        DataSource dataSource = videoDelegate.getDataSource();
        if (mDataSource != null && !mDataSource.equals(dataSource)) {
            setImageBitmap(null);
        }
        mDataSource = dataSource;
        mVideoPreviewHelper.setUp(videoDelegate);
    }

    @Override
    public void onSurfaceCreate(Surface surface) {
        mVideoPreviewHelper.onSurfaceCreate();
    }

    @Override
    public void onSurfaceSizeChanged(Surface surface, int width, int height) {
    }

    @Override
    public void onSurfaceDestroyed(Surface surface) {
        mVideoPreviewHelper.onSurfaceDestroyed();
    }
}