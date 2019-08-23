package com.hyh.web.window;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.animation.DecelerateInterpolator;


/**
 * @author Administrator
 * @description
 * @data 2018/1/24
 */

public class SmoothSlideController implements SlideController {

    private Context mContext;

    private FloatingWindow mFloatingWindow;

    private ReleaseListener mReleaseListener;

    private int[] mRelativePosition = new int[2];

    private final ValueAnimator mSmoothAnimator;

    private final SmoothAnimatorListener mSmoothAnimatorListener;

    public SmoothSlideController(Context context) {
        mContext = context;
        mSmoothAnimator = new ValueAnimator();
        mSmoothAnimator.setDuration(800);
        mSmoothAnimator.setInterpolator(new DecelerateInterpolator());
        mSmoothAnimatorListener = new SmoothAnimatorListener();
        mSmoothAnimator.addUpdateListener(mSmoothAnimatorListener);
        mSmoothAnimator.addListener(mSmoothAnimatorListener);
    }

    @Override
    public void bindFloatWindow(FloatingWindow floatingWindow) {
        mFloatingWindow = floatingWindow;
    }

    @Override
    public boolean tryDrag(int[] initialPosition, int[] touchPosition) {
        mRelativePosition[0] = initialPosition[0] - touchPosition[0];
        mRelativePosition[1] = initialPosition[1] - touchPosition[1];
        return true;
    }

    @Override
    public void onDrag(int[] touchPosition) {
        touchPosition[0] += mRelativePosition[0];
        touchPosition[1] += mRelativePosition[1];
        mFloatingWindow.setPosition(touchPosition[0], touchPosition[1]).commitUpdate();
    }

    @Override
    public void onReleased(int[] touchPosition, ReleaseListener listener) {
        mReleaseListener = listener;
        int currentX = touchPosition[0] + mRelativePosition[0];
        int currentY = touchPosition[1] + mRelativePosition[1];

        int[] screenSize = getScreenSize(mContext);
        int floatWidth = mFloatingWindow.getRootView().getMeasuredWidth();

        int lastX;
        int lastY = currentY;

        if (currentX + floatWidth / 2 > screenSize[0] / 2) {
            lastX = screenSize[0] - floatWidth;
        } else {
            lastX = 0;
        }
        mSmoothAnimator.setFloatValues(currentX, lastX);
        mSmoothAnimatorListener.setPosition(lastX, lastY);
        mSmoothAnimator.start();
    }

    private int[] getScreenSize(Context context) {
        int[] size = new int[2];
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        size[0] = dm.widthPixels;
        size[1] = dm.heightPixels;
        return size;
    }


    private class SmoothAnimatorListener implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

        private int mEndX, mEndY;


        void setPosition(int endX, int endY) {
            mEndX = endX;
            mEndY = endY;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float value = (float) animation.getAnimatedValue();
            int x = (int) (value + 0.5f);
            mFloatingWindow.setPosition(x, mEndY).commitUpdate();
        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mFloatingWindow.setPosition(mEndX, mEndY).commitUpdate();
            if (mReleaseListener != null) {
                mReleaseListener.onReleaseFinish(new int[]{mEndX, mEndY});
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
}
