package com.hyh.fyp.reflect;

/**
 * @author Administrator
 * @description
 * @data 2018/11/15
 */

public class RefResult<E> {

    private E result;

    private boolean isSuccess;

    private Throwable throwable;

    void setResult(E result) {
        this.result = result;
    }

    void setSuccess(boolean success) {
        isSuccess = success;
    }

    void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public E getResult() {
        return result;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
