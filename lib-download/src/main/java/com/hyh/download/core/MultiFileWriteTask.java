package com.hyh.download.core;

import com.hyh.download.net.HttpResponse;
import com.hyh.download.utils.L;
import com.hyh.download.utils.StreamUtil;

import java.io.BufferedInputStream;
import java.io.RandomAccessFile;

/**
 * @author Administrator
 * @description
 * @data 2017/7/14
 */
class MultiFileWriteTask implements FileWrite {

    private String filePath;

    private String tempPath;

    private long startPosition;

    private final long endPosition;

    private boolean stop;

    MultiFileWriteTask(String filePath, String tempPath, long startPosition, long endPosition) {
        this.filePath = filePath;
        this.tempPath = tempPath;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
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
            tempRaf = new RandomAccessFile(tempPath, "rws");

            fileRaf.seek(startPosition);
            byte[] buffer = new byte[8 * 1024];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                fileRaf.write(buffer, 0, len);
                startPosition += len;
                tempRaf.seek(0);
                tempRaf.writeLong(startPosition);
                listener.onWriteFile(len);
                if (stop) {
                    break;
                }
            }
        } catch (Exception e) {
            isException = true;
        } finally {
            StreamUtil.close(fileRaf, tempRaf, response);
        }
        StreamUtil.close(fileRaf, tempRaf, response);
        if (startPosition == endPosition + 1) {
            listener.onWriteFinish();
        } else if (isException) {
            listener.onWriteFailure();
        }
    }

    @Override
    public void stop() {
        L.d("MultiFileWriteTask stop");
        this.stop = true;
    }
}
