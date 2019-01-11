package com.hyh.filedownloader.sample.utils;

import java.io.File;

/**
 * @author Administrator
 * @description
 * @data 2019/1/11
 */

public class FileUtil {


    public static File ensureCreated(File fileDir) {
        if (!fileDir.exists() && !fileDir.mkdirs()) {
            Print.w("Unable to create the directory:" + fileDir.getPath());
        }
        return fileDir;
    }

    public static File ensureCreated(String fileDirPath) {
        return ensureCreated(new File(fileDirPath));
    }
}
