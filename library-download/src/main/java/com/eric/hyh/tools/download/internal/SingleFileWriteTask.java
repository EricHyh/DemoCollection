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

class SingleFileWriteTask implements FileWrite {


    private String filePath;

    private long currentSize;

    private long endSize;

    private volatile boolean stop;


    SingleFileWriteTask(String filePath, long currentSize, long endSize) {
        this.filePath = filePath;
        this.currentSize = currentSize;
        this.endSize = endSize;
    }

    @Override
    public void write(HttpResponse response, FileWriteListener listener) {
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

    @Override
    public void stop() {
        stop = true;
    }
}
