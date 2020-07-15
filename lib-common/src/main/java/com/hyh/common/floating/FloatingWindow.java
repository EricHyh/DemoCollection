package com.hyh.common.floating;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import java.lang.ref.WeakReference;


/**
 * @author Administrator
 * @description
 * @data 2017/7/24
 */
//http://blog.lixplor.com/2015/10/06/android-window-manager/

/**
 * systemUiVisibility:
 * 1. View.SYSTEM_UI_FLAG_VISIBLE：显示状态栏， Activity不全屏显示(恢复到有状态的正常情况)。
 * 2. View.INVISIBLE：隐藏状态栏，同时Activity会伸展全屏显示。
 * 3. View.SYSTEM_UI_FLAG_FULLSCREEN：Activity全屏显示，且状态栏被隐藏覆盖掉。
 * 4. View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN：Activity全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity顶端布局部分会被状态遮住。
 * 5. View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION：效果同View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
 * 6. View.SYSTEM_UI_LAYOUT_FLAGS：效果同View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
 * 7. View.SYSTEM_UI_FLAG_HIDE_NAVIGATION：隐藏虚拟按键(导航栏)。有些手机会用虚拟按键来代替物理按键。
 * 8. View.SYSTEM_UI_FLAG_LOW_PROFILE：状态栏显示处于低能显示状态(low profile模式)，状态栏上一些图标显示会被隐藏。
 */
public class FloatingWindow implements FloatingView.WindowEventListener {

    public static final int MATCH_PARENT = WindowManager.LayoutParams.MATCH_PARENT;
    public static final int WRAP_CONTENT = WindowManager.LayoutParams.WRAP_CONTENT;

    private Context mContext;

    private final WindowManager mWM;

    private final WindowManager.LayoutParams mParams;

    private final FloatingView mFloatingView;

    private View mWithView;

    private ViewOnAttachStateChangeListener mWithViewOnAttachStateChangeListener;

    private View mInnerView;

    private volatile boolean isAdded;

    private FloatingContentView mChildView;

    private Animation mShowAnimation;

    private Animation mHideAnimation;

    private boolean mIsDispatchKeyEvent;

    private WindowFocusChangedListener mWindowFocusChangedListener;

    private View.OnAttachStateChangeListener mAttachStateChangeListener;

    public void setWindowFocusChangedListener(WindowFocusChangedListener listener) {
        mWindowFocusChangedListener = listener;
    }

    public void setOnAttachStateChangeListener(View.OnAttachStateChangeListener listener) {
        mAttachStateChangeListener = listener;
    }

    public ViewGroup getRootView() {
        return mFloatingView;
    }

    WindowManager.LayoutParams getParams() {
        return mParams;
    }

    private FloatingWindow(Builder builder) {
        mContext = builder.context;
        mParams = builder.params;
        mIsDispatchKeyEvent = builder.dispatchKeyEvent;
        mWM = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        mFloatingView = new FloatingView(mContext);

        if (builder.background != null) {
            mFloatingView.setBackgroundDrawable(builder.background);
        } else {
            mFloatingView.setBackgroundColor(Color.TRANSPARENT);
        }
        mChildView = builder.childView;

        mShowAnimation = getShowAnimation(builder.showAnimation);
        mHideAnimation = getHideAnimation(builder.hideAnimation);

        mFloatingView.bindFloatWindow(this, this, builder.controller);

        this.mWithView = builder.withView;
        if (this.mWithView != null) {
            this.mWithViewOnAttachStateChangeListener = new ViewOnAttachStateChangeListener(this);
        }
    }

    public void setVisibility(int visibility) {
        mFloatingView.setVisibility(visibility);
    }

    public FloatingWindow interceptTouchEvent() {
        mParams.flags &=
                ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE & ~WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        return this;
    }

    public FloatingWindow requestFocus() {
        mParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        return this;
    }

    public FloatingWindow setSize(int width, int height) {
        mParams.width = width;
        mParams.height = height;
        return this;
    }

    public FloatingWindow setPosition(int x, int y) {
        mParams.x = x;
        mParams.y = y;
        return this;
    }

    public FloatingWindow hideStatusBar() {
        mParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mParams.systemUiVisibility |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        return this;
    }

    public FloatingWindow showStatusBar() {
        mParams.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN & ~WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                & ~WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        mParams.systemUiVisibility &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
        return this;
    }

    public FloatingWindow hideNavigationBar() {
        mParams.systemUiVisibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        return this;
    }

