package com.hyh.tools.download.internal;

import com.hyh.tools.download.net.HttpResponse;

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
