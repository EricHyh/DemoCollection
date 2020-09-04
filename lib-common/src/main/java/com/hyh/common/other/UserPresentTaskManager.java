package com.hyh.common.other;

import android.content.Context;

import com.hyh.common.log.Logger;
import com.hyh.common.receiver.UserPresentListener;
import com.hyh.common.receiver.UserPresentReceiver;
import com.hyh.common.utils.ThreadUtil;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * @author Administrator
 * @description
 * @data 2018/6/26
 */

public class UserPresentTaskManager implements UserPresentListener {

    private static volatile UserPresentTaskManager sInstance;

    public static UserPresentTaskManager getInstance(Context context) {
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (UserPresentTaskManager.class) {
            if (sInstance == null) {
                sInstance = new UserPresentTaskManager(context.getApplicationContext());
            }
            return sInstance;
        }
    }

    private final TreeSet<UserPresentTask> mMainThreadUserPresentTasks;

    private final TreeSet<UserPresentTask> mBackThreadUserPresentTasks;

    private UserPresentTaskManager(Context context) {
        mMainThreadUserPresentTasks = new TreeSet<>(new UserPresentTaskComparator());
        mBackThreadUserPresentTasks = new TreeSet<>(new UserPresentTaskComparator());
        UserPresentReceiver.getInstance(context).addListener(this);
    }

    public void enqueue(Runnable task) {
        enqueue(task, false, 0);
    }

    public void enqueue(Runnable task, int priority) {
        enqueue(task, false, priority);
    }

    public void enqueue(Runnable task, boolean isBackThread) {
        enqueue(task, isBackThread, 0);
    }

    public void enqueue(Runnable task, long delayMillis) {
        enqueue(task, false, delayMillis);
    }

    public void enqueue(Runnable task, boolean isBackThread, long delayMillis) {
        enqueue(task, isBackThread, delayMillis, 0);
    }

    public void enqueue(Runnable task, boolean isBackThread, int priority) {
        enqueue(task, isBackThread, 0, priority);
    }

    public void enqueue(Runnable task, long delayMillis, int priority) {
        enqueue(task, false, delayMillis, priority);
    }

    public synchronized void enqueue(Runnable task, boolean isBackThread, long delayMillis, int priority) {
        UserPresentTask userPresentTask = new UserPresentTask(task, delayMillis, priority);
        if (isBackThread) {
            mBackThreadUserPresentTasks.add(userPresentTask);
        } else {
            mMainThreadUserPresentTasks.add(userPresentTask);
        }
    }

    public boolean remove(Runnable task) {
        return remove(task, false);
    }

    public synchronized boolean remove(Runnable task, boolean isBackThread) {
        if (isBackThread) {
            return mMainThreadUserPresentTasks.remove(new UserPresentTask(task));
        } else {
            return mBackThreadUserPresentTasks.remove(new UserPresentTask(task));
        }
    }

    public boolean contains(Runnable task) {
        return contains(task, false);
    }

    public synchronized boolean contains(Runnable task, boolean isBackThread) {
        if (isBackThread) {
            return mBackThreadUserPresentTasks.contains(new UserPresentTask(task));
        } else {
            return mMainThreadUserPresentTasks.contains(new UserPresentTask(task));
        }
    }


    public boolean isEmpty() {
        return mMainThreadUserPresentTasks.isEmpty() && mBackThreadUserPresentTasks.isEmpty();
    }

    @Override
    public void onUserPresent() {
        Logger.d("UserPresentTaskManager: onUserPresent");

        Logger.d("main thread USER_PRESENT tasks size is " + mMainThreadUserPresentTasks.size());
        Logger.d("back thread USER_PRESENT tasks size is " + mBackThreadUserPresentTasks.size());
        synchronized (UserPresentTaskManager.this) {
            if (!mMainThreadUserPresentTasks.isEmpty()) {
                for (UserPresentTask mainThreadUserPresentTask : mMainThreadUserPresentTasks) {
                    if (mainThreadUserPresentTask.delayMillis == 0) {
                        ThreadUtil.postUiThread(mainThreadUserPresentTask);
                    } else {
                        ThreadUtil.postUiThreadDelayed(mainThreadUserPresentTask, mainThreadUserPresentTask.delayMillis);
                    }
                }
            }
            mMainThreadUserPresentTasks.clear();
            if (!mBackThreadUserPresentTasks.isEmpty()) {
                for (UserPresentTask backThreadUserPresentTask : mBackThreadUserPresentTasks) {
                    if (backThreadUserPresentTask.delayMillis == 0) {
                        ThreadUtil.postBackThread(backThreadUserPresentTask);
                    } else {
                        ThreadUtil.postBackThreadDelayed(backThreadUserPresentTask, backThreadUserPresentTask.delayMillis);
                    }
                }
            }
            mBackThreadUserPresentTasks.clear();
        }
    }

    private static class UserPresentTask implements Runnable {

        private Runnable realTask;
        private long delayMillis;
        private int priority;

        private UserPresentTask(Runnable realTask) {
            this.realTask = realTask;
        }

        private UserPresentTask(Runnable realTask, long delayMillis, int priority) {
            this.realTask = realTask;
            this.delayMillis = delayMillis < 0 ? 0 : delayMillis;
            this.priority = priority;
        }

        @Override
        public void run() {
            if (realTask != null) {
                realTask.run();
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserPresentTask that = (UserPresentTask) o;

            return realTask.equals(that.realTask);
        }

        @Override
        public int hashCode() {
            return realTask.hashCode();
        }
    }

    private static class UserPresentTaskComparator implements Comparator<UserPresentTask> {

        @Override
        public int compare(UserPresentTask o1, UserPresentTask o2) {
            if (o1 == o2) return 0;
            if (o1 != null && o1.equals(o2)) return 0;
            if (o1 == null) return 1;
            if (o1.delayMillis == o2.delayMillis) {
                if (o1.priority == o2.priority) {
                    return 1;
                } else {
                    return o2.priority - o1.priority;
                }
            } else {
                return (o1.delayMillis - o2.delayMillis) > 0 ? 1 : -1;
            }
        }
    }
}