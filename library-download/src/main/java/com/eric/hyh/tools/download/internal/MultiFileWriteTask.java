package com.eric.hyh.tools.download.internal;

import android.util.Log;

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

    private String tempPath;

    private long startPosition;

    private long endPosition;

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
        } catch (IOException e) {
            e.printStackTrace();
            isException = true;
        } finally {
            Utils.close(fileRaf);
            Utils.close(tempRaf);
            Utils.close(response);
        }
        if (startPosition == endPosition + 1) {
            listener.onWriteFinish();
        } else if (isException) {
            listener.onWriteFailure();
        }
    }

    @Override
    public void stop() {
        Log.d("FDL_HH","MultiFileWriteTask stop");
        this.stop = true;
    }
}
