package com.hyh.video.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * @author Administrator
 * @description
 * @data 2019/2/25
 */

@SuppressLint("AppCompatCustomView")
public class ImagePreview extends ImageView implements IVideoPreview {

    protected final VideoPreviewHelper mVideoPreviewHelper;

    protected Drawable mDefaultDrawable;

    protected DataSource mDataSource;

    public ImagePreview(Context context) {
        this(context, new ColorDrawable(0xFFE8E8E8));
    }

    public ImagePreview(Context context, Drawable defaultDrawable) {
        super(context);
        setScaleType(ScaleType.CENTER_CROP);
        mDefaultDrawable = defaultDrawable;
        setImageDrawable(mDefaultDrawable);

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

    public void setDefaultDrawable(Drawable defaultDrawable) {
        mDefaultDrawable = defaultDrawable;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setSurfaceMeasurer(ISurfaceMeasurer surfaceMeasurer) {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            setLayoutParams(layoutParams);
        } else {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
    }

    @Override
    public void setUp(VideoDelegate videoDelegate, IMediaInfo mediaInfo) {
        DataSource dataSource = videoDelegate.getDataSource();
        if (mDataSource != null && !mDataSource.equals(dataSource)) {
            if (mDefaultDrawable != null) {
                setImageDrawable(mDefaultDrawable);
            } else {
                setImageBitmap(null);
            }
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