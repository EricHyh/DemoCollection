package cn.jzvd;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

/**
 * @author Administrator
 * @description
 * @data 2019/1/18
 */

public class ActWindowController implements IWindowController {

    private Activity mActivity;

    public ActWindowController(Activity activity) {
        mActivity = activity;
    }

    @Override
    public View getDecorView() {
        return mActivity.getWindow().getDecorView();
    }

    @Override
    public void addFlags(int flags) {
        mActivity.getWindow().addFlags(flags);
    }

    @Override
    public void clearFlags(int flags) {
        mActivity.getWindow().clearFlags(flags);
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        mActivity.setRequestedOrientation(requestedOrientation);
    }

    @Override
    public void setFlags(int flags, int mask) {
        mActivity.getWindow().setFlags(flags, mask);
    }

    @Override
    public WindowManager.LayoutParams getAttributes() {
        return mActivity.getWindow().getAttributes();
    }

    @Override
    public void setAttributes(WindowManager.LayoutParams a) {
        mActivity.getWindow().setAttributes(a);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void showActionBar() {
        if (mActivity instanceof AppCompatActivity) {
            AppCompatActivity appCompatActivity = (AppCompatActivity) mActivity;
            android.support.v7.app.ActionBar actionBar = appCompatActivity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setShowHideAnimationEnabled(false);
                actionBar.show();
            }
        } else {
            ActionBar actionBar = mActivity.getActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void hideActionBar() {
        if (mActivity instanceof AppCompatActivity) {
            AppCompatActivity appCompatActivity = (AppCompatActivity) mActivity;
            android.support.v7.app.ActionBar actionBar = appCompatActivity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setShowHideAnimationEnabled(false);
                actionBar.hide();
            }
        } else {
            ActionBar actionBar = mActivity.getActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
        }
    }
}
