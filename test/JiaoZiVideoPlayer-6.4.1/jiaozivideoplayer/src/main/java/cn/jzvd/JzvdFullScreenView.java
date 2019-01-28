package com.yly.mob.ssp.video;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.lang.reflect.Constructor;

/**
 * @author Administrator
 * @description
 * @data 2019/1/26
 */

public class JzvdFullScreenView implements IFullScreenView {

    private static boolean ACTION_BAR_EXIST = true;

    private static boolean TOOL_BAR_EXIST = true;

    private final int mFullScreenOrientation;

    private Jzvd mSmall;

    private Jzvd mFull;

    private long mLastBackPressTimeMillis;

    private ViewGroup mContentView;

    private boolean mIsShow;


    public JzvdFullScreenView() {
        this(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }

    public JzvdFullScreenView(int fullScreenOrientation) {
        this.mFullScreenOrientation = fullScreenOrientation;
    }

    @Override
    public void setUp(Jzvd small) {
        this.mSmall = small;
        try {
            Constructor<Jzvd> constructor = (Constructor<Jzvd>) small.getClass().getConstructor(Context.class);
            mFull = constructor.newInstance(small.getContext());
            mFull.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
            mFull.setUp(small.getJzDataSource(), JzvdStd.SCREEN_WINDOW_FULLSCREEN);
            mFull.setState(small.getCurrentState());
            mFull.progressBar.setSecondaryProgress(mSmall.getSecondaryProgress());
            mFull.mWindowController = mSmall.getWindowController();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isShow() {
        return mIsShow;
    }

    @Override
    public void show() {
        mIsShow = true;
        hideSupportActionBar(mFull.mWindowController);
        mFull.getWindowController().setRequestedOrientation(getFullScreenOrientation());

        mContentView = mFull.getWindowController().getDecorView().findViewById(Window.ID_ANDROID_CONTENT);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContentView.addView(mFull, lp);
        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        mFull.mTextureViewContainer.addView(mSmall.mTextureView, layoutParams);
        mFull.mTextureView = mSmall.mTextureView;

        JzvdMgr.setSecondFloor(mFull);
        mFull.startProgressTimer();
    }

    @Override
    public boolean close() {
        mIsShow = false;
        long currentTimeMillis = System.currentTimeMillis();
        if (Math.abs(currentTimeMillis - mLastBackPressTimeMillis) <= 300) return false;
        mLastBackPressTimeMillis = currentTimeMillis;


        mFull.onEvent(mFull.getCurrentState() == JzvdStd.SCREEN_WINDOW_FULLSCREEN ?
                JZUserAction.ON_QUIT_FULLSCREEN :
                JZUserAction.ON_QUIT_TINYSCREEN);

        mContentView.removeView(mFull);
        if (mFull.mTextureViewContainer != null && mFull.mTextureView != null) {
            mFull.mTextureViewContainer.removeView(mFull.mTextureView);
        }
        JzvdMgr.setSecondFloor(null);

        mFull.getWindowController().setRequestedOrientation(mSmall.getOrientation());
        showSupportActionBar(mFull.getWindowController());

        JzvdMgr.setSecondFloor(null);

        if (mSmall.getJzDataSource().containsTheUrl(JZMediaManager.getDataSource().getCurrentUrl())) {
            mSmall.onCloseFullScreen(mFull);
        } else {
            JZMediaManager.instance().releaseMediaPlayer();
            JzvdMgr.completeAll();
        }

        return true;
    }

    private int getFullScreenOrientation() {
        return mFullScreenOrientation;
    }

    @Override
    public void destroy() {
        if (mFull != null) {
            mContentView.removeView(mFull);
            if (mFull.mTextureViewContainer != null && mFull.mTextureView != null) {
                mFull.mTextureViewContainer.removeView(mFull.mTextureView);
            }
            mFull.getWindowController().setRequestedOrientation(mSmall.getOrientation());
            showSupportActionBar(mFull.getWindowController());
            mFull.onCompletion();
        }
    }


    private void showSupportActionBar(IWindowController windowController) {
        if (ACTION_BAR_EXIST) {
            windowController.showActionBar();
        }
        if (TOOL_BAR_EXIST) {
            windowController.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void hideSupportActionBar(IWindowController windowController) {
        if (ACTION_BAR_EXIST) {
            windowController.hideActionBar();
        }
        if (TOOL_BAR_EXIST) {
            windowController.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
}
