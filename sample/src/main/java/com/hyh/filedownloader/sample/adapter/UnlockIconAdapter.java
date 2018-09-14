package com.hyh.filedownloader.sample.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.hyh.filedownloader.sample.utils.DisplayUtil;
import com.hyh.filedownloader.sample.widget.UnlockIconLayout;

import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2018/9/12
 */

public class UnlockIconAdapter extends UnlockIconLayout.Adapter {


    private List<Integer> mIconResourceIds;

    public UnlockIconAdapter(List<Integer> iconResourceIds) {
        mIconResourceIds = iconResourceIds;
    }

    @Override
    public int getIconCount() {
        return mIconResourceIds == null ? 0 : mIconResourceIds.size();
    }

    @Override
    public View onCreateIcon(ViewGroup parent, final int position) {
        final Context context = parent.getContext();
        ImageView imageView = new ImageView(parent.getContext());
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "position:" + position, Toast.LENGTH_SHORT).show();
            }
        });
        int _8dp = DisplayUtil.dip2px(context, 8);
        int _15dp = DisplayUtil.dip2px(context, 15);
        int _30dp = DisplayUtil.dip2px(context, 30);
        UnlockIconLayout.LayoutParams layoutParams = new UnlockIconLayout.LayoutParams(_30dp, _30dp);
        layoutParams.leftMargin = layoutParams.rightMargin = _15dp;
        layoutParams.topMargin = layoutParams.bottomMargin = _8dp;
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        return imageView;
    }

    @Override
    public void onBindIcon(View icon, int position) {
        ImageView imageView = (ImageView) icon;
        imageView.setImageResource(mIconResourceIds.get(position));
    }
}
