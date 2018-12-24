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

    private String rangeFilePath;

    private long startPosition;

    private final long endPosition;

    private boolean stop;

    MultiFileWriteTask(String filePath, RangeInfo rangeInfo) {
        this.filePath = filePath;
        this.rangeIndex = rangeInfo.getRangeIndex();
        this.rangeFilePath = rangeInfo.getRangeFilePath();
        this.startPosition = rangeInfo.getStartPosition();
        this.endPosition = rangeInfo.getEndPosition();
    }

    @Override
    public void write(HttpResponse response, FileWriteListener listener) {
        stop = false;
        RandomAccessFile fileRaf = null;
        RandomAccessFile rangeFileRaf = null;
        boolean isException = false;
        try {
            BufferedInputStream bis = new BufferedInputStream(response.inputStream());
            fileRaf = new RandomAccessFile(filePath, "rw");
            rangeFileRaf = new RandomAccessFile(rangeFilePath, "rws");

            fileRaf.seek(startPosition);
            byte[] buffer = new byte[8 * 1024];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                fileRaf.write(buffer, 0, len);
                startPosition += len;
                rangeFileRaf.seek(rangeIndex * 8);
                rangeFileRaf.writeLong(startPosition);
                listener.onWriteFile(len);
                if (stop) {
                    break;
                }
            }
        } catch (Exception e) {
            isException = true;
        } finally {
            StreamUtil.close(fileRaf, rangeFileRaf, response);
        }
        StreamUtil.close(fileRaf, rangeFileRaf, response);
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
