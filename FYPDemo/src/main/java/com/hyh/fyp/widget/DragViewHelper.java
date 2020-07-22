package com.hyh.fyp.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;

import com.hyh.common.utils.ViewUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

/**
 * @author Administrator
 * @description
 * @data 2020/7/21
 */
public class DragViewHelper implements View.OnTouchListener, View.OnAttachStateChangeListener, ViewTreeObserver.OnGlobalLayoutListener {


    public static final int IDLE_MODE_NONE = 0;
    public static final int IDLE_MODE_LEFT_SIDE = 1;
    public static final int IDLE_MODE_RIGHT_SIDE = 2;
    public static final int IDLE_MODE_BOTH_SIDE = 3;

    @IntDef({IDLE_MODE_NONE, IDLE_MODE_LEFT_SIDE, IDLE_MODE_RIGHT_SIDE, IDLE_MODE_BOTH_SIDE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface IdleMode {
    }

    private final WeakReference<View> viewRef;
    private final InitialPosition mInitialPosition;
    private final int mIdleMode;
    private final boolean mHalfHidden;
    private final float mLeftRatio;
    private final int mTouchSlop;
    private final HalfHiddenTask mHalfHiddenTask = new HalfHiddenTask();

    private float mInitialTouchX;
    private float mInitialTouchY;

    private float mLastTranslationX;
    private float mLastTranslationY;

    private boolean mInDragging;

    private float mMinTranslationX;
    private float mMaxTranslationX;
    private float mMinTranslationY;
    private float mMaxTranslationY;

    private boolean mPositionInitialized;

    private boolean mAllowHandleTouch;

    private ObjectAnimator mIdleAnim;


    private DragViewHelper(Builder builder) {
        this.viewRef = new WeakReference<>(builder.view);
        this.mInitialPosition = builder.initialPosition;
        this.mIdleMode = builder.idleMode;
        this.mHalfHidden = builder.halfHidden;
        this.mLeftRatio = builder.leftRatio;
        this.mTouchSlop = ViewConfiguration.get(builder.view.getContext()).getScaledTouchSlop();

        builder.view.setOnTouchListener(this);
        builder.view.addOnAttachStateChangeListener(this);
        if (ViewUtil.isAttachedToWindow(builder.view)) {
            onViewAttachedToWindow(builder.view);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            mHalfHiddenTask.cancel();
            ObjectAnimator idleAnim = mIdleAnim;
            mAllowHandleTouch = idleAnim == null || !idleAnim.isStarted();
        }

        boolean result = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mInitialTouchX = event.getRawX();
                mInitialTouchY = event.getRawY();
                float translationX = v.getTranslationX();
                if (translationX < mMinTranslationX) {
                    v.setTranslationX(mMinTranslationX);
                }
                if (translationX > mMaxTranslationX) {
                    v.setTranslationX(mMaxTranslationX);
                }

                mLastTranslationX = v.getTranslationX();
                mLastTranslationY = v.getTranslationY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!mAllowHandleTouch) {
                    return false;
                }

                float x = event.getRawX();
                float y = event.getRawY();

                float tx = x - mInitialTouchX;
                float ty = y - mInitialTouchY;

                if (Math.abs(tx) > mTouchSlop || Math.abs(ty) > mTouchSlop) {
                    mInDragging = true;
                }

                if (mInDragging) {
                    float translationX = mLastTranslationX + tx;
                    float translationY = mLastTranslationY + ty;

                    translationX = Math.max(mMinTranslationX, translationX);
                    translationX = Math.min(mMaxTranslationX, translationX);

                    translationY = Math.max(mMinTranslationY, translationY);
                    translationY = Math.min(mMaxTranslationY, translationY);

                    v.setTranslationX(translationX);
                    v.setTranslationY(translationY);
                }
                result = mInDragging;
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                result = mInDragging;
                mInDragging = false;
                startIdleAnim();
                break;
            }
        }
        return result;
    }

    private void startIdleAnim() {
        View view = viewRef.get();
        if (view == null) return;

        float startTranslation = view.getTranslationX();
        float endTranslation;
        switch (mIdleMode) {
            default:
            case IDLE_MODE_NONE: {
                endTranslation = startTranslation;
                break;
            }
            case IDLE_MODE_LEFT_SIDE: {
                endTranslation = mMinTranslationX;
                break;
            }
            case IDLE_MODE_RIGHT_SIDE: {
                endTranslation = mMaxTranslationX;
                break;
            }
            case IDLE_MODE_BOTH_SIDE: {
                if (startTranslation > (mMaxTranslationX - mMinTranslationX) * 0.5f) {
                    endTranslation = mMaxTranslationX;
                } else {
                    endTranslation = mMinTranslationX;
                }
                break;
            }
        }
        if (startTranslation == endTranslation) {
            mHalfHiddenTask.post();
            return;
        }

        mIdleAnim = ObjectAnimator.ofFloat(view, "translationX", startTranslation, endTranslation)
                .setDuration(300);

        mIdleAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mIdleAnim = null;
                mHalfHiddenTask.post();
            }
        });

        mIdleAnim.start();
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        v.getViewTreeObserver().addOnGlobalLayoutListener(this);

        computeBound();

        mHalfHiddenTask.post();
    }


    @Override
    public void onViewDetachedFromWindow(View v) {
        v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        computeBound();
    }

    private void computeBound() {
        View view = viewRef.get();
        if (view == null) return;

        View parent = (View) view.getParent();

        mMinTranslationX = -view.getLeft();
        mMaxTranslationX = parent.getWidth() - view.getWidth() - view.getLeft();

        mMinTranslationY = -view.getTop();
        mMaxTranslationY = parent.getHeight() - view.getHeight() - view.getTop();

        setInitialPosition();
    }

    private void setInitialPosition() {
        if (mPositionInitialized) return;
        View view = viewRef.get();
        if (view == null) return;
        if (mInitialPosition != null) {
            View parent = (View) view.getParent();
            if (parent == null) return;
            int width = parent.getWidth();
            int height = parent.getHeight();
            if (width == 0 || height == 0) return;

            mPositionInitialized = true;

            float translationX;
            float translationY;

            int left = view.getLeft();
            int top = view.getTop();

            int gravity = mInitialPosition.gravity;

            final int layoutDirection = view.getLayoutDirection();
            final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
            final int horizontalGravity = absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
            final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

            switch (horizontalGravity) {
                case Gravity.CENTER_HORIZONTAL: {
                    translationX = -left + (parent.getWidth() - view.getWidth()) * 0.5f;
                    break;
                }
                case Gravity.RIGHT: {
                    translationX = -left + (parent.getWidth() - view.getWidth());
                    break;
                }
                case Gravity.LEFT: {
                    translationX = -left;
                    break;
                }
                default: {
                    translationX = 0;
                    break;
                }
            }
            switch (verticalGravity) {
                case Gravity.TOP: {
                    translationY = -top;
                    break;
                }
                case Gravity.CENTER_VERTICAL: {
                    translationY = -top + (parent.getHeight() - view.getHeight()) * 0.5f;
                    break;
                }
                case Gravity.BOTTOM: {
                    translationY = -top + (parent.getHeight() - view.getHeight());
                    break;
                }
                default: {
                    translationY = 0;
                    break;
                }
            }
            translationX += parent.getWidth() * mInitialPosition.horizontalBias;
            translationY += parent.getHeight() * mInitialPosition.verticalBias;

            view.setTranslationX(translationX);
            view.setTranslationY(translationY);
        }
    }

    private class HalfHiddenTask implements Runnable {

        @Override
        public void run() {
            View view = viewRef.get();
            if (view == null) return;

            float startTranslationX = view.getTranslationX();
            float endTranslationX = startTranslationX;
            if (startTranslationX == mMinTranslationX) {
                endTranslationX = startTranslationX - view.getWidth() * mLeftRatio;
            } else if (startTranslationX == mMaxTranslationX) {
                endTranslationX = startTranslationX + view.getWidth() * (1 - mLeftRatio);
            }
            if (startTranslationX == endTranslationX) {
                return;
            }

            ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", startTranslationX, endTranslationX)
                    .setDuration(300);
            animator.start();
        }

        void post() {
            if (!mHalfHidden) return;
            View view = viewRef.get();
            if (view == null) return;
            Handler handler = view.getHandler();
            if (handler == null) return;
            handler.postDelayed(this, 3000);
        }

        void cancel() {
            View view = viewRef.get();
            if (view == null) return;
            Handler handler = view.getHandler();
            if (handler == null) return;
            handler.removeCallbacks(this);
        }
    }

    public static class InitialPosition {

        public int gravity;

        public float horizontalBias;

        public float verticalBias;

        public InitialPosition(int gravity, float horizontalBias, float verticalBias) {
            this.gravity = gravity;
            this.horizontalBias = horizontalBias;
            this.verticalBias = verticalBias;
        }
    }

    public static class Builder {

        private View view;
        private InitialPosition initialPosition;
        private int idleMode;
        private boolean halfHidden;
        private float leftRatio;

        public Builder(View view) {
            this.view = view;
        }

        public Builder initialPosition(InitialPosition initialPosition) {
            this.initialPosition = initialPosition;
            return this;
        }

        public Builder idleMode(@IdleMode int idleMode) {
            this.idleMode = idleMode;
            return this;
        }

        public Builder halfHidden(boolean halfHidden) {
            this.halfHidden = halfHidden;
            return this;
        }

        public Builder leftRatio(float leftRatio) {
            this.leftRatio = leftRatio;
            return this;
        }

        public DragViewHelper build() {
            return new DragViewHelper(this);
        }
    }
}