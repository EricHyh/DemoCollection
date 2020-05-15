package com.hyh.plg.utils;

import android.content.Context;

import java.io.File;

/**
 * @author Administrator
 * @description
 * @data 2018/11/19
 */

public class FileUtil {

    private static String sBlockDirName = "block";

    public static void setBlockDirName(String blockDirName) {
        sBlockDirName = blockDirName;
    }

    public static String getOptimizedDirectory(Context context) {
        File dataDir = context.getApplicationContext().getFilesDir();
        File blockDex = new File(dataDir, sBlockDirName.concat(File.separator).concat("dex"));
        if (!blockDex.exists()) {
            boolean mkdirs = blockDex.mkdirs();
            if (mkdirs) {
                return blockDex.getAbsolutePath();
            } else {
                return dataDir.getAbsolutePath();
            }
        } else {
            return blockDex.getAbsolutePath();
        }
    }

    public static String getLibraryPath(Context context, String blockPackage) {
        /*File hostDataDir = new File(context.getApplicationInfo().dataDir);
        File libDir = ensureCreated(new File(hostDataDir, "lib"));
        return libDir.getAbsolutePath();*/
        File hostDataDir = new File(context.getApplicationInfo().dataDir);
        File root = ensureCreated(new File(hostDataDir, sBlockDirName));
        File blockDataDir = ensureCreated(new File(root, "data"));
        File blockDataAppDir = ensureCreated(new File(blockDataDir, "app"));
        File blockDataAppPackageDir = ensureCreated(new File(blockDataAppDir, blockPackage));
        return ensureCreated(new File(blockDataAppPackageDir, "lib")).getAbsolutePath();
        /*File dataDir = context.getApplicationContext().getFilesDir();
        File blockLib = new File(dataDir, "block".concat(File.separator).concat("lib"));
        if (!blockLib.exists()) {
            boolean mkdirs = blockLib.mkdirs();
            if (mkdirs) {
                return blockLib.getAbsolutePath();
            } else {
                return dataDir.getAbsolutePath();
            }
        } else {
            return blockLib.getAbsolutePath();
        }*/
    }

    private static File ensureCreated(File folder) {
        if (!folder.exists() && !folder.mkdirs()) {
            Logger.w("Unable to create the directory:" + folder.getPath());
        }
        return folder;
    }
}
