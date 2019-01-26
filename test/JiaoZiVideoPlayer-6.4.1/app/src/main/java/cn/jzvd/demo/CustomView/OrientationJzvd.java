package cn.jzvd.demo.CustomView;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;

import cn.jzvd.IFullScreenView;
import cn.jzvd.JzvdFullScreenView;
import cn.jzvd.JzvdStd;

/**
 * Created by Eric_He on 2019/1/26.
 */

public class OrientationJzvd extends JzvdStd {


    public OrientationJzvd(Context context) {
        super(context);
    }

    public OrientationJzvd(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected IFullScreenView newFullScreenView() {
        return new JzvdFullScreenView(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
