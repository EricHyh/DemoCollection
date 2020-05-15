package com.hyh.plg.reflect;

/**
 * @author Administrator
 * @description
 * @data 2018/11/16
 */

class RefException extends RuntimeException {

    RefException(String message) {
        super(message);
    }

    RefException(String message, Throwable cause) {
        super(message, cause);
    }
}
