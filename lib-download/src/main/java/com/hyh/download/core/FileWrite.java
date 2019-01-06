package com.hyh.download.core;


import com.hyh.download.net.HttpResponse;

/**
 * @author Administrator
 * @description
 * @data 2017/7/14
 */
interface FileWrite {

    void write(HttpResponse response, FileWriteListener listener);

    void stop();

    interface FileWriteListener {

        void onWriteFile(long writeLength);

        void onWriteFinish();

        void onWriteFailure(Exception e);

        void onWriteLengthError(long startPosition, long endPosition);
    }
}
