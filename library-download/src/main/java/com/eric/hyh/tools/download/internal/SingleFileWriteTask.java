package com.eric.hyh.tools.download.internal;

import android.util.Log;

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

    private long startPosition;

    private long endPosition;

    private volatile boolean stop;


    SingleFileWriteTask(String filePath, long startPosition, long endPosition) {
        this.filePath = filePath;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    @Override
    public void write(HttpResponse response, FileWriteListener listener) {
        stop = false;
        BufferedOutputStream bos = null;
        boolean isException = false;
        try {
            BufferedInputStream bis = new BufferedInputStream(response.inputStream());
            bos = new BufferedOutputStream(new FileOutputStream(filePath, true));
            byte[] buffer = new byte[8 * 1024];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
                startPosition += len;
                listener.onWriteFile(len);
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
        if (startPosition == endPosition) {
            listener.onWriteFinish();
        } else if (isException) {
            listener.onWriteFailure();
        }
    }

    @Override
    public void stop() {
        Log.d("FDL_HH","SingleFileWrite stop");
        stop = true;
    }
}
