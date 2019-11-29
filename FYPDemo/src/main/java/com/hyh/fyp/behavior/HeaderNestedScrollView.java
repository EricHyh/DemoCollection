package com.hyh.fyp.behavior;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.OverScroller;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2019/11/16
 */
public class HeaderNestedScrollView extends CustomNestedScrollView implements CoordinatorLayout.AttachedBehavior {

    public HeaderNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HeaderNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @NonNull
    @Override
    public CoordinatorLayout.Behavior getBehavior() {
        return new HeaderBehavior();
    }


    static class HeaderBehavior extends BaseBehavior<View> {

        private static final String TAG = "HeaderBehavior";


        private View mLastFlingView;
        private Field mScrollerField;
        private Map<Class, Method> mStopFlingMethodMap = new HashMap<>();

        HeaderBehavior() {
        }

        @Override
        public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull MotionEvent ev) {
            int action = ev.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                tryToStopFlingOnActionDown(child);
            }
            return super.onInterceptTouchEvent(parent, child, ev);
        }

        private void tryToStopFlingOnActionDown(@NonNull View currentTouchView) {
            View lastFlingView = this.mLastFlingView;
            if (lastFlingView == null || lastFlingView == currentTouchView) {
                return;
            }

            if (lastFlingView instanceof NestedScrollView) {
                OverScroller scroller = getOverScroller((NestedScrollView) lastFlingView);
                if (scroller != null) {
                    Log.d(TAG, "tryToStopFlingOnActionDown: lastFlingView is NestedScrollView");
                    scroller.abortAnimation();
                }
            } else {
                Class<? extends View> viewClass = lastFlingView.getClass();
                Log.d(TAG, "tryToStopFlingOnActionDown: lastFlingView is " + viewClass.getName());
                Method method = mStopFlingMethodMap.get(viewClass);
                if (method == null) {
                    method = findStopFlingMethod(viewClass);
                    if (method != null) {
                        method.setAccessible(true);
                        mStopFlingMethodMap.put(viewClass, method);
                    }
                }
                Log.d(TAG, "tryToStopFlingOnActionDown: stopFlingMethod = " + method);
                if (method != null) {
                    try {
                        method.invoke(lastFlingView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private Method findStopFlingMethod(Class<?> viewClass) {
            try {
                Method stopScrollMethod = viewClass.getDeclaredMethod("stopScroll");
                stopScrollMethod.setAccessible(true);
                return stopScrollMethod;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Method stopFlingMethod = viewClass.getDeclaredMethod("stopFling");
                stopFlingMethod.setAccessible(true);
                return stopFlingMethod;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Class<?> superclass = viewClass.getSuperclass();
            if (superclass == null || superclass == ViewGroup.class || superclass == View.class) {
                return null;
            }
            return findStopFlingMethod(superclass);
        }

        private OverScroller getOverScroller(NestedScrollView nestedScrollView) {
            if (mScrollerField == null) {
                try {
                    mScrollerField = NestedScrollView.class.getDeclaredField("mScroller");
                    mScrollerField.setAccessible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (mScrollerField != null) {
                try {
                    return (OverScroller) mScrollerField.get(nestedScrollView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                           @NonNull View child,
                                           @NonNull View directTargetChild,
                                           @NonNull View target,
                                           int axes,
                                           int type) {
            Log.d(TAG, "onStartNestedScroll: type = " + type);
            return true;
        }

        @Override
        public void onNestedScrollAccepted(@NonNull CoordinatorLayout coordinatorLayout,
                                           @NonNull View child,
                                           @NonNull View directTargetChild,
                                           @NonNull View target,
                                           int axes,
                                           int type) {
            super.onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target, axes, type);
        }

        @Override
        public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                      @NonNull View child,
                                      @NonNull View target,
                                      int dx, int dy,
                                      @NonNull int[] consumed,
                                      int type) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
            if (dy == 0) {
                return;
            }

            int consumedDy;
            if (dy > 0) {//向上滑动
                consumedDy = handleNestedScrollUp(coordinatorLayout, target, dy, type);
            } else {//向下滑动
                consumedDy = handleNestedScrollDown(coordinatorLayout, target, dy, type);
            }
            consumed[1] = consumedDy;
        }


        @Override
        public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                   @NonNull View child,
                                   @NonNull View target,
                                   int dxConsumed,
                                   int dyConsumed,
                                   int dxUnconsumed,
                                   int dyUnconsumed,
                                   int type) {
            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
            if (dyUnconsumed == 0) {
                return;
            }
            if (dyUnconsumed > 0) {//向上滑动
                handleNestedScrollUp(coordinatorLayout, target, dyUnconsumed, type);
            } else {//向下滑动
                handleNestedScrollDown(coordinatorLayout, target, dyUnconsumed, type);
            }
        }


        private int handleNestedScrollUp(CoordinatorLayout parent, View target, int dyUnconsumed, int type) {
            int totalHeaderHeight = NestedScrollHelper.getTotalHeaderHeight(parent);
            int parentScrollY = parent.getScrollY();

            if (parentScrollY >= totalHeaderHeight) {
                return 0;
            }
            int currentParentMaxScrollUpDy = totalHeaderHeight - parentScrollY;
            int parentScrollUpDy = Math.min(dyUnconsumed, currentParentMaxScrollUpDy);
            parent.scrollBy(0, parentScrollUpDy);
            return parentScrollUpDy;
        }

        private int handleNestedScrollDown(CoordinatorLayout parent, View target, int dyUnconsumed, int type) {
            if (target.canScrollVertically(dyUnconsumed)) {
                return 0;
            }
            int scrollY = parent.getScrollY();
            if (scrollY == 0) {
                return 0;
            }
            int currentParentMinScrollDownDy = -scrollY;
            int parentScrollDownDy = Math.max(dyUnconsumed, currentParentMinScrollDownDy);
            parent.scrollBy(0, parentScrollDownDy);
            return parentScrollDownDy;
        }

        @Override
        public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull View child,
                                       @NonNull View target,
                                       int type) {
            super.onStopNestedScroll(coordinatorLayout, child, target, type);
            if (type == ViewCompat.TYPE_NON_TOUCH) {
                mLastFlingView = null;
            }
        }

        @Override
        public boolean onNestedPreFling(@NonNull CoordinatorLayout coordinatorLayout,
                                        @NonNull View child,
                                        @NonNull View target,
                                        float velocityX, float velocityY) {
            if (NestedScrollHelper.isContentView(target)) {
                mLastFlingView = target;
                return false;
            }
            if (Math.abs(velocityY) > 2000) {
                mLastFlingView = target;
                return false;
            }
            mLastFlingView = null;
            return true;
        }

        @Override
        public boolean onNestedFling(@NonNull CoordinatorLayout coordinatorLayout,
                                     @NonNull View child,
                                     @NonNull View target,
                                     float velocityX, float velocityY,
                                     boolean consumed) {
            if (NestedScrollHelper.isContentView(target)) {
                mLastFlingView = target;
                return false;
            }
            if (Math.abs(velocityY) > 2000) {
                mLastFlingView = target;
                return false;
            }
            mLastFlingView = null;
            return true;
        }
    }
}