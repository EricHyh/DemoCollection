package com.hyh.download.core;

import com.hyh.download.net.HttpResponse;
import com.hyh.download.utils.L;
import com.hyh.download.utils.StreamUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

/**
 * @author Administrator
 * @description
 * @data 2017/7/13
 */

class SingleFileWriteTask implements FileWrite {


    private String filePath;

    private long startPosition;

    private final long endPosition;

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
                    bos.flush();
                    break;
                }
            }
        } catch (Exception e) {
            isException = true;
        } finally {
            StreamUtil.close(bos, response);
        }
        if (startPosition == endPosition) {
            listener.onWriteFinish();
        } else if (endPosition == -1 && !isException && !stop) {
            listener.onWriteFinish();
        } else if (isException) {
            listener.onWriteFailure();
        }
    }

    @Override
    public void stop() {
        L.d("SingleFileWrite stop");
        stop = true;
    }
}