    public FloatingWindow showNavigationBar() {
        mParams.systemUiVisibility &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                & ~View.SYSTEM_UI_FLAG_IMMERSIVE & ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        return this;
    }

    public FloatingWindow forceNotFullscreen() {
        mParams.flags |= WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
        return this;
    }

    public FloatingWindow setAnimationStyle(int animationStyle) {
        mParams.windowAnimations = animationStyle;
        return this;
    }


    public void commitUpdate() {
        if (isAdded) {
            try {
                mWM.updateViewLayout(mFloatingView, mParams);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void show() {
        if (mWithView != null) {
            if (mWithView.getContext() == null || mWithView.getWindowToken() == null) {
                return;
            }
        }
        if (!isAdded) {
            mFloatingView.addView(mInnerView = mChildView.onCreateView(mContext, this));
            mWM.addView(mFloatingView, mParams);
            isAdded = true;
            if (mShowAnimation != null && mInnerView != null) {
                mInnerView.startAnimation(mShowAnimation);
            }
            final View withView = mWithView;
            final ViewOnAttachStateChangeListener withViewOnAttachStateChangeListener = mWithViewOnAttachStateChangeListener;
            if (withView != null && withViewOnAttachStateChangeListener != null) {
                withView.addOnAttachStateChangeListener(withViewOnAttachStateChangeListener);
            }
        }
    }

    public void dismiss() {
        dismiss(true);
    }

    public void dismiss(boolean anim) {
        if (isAdded) {
            isAdded = false;
            if (anim && mHideAnimation != null && mInnerView != null) {
                mInnerView.startAnimation(mHideAnimation);
            } else {
                onDismiss();
            }
        }
    }

    private void onDismiss() {
        if (mChildView != null) {
            mChildView.onDestroyView();
        }
        mFloatingView.setVisibility(View.GONE);
        mWM.removeView(mFloatingView);

        final View withView = mWithView;
        final ViewOnAttachStateChangeListener withViewOnAttachStateChangeListener = mWithViewOnAttachStateChangeListener;
        if (withView != null && withViewOnAttachStateChangeListener != null) {
            withView.removeOnAttachStateChangeListener(withViewOnAttachStateChangeListener);
        }
    }

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        if (mIsDispatchKeyEvent) {
            return false;
        }
        if (mChildView.onKeyEvent(event)) {
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            showStatusBar().commitUpdate();
            dismiss();
            return true;
        }
        return false;
    }

    @Override
    public void onAttachedToWindow(View view) {
        mChildView.onAttachedToWindow(view);
        if (mAttachStateChangeListener != null) {
            mAttachStateChangeListener.onViewAttachedToWindow(view);
        }
    }

    @Override
    public void onDetachedFromWindow(View view) {
        mChildView.onDetachedFromWindow(view);
        if (mAttachStateChangeListener != null) {
            mAttachStateChangeListener.onViewDetachedFromWindow(view);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (mWindowFocusChangedListener != null) {
            mWindowFocusChangedListener.onWindowFocusChanged(hasWindowFocus);
        }
        if (mChildView != null) {
            mChildView.onWindowFocusChanged(hasWindowFocus);
        }
    }

    private Animation getShowAnimation(AnimationStyle style) {
        Animation animation = null;
        switch (style) {
            case NONE:
                animation = null;
                break;
            case SCALE:
                animation = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_PARENT, 0.5f,
                        Animation.RELATIVE_TO_PARENT, 0.5f);
                break;
            case TRANSLATE:
                animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 1f, Animation.RELATIVE_TO_PARENT, 0f,
                        Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0f);
                break;
        }
        if (animation != null) {
            animation.setDuration(300);
        }
        return animation;
    }

    private Animation getHideAnimation(AnimationStyle style) {
        Animation animation = null;
        switch (style) {
            case NONE:
                animation = null;
                break;
            case SCALE:
                animation = new ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_PARENT, 0.5f,
                        Animation.RELATIVE_TO_PARENT, 0.5f);
                break;
            case TRANSLATE:
                animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 1f,
                        Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0f);
                break;
        }
        if (animation != null) {
            animation.setDuration(300);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    onDismiss();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
        return animation;
    }

    public boolean isAdded() {
        return isAdded;
    }

    public int[] getPosition() {
        return new int[]{mParams.x, mParams.y};
    }

    public WindowManager.LayoutParams getWindowManagerLayoutParams() {
        return mParams;
    }

