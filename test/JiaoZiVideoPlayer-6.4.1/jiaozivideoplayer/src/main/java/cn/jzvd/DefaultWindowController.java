package cn.jzvd;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * @author Administrator
 * @description
 * @data 2019/1/18
 */

public class DefaultWindowController implements IWindowController {

    private View mView;

    private ActWindowController mWindowController;

    public DefaultWindowController(View view) {
        this.mView = view;
        Activity activity = findActivity();
        if (activity != null) {
            mWindowController = new ActWindowController(activity);
        }
    }

    @Override
    public View getDecorView() {
        if (mWindowController != null) {
            return mWindowController.getDecorView();
        }
        return mView.getRootView();
    }

    @Override
    public void addFlags(int flags) {
        if (mWindowController != null) {
            mWindowController.addFlags(flags);
        }
    }

    @Override
    public void clearFlags(int flags) {
        if (mWindowController != null) {
            mWindowController.clearFlags(flags);
        }
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        if (mWindowController != null) {
            mWindowController.setRequestedOrientation(requestedOrientation);
        }
    }

    @Override
    public void setFlags(int flags, int mask) {
        if (mWindowController != null) {
            mWindowController.setFlags(flags, mask);
        }
    }

    @Override
    public WindowManager.LayoutParams getAttributes() {
        if (mWindowController != null) {
            return mWindowController.getAttributes();
        } else {
            ViewGroup.LayoutParams params = mView.getRootView().getLayoutParams();
            if (params == null) {
                return new WindowManager.LayoutParams();
            }
            if (params instanceof WindowManager.LayoutParams) {
                return (WindowManager.LayoutParams) params;
            } else {
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.width = params.width;
                layoutParams.height = params.height;
                return layoutParams;
            }
        }
    }

    @Override
    public void setAttributes(WindowManager.LayoutParams a) {
        if (mWindowController != null) {
            mWindowController.setAttributes(a);
        }
    }

    @Override
    public void showActionBar() {
        if (mWindowController != null) {
            mWindowController.showActionBar();
        }
    }

    @Override
    public void hideActionBar() {
        if (mWindowController != null) {
            mWindowController.hideActionBar();
        }
    }

    private Activity findActivity() {
        Context context = mView.getContext();
        if (context instanceof Activity) {
            return (Activity) context;
        }
        return null;
    }
}
