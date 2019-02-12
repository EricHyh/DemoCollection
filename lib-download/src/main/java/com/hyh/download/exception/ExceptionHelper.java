package com.hyh.download.exception;

import com.hyh.download.FailureCode;

import java.net.SocketTimeoutException;

/**
 * Created by Eric_He on 2019/1/6.
 */

public class ExceptionHelper {

    public static int convertFailureCode(Exception exception) {
        int failureCode = FailureCode.UNKNOWN;
        if (exception == null) {
            return failureCode;
        }
        if (exception instanceof SocketTimeoutException) {
            failureCode = FailureCode.TIME_OUT;
        }
        return failureCode;
    }
}
