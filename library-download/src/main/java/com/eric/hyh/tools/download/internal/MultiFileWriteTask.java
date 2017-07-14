package com.eric.hyh.tools.download.internal;

import com.eric.hyh.tools.download.api.HttpResponse;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Administrator
 * @description
 * @data 2017/7/14
 */
class MultiFileWriteTask implements FileWrite {

    private String filePath;

    private long currentSize;

    private long endSize;

    private boolean stop;


    MultiFileWriteTask(String filePath, long currentSize, long totalSize) {
        this.filePath = filePath;
        this.currentSize = currentSize;
        this.endSize = totalSize;
    }

    @Override
    public void write(HttpResponse response, FileWriteListener listener) {
        stop = false;
        RandomAccessFile fileRaf = null;
        RandomAccessFile tempRaf = null;
        boolean isException = false;
        try {
            BufferedInputStream bis = new BufferedInputStream(response.inputStream());
            fileRaf = new RandomAccessFile(filePath, "rw");
            tempRaf = new RandomAccessFile(filePath, "rws");

            fileRaf.seek(currentSize);
            byte[] buffer = new byte[8 * 1024];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                fileRaf.write(buffer, 0, len);
                currentSize += len;
                tempRaf.seek(0);
                tempRaf.writeLong(currentSize);
                listener.onWriteFile(currentSize);
                if (stop) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            isException = true;
        } finally {
            Utils.close(fileRaf);
            Utils.close(tempRaf);
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
        this.stop = true;
    }
}
