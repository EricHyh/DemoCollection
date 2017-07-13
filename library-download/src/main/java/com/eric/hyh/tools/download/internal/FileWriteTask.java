package com.eric.hyh.tools.download.internal;

import com.eric.hyh.tools.download.api.HttpResponse;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Administrator
 * @description
 * @data 2017/7/13
 */

class FileWriteTask {


    private String filePath;

    private long currentSize;

    private long endSize;

    private volatile boolean stop;

    private FileWriteListener listener;


    void write(HttpResponse response) {
        stop = false;
        BufferedOutputStream bos = null;
        boolean isException = false;
        try {
            BufferedInputStream bis = new BufferedInputStream(response.inputStream());
            bos = new BufferedOutputStream(new FileOutputStream(filePath));
            byte[] buffer = new byte[8 * 1024];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
                currentSize += len;
                listener.onWriteFile(currentSize);
                if (stop) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            isException = true;
        } finally {
            Utils.close(bos);
            Utils.close(response);
        }
        if (currentSize == endSize) {
            listener.onWriteFinish();
        } else if (isException) {
            listener.onWriteFailure();
        }
    }

    void stop() {
        stop = true;
    }

    interface FileWriteListener {

        void onWriteFile(long currentSize);

        void onWriteFailure();

        void onWriteFinish();
    }
}
