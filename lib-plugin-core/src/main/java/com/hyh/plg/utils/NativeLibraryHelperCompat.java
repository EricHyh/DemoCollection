package com.hyh.plg.utils;

import android.annotation.TargetApi;
import android.os.Build;

import com.hyh.plg.reflect.Reflect;

import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class NativeLibraryHelperCompat {

    public static int copyNativeBinaries(File apkFile, File sharedLibraryDir) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return copyNativeBinariesAfterL(apkFile, sharedLibraryDir);
        } else {
            return copyNativeBinariesBeforeL(apkFile, sharedLibraryDir);
        }
    }

    private static int copyNativeBinariesBeforeL(File apkFile, File sharedLibraryDir) {
        return Reflect.from("com.android.internal.content.NativeLibraryHelper")
                .method("copyNativeBinariesIfNeededLI", int.class)
                .param(File.class, apkFile)
                .param(File.class, sharedLibraryDir)
                .defaultValue(-1)
                .invoke(null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static int copyNativeBinariesAfterL(File apkFile, File sharedLibraryDir) {
        Class handleClass = Reflect.classForName("com.android.internal.content.NativeLibraryHelper$Handle");
        if (handleClass == null) return -1;

        Object handle = Reflect.from(handleClass)
                .method("create")
                .param(File.class, apkFile)
                .invoke(null);
        if (handle == null) return -1;

        String abi = null;
        final String nativeLibraryHelperClassPath = "com.android.internal.content.NativeLibraryHelper";
        if (is64Bit()) {
            if (Build.SUPPORTED_64_BIT_ABIS.length > 0) {
                int abiIndex = Reflect.from(nativeLibraryHelperClassPath)
                        .method("findSupportedAbi", int.class)
                        .param(handleClass, handle)
                        .param(String[].class, Build.SUPPORTED_64_BIT_ABIS)
                        .invoke(null);
                if (abiIndex >= 0) {
                    abi = Build.SUPPORTED_64_BIT_ABIS[abiIndex];
                }
            }
        } else {
            if (Build.SUPPORTED_32_BIT_ABIS.length > 0) {
                int abiIndex = Reflect.from(nativeLibraryHelperClassPath)
                        .method("findSupportedAbi", int.class)
                        .param(handleClass, handle)
                        .param(String[].class, Build.SUPPORTED_32_BIT_ABIS)
                        .invoke(null);
                if (abiIndex >= 0) {
                    abi = Build.SUPPORTED_32_BIT_ABIS[abiIndex];
                }
            }
        }
        if (abi == null) {
            Logger.e("Not match any abi [" + apkFile.getPath() + "].");
            return -1;
        }

        return Reflect.from(nativeLibraryHelperClassPath)
                .method("copyNativeBinaries", int.class)
                .param(handleClass, handle)
                .param(File.class, sharedLibraryDir)
                .param(String.class, abi)
                .defaultValue(-1)
                .invoke(null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static boolean is64Bit() {
        Object runtime = Reflect.from("dalvik.system.VMRuntime")
                .method("getRuntime")
                .invoke(null);
        if (runtime == null) {
            return false;
        }
        return Reflect.from(runtime.getClass()).method("is64Bit", boolean.class).invoke(runtime);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static boolean isVM64(Set<String> supportedABIs) {
        if (Build.SUPPORTED_64_BIT_ABIS.length == 0) {
            return false;
        }

        if (supportedABIs == null || supportedABIs.isEmpty()) {
            return true;
        }

        for (String supportedAbi : supportedABIs) {
            if ("arm64-v8a".endsWith(supportedAbi) || "x86_64".equals(supportedAbi) || "mips64".equals(supportedAbi)) {
                return true;
            }
        }

        return false;
    }

    private static Set<String> getABIsFromApk(String apk) {
        try {
            ZipFile apkFile = new ZipFile(apk);
            Enumeration<? extends ZipEntry> entries = apkFile.entries();
            Set<String> supportedABIs = new HashSet<String>();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.contains("../")) {
                    continue;
                }
                if (name.startsWith("lib/") && !entry.isDirectory() && name.endsWith(".so")) {
                    String supportedAbi = name.substring(name.indexOf("/") + 1, name.lastIndexOf("/"));
                    supportedABIs.add(supportedAbi);
                }
            }
            return supportedABIs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
