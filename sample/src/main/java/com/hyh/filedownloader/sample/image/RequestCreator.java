package com.hyh.filedownloader.sample.image;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

/**
 * @author Administrator
 * @description
 * @data 2019/1/7
 */

public class RequestCreator {

    private HappyImage happyImage;

    private Uri uri;

    private int placeholderResId;

    private Drawable placeholderDrawable;

    private int errorResId;

    private Drawable errorDrawable;

    private int[] size;

    public RequestCreator(HappyImage happyImage, Uri uri) {
        this.happyImage = happyImage;
        this.uri = uri;
    }

    public RequestCreator placeholder(int placeholderResId) {
        this.placeholderResId = placeholderResId;
        return this;
    }

    public RequestCreator placeholder(Drawable placeholderDrawable) {
        this.placeholderDrawable = placeholderDrawable;
        return this;
    }

    public RequestCreator error(int errorResId) {
        this.errorResId = errorResId;
        return this;
    }

    public RequestCreator error(Drawable errorDrawable) {
        this.errorDrawable = errorDrawable;
        return this;
    }

    public void into(ImageView imageView, Callback callback) {
        int width = imageView.getMeasuredWidth();
    }

    public void into(Target target) {

    }


    private String createKey() {
        StringBuilder sb = new StringBuilder();
        sb.append("path:").append(uri.toString());
        return sb.toString();
    }
}