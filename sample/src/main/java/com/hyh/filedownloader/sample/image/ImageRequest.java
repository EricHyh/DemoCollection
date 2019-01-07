package com.hyh.filedownloader.sample.image;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

/**
 * @author Administrator
 * @description
 * @data 2019/1/7
 */

public class ImageRequest {

    private ImageLoader imageLoader;

    private Uri uri;

    private int placeholderResId;

    private Drawable placeholderDrawable;

    private int errorResId;

    private Drawable errorDrawable;

    public ImageRequest(ImageLoader imageLoader, Uri uri) {
        this.imageLoader = imageLoader;
        this.uri = uri;
    }

    public ImageRequest placeholder(int placeholderResId) {
        this.placeholderResId = placeholderResId;
        return this;
    }

    public ImageRequest placeholder(Drawable placeholderDrawable) {
        this.placeholderDrawable = placeholderDrawable;
        return this;
    }

    public ImageRequest error(int errorResId) {
        this.errorResId = errorResId;
        return this;
    }

    public ImageRequest error(Drawable errorDrawable) {
        this.errorDrawable = errorDrawable;
        return this;
    }

    public void into(ImageView imageView, Callback callback) {

    }

    public void into(Target target) {
        //Drawable.createFromStream()
    }
}