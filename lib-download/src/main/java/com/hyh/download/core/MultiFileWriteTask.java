package com.hyh.download.core;

import com.hyh.download.net.HttpResponse;
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

    private int rangeIndex;

    private String tempFilePath;

    private long startPosition;

    private final long endPosition;

    private boolean stop;

    MultiFileWriteTask(String filePath, RangeInfo rangeInfo) {
        this.filePath = filePath;
        this.rangeIndex = rangeInfo.getRangeIndex();
        this.tempFilePath = rangeInfo.getTempFilePath();
        this.startPosition = rangeInfo.getStartPosition();
        this.endPosition = rangeInfo.getEndPosition();
    }

    @Override
    public void write(HttpResponse response, FileWriteListener listener) {
        stop = false;
        RandomAccessFile fileRaf = null;
        RandomAccessFile tempFileRaf = null;
        boolean isException = false;
        try {
            BufferedInputStream bis = new BufferedInputStream(response.inputStream());
            fileRaf = new RandomAccessFile(filePath, "rw");
            tempFileRaf = new RandomAccessFile(tempFilePath, "rws");

            fileRaf.seek(startPosition);
            byte[] buffer = new byte[8 * 1024];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                fileRaf.write(buffer, 0, len);
                startPosition += len;
                listener.onWriteFile(len);
                tempFileRaf.seek(rangeIndex * 8);
                tempFileRaf.writeLong(startPosition);
                if (stop) {
                    break;
                }
            }
        } catch (Exception e) {
            isException = true;
        }
        StreamUtil.close(fileRaf, tempFileRaf, response);
        if (startPosition == endPosition + 1) {
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
