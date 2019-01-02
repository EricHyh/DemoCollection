package com.hyh.download.core;

import android.os.SystemClock;

import com.hyh.download.net.HttpResponse;
import com.hyh.download.utils.L;
import com.hyh.download.utils.StreamUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private volatile long startPosition;

    private final long endPosition;

    private volatile long lastSyncPosition;

    private volatile long lastSyncTimeMillis;

    private volatile boolean stop;

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
        BufferedOutputStream bos = null;
        boolean isException = false;
        try {
            BufferedInputStream bis = new BufferedInputStream(response.inputStream());
            fileRaf = new RandomAccessFile(filePath, "rw");
            FileDescriptor fd = fileRaf.getFD();
            bos = new BufferedOutputStream(new FileOutputStream(fd));
            tempFileRaf = new RandomAccessFile(tempFilePath, "rws");

            fileRaf.seek(startPosition);
            byte[] buffer = new byte[32 * 1024];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                fileRaf.write(buffer, 0, len);
                L.d("MultiFileWriteTask write:" + len);
                startPosition += len;
                listener.onWriteFile(len);

                if (isNeedSync()) {
                    sync(bos, fd, tempFileRaf);
                }
                if (stop) {
                    break;
                }
            }
            sync(bos, fd, tempFileRaf);
        } catch (Exception e) {
            isException = true;
        }
        StreamUtil.close(bos, fileRaf, tempFileRaf, response);
        if (stop) {
            return;
        }
        if (isException) {
            listener.onWriteFailure();
        } else if (startPosition == endPosition + 1) {
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

    private void sync(BufferedOutputStream bos, FileDescriptor fd, RandomAccessFile tempFileRaf) throws IOException {
        bos.flush();
        fd.sync();
        tempFileRaf.seek(rangeIndex * 8);
        tempFileRaf.writeLong(startPosition);
        lastSyncPosition = startPosition;
        lastSyncTimeMillis = SystemClock.elapsedRealtime();
    }

    @Override
    public void stop() {
        this.stop = true;
    }
}