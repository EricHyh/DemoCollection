package com.hyh.download.core;

import android.os.SystemClock;

import com.hyh.download.net.HttpResponse;
import com.hyh.download.utils.StreamUtil;

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

    private final long endPosition;

    private volatile long lastSyncPosition;

    private volatile long lastSyncTimeMillis;

    private volatile boolean stop;

    SingleFileWriteTask(String filePath, long startPosition, long endPosition) {
        this.filePath = filePath;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    @Override
    public void write(HttpResponse response, FileWriteListener listener) {
        stop = false;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        Exception exception = null;
        try {
            bis = new BufferedInputStream(response.inputStream());
            bos = new BufferedOutputStream(new FileOutputStream(filePath, true));
            byte[] buffer = new byte[8 * 1024];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
                startPosition += len;
                listener.onWriteFile(len);
                if (isNeedSync()) {
                    sync(bos);
                }
                if (stop) {
                    break;
                }
            }
            sync(bos);
        } catch (Exception e) {
            exception = e;
        }
        StreamUtil.close(bos, bis, response);
        if (stop) {
            return;
        }
        if (exception != null) {
            listener.onWriteFailure(exception);
        } else if (startPosition == endPosition || endPosition <= 0) {
            listener.onWriteFinish();
        } else {
            //下载长度有误
            listener.onWriteLengthError(startPosition, endPosition);
        }
    }

    private boolean isNeedSync() {
        long positionDiff = startPosition - lastSyncPosition;
        long timeMillisDiff = SystemClock.elapsedRealtime() - lastSyncTimeMillis;
        return positionDiff > 65536 && timeMillisDiff > 2000;
    }

    private void sync(BufferedOutputStream bos) throws IOException {
        bos.flush();
        lastSyncPosition = startPosition;
        lastSyncTimeMillis = SystemClock.elapsedRealtime();
    }

    @Override
    public void stop() {
        stop = true;
    }
}