    private static class ViewOnAttachStateChangeListener implements View.OnAttachStateChangeListener {

        private final WeakReference<FloatingWindow> mFloatingWindowRef;

        ViewOnAttachStateChangeListener(FloatingWindow floatingWindow) {
            this.mFloatingWindowRef = new WeakReference<>(floatingWindow);
        }

        @Override
        public void onViewAttachedToWindow(View v) {
        }

        @Override
        public void onViewDetachedFromWindow(View v) {
            FloatingWindow floatingWindow = mFloatingWindowRef.get();
            if (floatingWindow != null) {
                floatingWindow.dismiss(false);
            }
        }
    }

    public static class Builder {

        private Context context;

        private WindowManager.LayoutParams params;

        private Drawable background;

        private View withView;

        private boolean dispatchKeyEvent;

        private SlideController controller;

        private FloatingContentView childView;

        private AnimationStyle showAnimation = AnimationStyle.NONE;

        private AnimationStyle hideAnimation = AnimationStyle.NONE;

        public Builder(Context context, IBinder token, int windowType) {
            this.context = context;
            this.params = new WindowManager.LayoutParams();
            this.params.gravity = Gravity.LEFT | Gravity.TOP;
            params.format = PixelFormat.TRANSLUCENT;
            params.token = token;
            params.type = windowType;
            params.setTitle("FloatingWindow");
            params.flags |= WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }

        public Builder initialPosition(int x, int y) {
            params.x = x;
            params.y = y;
            return this;
        }

        public Builder size(int width, int height) {
            params.width = width;
            params.height = height;
            return this;
        }

        public Builder background(int color) {
            this.background = new ColorDrawable(color);
            return this;
        }

        public Builder background(Drawable drawable) {
            this.background = drawable;
            return this;
        }

        public Builder with(View view) {
            this.withView = view;
            return this;
        }

        public Builder view(int layout) {
            View view = LayoutInflater.from(context).inflate(layout, null);
            this.childView = new DefaultContentView(view);
            return this;
        }

        public Builder view(View view) {
            this.childView = new DefaultContentView(view);
            return this;
        }

        public Builder view(FloatingContentView listener) {
            this.childView = listener;
            return this;
        }

        public Builder keepScreenOn() {
            params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            return this;
        }

        public Builder hideStatusBar() {
            params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
            //params.systemUiVisibility = 2050;
            params.systemUiVisibility |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            return this;
        }

        public Builder hideNavigationBar() {
            params.systemUiVisibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            return this;
        }

        public Builder fullScreenWithStatusBar() {
            params.systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            showStatusBar();
            return this;
        }

        public Builder showStatusBar() {
            params.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN & ~WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    & ~WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

            params.systemUiVisibility &= ~View.SYSTEM_UI_FLAG_FULLSCREEN & ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | ~View.SYSTEM_UI_FLAG_IMMERSIVE & ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            return this;
        }

        public Builder forceNotFullscreen() {
            params.flags |= WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
            return this;
        }

        public Builder dispatchTouchEvent() {
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            return this;
        }

        public Builder refuseFocus() {
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            return this;
        }

        public Builder dispatchKeyEvent() {
            this.dispatchKeyEvent = true;
            return this;
        }

        public Builder slidingController(SlideController controller) {
            this.controller = controller;
            return this;
        }

        public Builder animation(AnimationStyle showAnimation, AnimationStyle hideAnimation) {
            this.showAnimation = showAnimation;
            this.hideAnimation = hideAnimation;
            return this;
        }

        public Builder windowAnimations(int windowAnimations) {
            params.windowAnimations = windowAnimations;
            return this;
        }

        public Builder gravity(int gravity) {
            params.gravity = gravity;
            return this;
        }

        public FloatingWindow build() {
            return new FloatingWindow(this);
        }

    }

    private static class DefaultContentView implements FloatingContentView {

        private View view;

        DefaultContentView(View view) {
            this.view = view;
        }

        @Override
        public View onCreateView(Context context, FloatingWindow floatingWindow) {
            return view;
        }

        @Override
        public void onAttachedToWindow(View view) {
        }

        @Override
        public void onDetachedFromWindow(View view) {
        }

        @Override
        public void onWindowFocusChanged(boolean hasWindowFocus) {
        }

        @Override
        public boolean onKeyEvent(KeyEvent event) {
            return false;
        }

        @Override
        public void onDestroyView() {
        }
    }

    public enum AnimationStyle {
        NONE, SCALE, TRANSLATE
    }
}
