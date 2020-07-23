package com.hyh.common.instance;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.hyh.common.log.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2019/3/5
 */

public abstract class LazyInstance<T> {

    private final Object mLock = new Object();

    private final Handler mMessageHandler = new MessageHandler();

    private final InstancePreparedListener mInstancePreparedListener = new InstancePreparedListenerImpl();

    private final Map<String, List<ActionWrapper>> mActionMap = new HashMap<>();

    protected volatile T mInstance;

    private volatile boolean mCanceled;

    private volatile Boolean mInstanceCreated;

    public LazyInstance() {
    }

    protected void prepareInstance() {
        prepareInstance(0);
    }

    protected void prepareInstance(long newInstanceTimeout) {
        if (isPrepared()) {
            newInstance();
        } else {
            addInstancePreparedListener(mInstancePreparedListener);
        }
        if (mInstance == null) {
            if (newInstanceTimeout > 0) {
                mMessageHandler.postDelayed(() -> {
                    if (mInstance == null) {
                        onNewInstanceTimeout();
                    }
                }, newInstanceTimeout);
            }
        }
    }

    private void newInstance() {
        mInstance = getInstance();
        if (mInstance != null) {
            onNewInstanceSuccess(mInstance);
            doSuccessAction();
        } else {
            onNewInstanceFailure();
            doFailureAction();
        }
    }

    protected abstract boolean isPrepared();

    protected abstract void addInstancePreparedListener(InstancePreparedListener listener);

    protected abstract void removeInstancePreparedListener(InstancePreparedListener listener);

    protected abstract T getInstance();

    protected void onNewInstanceTimeout() {
        Logger.d("LazyBlockInstance onNewInstanceTimeout");
    }

    protected void onNewInstanceFailure() {
        Logger.d("LazyBlockInstance onNewInstanceFailure");
    }

    protected void onNewInstanceSuccess(T instance) {
        Logger.d("LazyBlockInstance onNewInstanceSuccess");
    }

    protected void saveAction(String actionKey, Runnable successAction, boolean unique) {
        saveAction(actionKey, successAction, null, 0, null, unique);
    }

    protected void saveAction(String actionKey, Runnable successAction, long timeout, boolean unique) {
        saveAction(actionKey, successAction, null, timeout, null, unique);
    }

    protected void saveAction(String actionKey,
                              Runnable successAction,
                              Runnable failureAction,
                              long timeout,
                              Runnable timeoutAction,
                              boolean unique) {
        synchronized (mLock) {
            if (mInstanceCreated != null) {
                if (mInstanceCreated) {
                    if (successAction != null) {
                        successAction.run();
                    }
                } else {
                    if (failureAction != null) {
                        failureAction.run();
                    }
                }
                return;
            }
            ActionWrapper actionWrapper = new ActionWrapper(actionKey, successAction, failureAction, timeoutAction);
            List<ActionWrapper> actions = mActionMap.get(actionKey);
            if (unique) {
                if (actions == null) {
                    actions = new ArrayList<>();
                    actions.add(actionWrapper);
                    mActionMap.put(actionKey, actions);
                } else {
                    actions.clear();
                    actions.add(actionWrapper);
                }
            } else {
                if (actions == null) {
                    actions = new ArrayList<>();
                    actions.add(actionWrapper);
                    mActionMap.put(actionKey, actions);
                } else {
                    actions.add(actionWrapper);
                }
            }
            if (timeout > 0) {
                Message message = mMessageHandler.obtainMessage();
                message.what = MessageHandler.MSG_TIMEOUT;
                message.obj = actionWrapper;
                mMessageHandler.sendMessageDelayed(message, timeout);
            }
        }
    }

    private void doSuccessAction() {
        synchronized (mLock) {
            mInstanceCreated = true;
            if (!mActionMap.isEmpty()) {
                Collection<List<ActionWrapper>> values = mActionMap.values();
                for (List<ActionWrapper> actions : values) {
                    if (actions != null && !actions.isEmpty()) {
                        for (ActionWrapper action : actions) {
                            if (action != null) {
                                action.doSuccess();
                            }
                        }
                    }
                }
            }
            mActionMap.clear();
        }
    }

    private void doFailureAction() {
        synchronized (mLock) {
            mInstanceCreated = false;
            if (!mActionMap.isEmpty()) {
                Collection<List<ActionWrapper>> values = mActionMap.values();
                for (List<ActionWrapper> actions : values) {
                    if (actions != null && !actions.isEmpty()) {
                        for (ActionWrapper action : actions) {
                            if (action != null) {
                                action.doFailure();
                            }
                        }
                    }
                }
            }
            mActionMap.clear();
        }
    }


    private void doTimeoutAction(ActionWrapper actionWrapper) {
        synchronized (mLock) {
            List<ActionWrapper> actions = mActionMap.get(actionWrapper.actionKey);
            if (actions != null && !actions.isEmpty()) {
                boolean remove = actions.remove(actionWrapper);
                if (remove) {
                    actionWrapper.doTimeout();
                }
            }
        }
    }

    protected void cancel() {
        mCanceled = true;
        removeInstancePreparedListener(mInstancePreparedListener);
        synchronized (mLock) {
            mMessageHandler.removeMessages(MessageHandler.MSG_PREPARED);
            mMessageHandler.removeMessages(MessageHandler.MSG_TIMEOUT);
            mActionMap.clear();
        }
    }

    private class InstancePreparedListenerImpl implements InstancePreparedListener {

        @Override
        public void onPrepared() {
            mMessageHandler.sendEmptyMessage(MessageHandler.MSG_PREPARED);
        }
    }

    private static class ActionWrapper {

        private String actionKey;
        private Runnable successAction;
        private Runnable failureAction;
        private Runnable timeoutAction;

        ActionWrapper(String actionKey, Runnable successAction, Runnable failureAction, Runnable timeoutAction) {
            this.actionKey = actionKey;
            this.successAction = successAction;
            this.failureAction = failureAction;
            this.timeoutAction = timeoutAction;
        }

        void doSuccess() {
            if (successAction != null) {
                successAction.run();
            }
        }

        void doFailure() {
            if (failureAction != null) {
                failureAction.run();
            }
        }

        void doTimeout() {
            if (timeoutAction != null) {
                timeoutAction.run();
            }
        }
    }


    @SuppressLint("HandlerLeak")
    private class MessageHandler extends Handler {

        private static final int MSG_PREPARED = 1;
        private static final int MSG_TIMEOUT = 2;

        MessageHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mCanceled) return;
            int what = msg.what;
            switch (what) {
                case MSG_PREPARED: {
                    removeInstancePreparedListener(mInstancePreparedListener);
                    newInstance();
                    break;
                }
                case MSG_TIMEOUT: {
                    doTimeoutAction((ActionWrapper) msg.obj);
                    break;
                }
            }
        }
    }

    public interface InstancePreparedListener {

        void onPrepared();

    }
}