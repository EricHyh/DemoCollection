package cn.jzvd;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import java.lang.reflect.Constructor;

import static cn.jzvd.Jzvd.FULLSCREEN_ORIENTATION;

/**
 * @author Administrator
 * @description
 * @data 2019/1/26
 */

public class JzvdFullScreenView implements IFullScreenView {

    private Jzvd mSmall;

    private Jzvd mFull;

    private long mLastBackPressTimeMillis;


    @Override
    public void setUp(Jzvd small) {
        this.mSmall = small;
        try {
            ViewGroup contentView = small.mWindowController.getDecorView().findViewById(Window.ID_ANDROID_CONTENT);

            Constructor<Jzvd> constructor = (Constructor<Jzvd>) small.getClass().getConstructor(Context.class);
            mFull = constructor.newInstance(small.getContext());
            mFull.setId(R.id.jz_fullscreen_id);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            contentView.addView(mFull, lp);
            mFull.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
            mFull.setUp(small.getJzDataSource(), JzvdStd.SCREEN_WINDOW_FULLSCREEN);
            mFull.setState(small.getCurrentState());
            mFull.mWindowController = mSmall.mWindowController;
            mFull.progressBar.setSecondaryProgress(mSmall.getProgress());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        mFull.addTextureView();
        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        mFull.mTextureViewContainer.addView(mSmall.mTextureView, layoutParams);

        JzvdMgr.setSecondFloor(mFull);
        mFull.getWindowController().setRequestedOrientation(FULLSCREEN_ORIENTATION);
        mFull.startProgressTimer();
    }

    @Override
    public boolean onBackPress() {
        long currentTimeMillis = System.currentTimeMillis();
        if (Math.abs(currentTimeMillis - mLastBackPressTimeMillis) <= 300) return false;
        mLastBackPressTimeMillis = currentTimeMillis;

        if (mSmall.getJzDataSource().containsTheUrl(JZMediaManager.getDataSource().getCurrentUrl())) {
            mFull.onEvent(mFull.getCurrentState() == JzvdStd.SCREEN_WINDOW_FULLSCREEN ?
                    JZUserAction.ON_QUIT_FULLSCREEN :
                    JZUserAction.ON_QUIT_TINYSCREEN);

            mSmall.mWindowController.setRequestedOrientation(Jzvd.NORMAL_ORIENTATION);


            ViewGroup vp = mWindowController.getDecorView().findViewById(Window.ID_ANDROID_CONTENT);
            if (vp == null) {
                return;
            }

            Jzvd fullJzvd = vp.findViewById(R.id.jz_fullscreen_id);
            Jzvd tinyJzvd = vp.findViewById(R.id.jz_tiny_id);


            if (fullJzvd != null) {
                vp.removeView(fullJzvd);
                if (fullJzvd.mTextureViewContainer != null)
                    fullJzvd.mTextureViewContainer.removeView(JZMediaManager.textureView);
            }
            if (tinyJzvd != null) {
                vp.removeView(tinyJzvd);
                if (tinyJzvd.mTextureViewContainer != null)
                    tinyJzvd.mTextureViewContainer.removeView(JZMediaManager.textureView);
            }
            JzvdMgr.setSecondFloor(null);


            mSmall.playOnThisJzvd();


        } else {
            mSmall.clearFloatScreen();
            JZMediaManager.instance().releaseMediaPlayer();
            JzvdMgr.completeAll();
        }
        return true;

    }
}
