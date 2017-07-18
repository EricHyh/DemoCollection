package com.eric.hyh.tools.download.internal;

import com.eric.hyh.tools.download.api.HttpResponse;

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

        void onWriteFailure();

    }
}
