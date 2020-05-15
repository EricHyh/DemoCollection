package com.hyh.plg.exception;

/**
 * @author Administrator
 * @description
 * @data 2019/9/5
 */

public class ProxyActivityNotFoundException extends RuntimeException {

    public ProxyActivityNotFoundException() {
    }

    public ProxyActivityNotFoundException(String message) {
        super(message);
    }

    public ProxyActivityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProxyActivityNotFoundException(Throwable cause) {
        super(cause);
    }
}