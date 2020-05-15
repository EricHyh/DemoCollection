package com.hyh.plg.protocol;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.text.TextUtils;

import com.hyh.plg.android.InnerBlockClassLoader;
import com.hyh.plg.reflect.HiddenApiExempt;
import com.hyh.plg.reflect.Reflect;
import com.hyh.plg.utils.FileUtil;
import com.hyh.plg.utils.Logger;
import com.hyh.plg.utils.NativeLibraryHelperCompat;
import com.hyh.plg.utils.PackageUtils;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2018/3/21
 */

public class BlockClient implements IBlock {

    private IBlock mRealBlockProxy;

    private void initCheckerFlag(Map params) {
        String checkerFlag = null;
        Object checkerFlagObj = params.get("__master_checker_flag__");
        if (checkerFlagObj != null) {
            checkerFlag = checkerFlagObj.toString();
        }
        if (TextUtils.isEmpty(checkerFlag)) {
            Logger.setTag("HOT_UPDATER");
            FileUtil.setBlockDirName("block");
        } else {
            Logger.setTag(checkerFlag + "_" + "HOT_UPDATER");
            FileUtil.setBlockDirName(checkerFlag + "_" + "block");
        }
    }

    private IBlock createRealBlock(Context context, String blockPath) {
        PackageInfo blockPackageInfo = PackageUtils.getBlockPackageInfo(context, blockPath);
        if (blockPackageInfo == null) {
            return new InnerBlockClient();
        }
        String libraryPath = FileUtil.getLibraryPath(context, blockPackageInfo.packageName);
        boolean isClassLoaderSetLibraryPath = false;
        ClassLoader classLoader = this.getClass().getClassLoader();
        try {
            Object pathList = Reflect.from(classLoader.getClass()).filed("pathList").get(classLoader);
            if (pathList != null) {
                @SuppressWarnings("unchecked")
                List<File> nativeLibraryDirectories =
                        (List<File>) Reflect.from(pathList.getClass()).filed("nativeLibraryDirectories").get(pathList);
                if (nativeLibraryDirectories != null && !nativeLibraryDirectories.isEmpty()) {
                    for (File nativeLibraryDirectory : nativeLibraryDirectories) {
                        if (TextUtils.equals(libraryPath, nativeLibraryDirectory.getAbsolutePath())) {
                            isClassLoaderSetLibraryPath = true;
                            break;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (isClassLoaderSetLibraryPath) {
            long copyNativeBinariesStart = System.currentTimeMillis();
            int result = NativeLibraryHelperCompat.copyNativeBinaries(new File(blockPath), new File(libraryPath));
            Logger.d("loadBlock copyNativeBinaries: result=" + result + ", use time=" + (System.currentTimeMillis() - copyNativeBinariesStart));
            return new InnerBlockClient();
        } else {
            long copyNativeBinariesStart = System.currentTimeMillis();
            int result = NativeLibraryHelperCompat.copyNativeBinaries(new File(blockPath), new File(libraryPath));
            long copyNativeBinariesEnd = System.currentTimeMillis();
            Logger.d("loadBlock copyNativeBinaries: result=" + result + ", use time=" + (copyNativeBinariesEnd - copyNativeBinariesStart));
            if (result == 1) {
                long start = System.currentTimeMillis();
                String optimizedDirectory = FileUtil.getOptimizedDirectory(context);
                InnerBlockClassLoader blockClassLoader = new InnerBlockClassLoader(context, blockPath, optimizedDirectory, libraryPath);
                long end = System.currentTimeMillis();
                Logger.d("loadBlock create InnerBlockClassLoader use time=" + (end - start));
                Object innerBlock = null;
                try {
                    Class<?> innerBlockClass = blockClassLoader.loadClass(InnerBlockClient.class.getName());
                    innerBlock = innerBlockClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (innerBlock == null) {
                    return null;
                }
                return (IBlock) Proxy.newProxyInstance(IBlock.class.getClassLoader(), new Class[]{IBlock.class}, new BlockHandler(innerBlock));
            } else {
                return new InnerBlockClient();
            }
        }
    }

    /**
     * @param context
     * @param masterClassLoader
     * @param blockPath
     * @param params            需要代理的接口的传参（基本数据类型）
     * @param masterClients     需要代理的接口的传参（宿主的对象）
     * @return
     */
    @SuppressWarnings("all")
    @Override
    public boolean onCreate(Context context, ClassLoader masterClassLoader, String blockPath, Map params, Object... masterClients) {
        //__master_checker_flag__
        initCheckerFlag(params);
        synchronized (BlockClient.this) {
            if (mRealBlockProxy == null) {
                mRealBlockProxy = createRealBlock(context, blockPath);
            }
            if (mRealBlockProxy != null) {
                return mRealBlockProxy.onCreate(context, masterClassLoader, blockPath, params, masterClients);
            }
            return false;
        }
    }


    @Override
    public Object command(String command, Map params, Object... masterClients) {
        if (mRealBlockProxy != null) {
            return mRealBlockProxy.command(command, params, masterClients);
        }
        return null;
    }

    @Override
    public Object getBlockProxy(String proxyKey, Map params, Object... masterClients) {
        if (mRealBlockProxy != null) {
            return mRealBlockProxy.getBlockProxy(proxyKey, params, masterClients);
        }
        return null;
    }

    @Override
    public void setInnerProtocolInterfaceMap(Map<String, String> interfaceMap) {
        if (mRealBlockProxy != null) {
            mRealBlockProxy.setInnerProtocolInterfaceMap(interfaceMap);
        }
    }

    @Override
    public Object getInnerBlockProxy(String proxyKey, Map params, Object... masterClients) {
        if (mRealBlockProxy != null) {
            return mRealBlockProxy.getInnerBlockProxy(proxyKey, params, masterClients);
        }
        return null;
    }

    @Override
    public void onDestroy() {
        if (mRealBlockProxy != null) {
            mRealBlockProxy.onDestroy();
        }
    }

    private static class BlockHandler implements InvocationHandler {

        private Object mInnerBlock;

        //用于保证返回值为基础数据类型时，不返回null
        private final Map<Class, Object> mElementaryDefaultValue = new HashMap<>(8);

        {
            mElementaryDefaultValue.put(byte.class, Byte.valueOf("0"));
            mElementaryDefaultValue.put(short.class, Short.valueOf("0"));
            mElementaryDefaultValue.put(int.class, 0);
            mElementaryDefaultValue.put(long.class, 0L);
            mElementaryDefaultValue.put(float.class, 0.0f);
            mElementaryDefaultValue.put(double.class, 0.0d);
            mElementaryDefaultValue.put(boolean.class, false);
            mElementaryDefaultValue.put(char.class, '\u0000');
        }

        BlockHandler(Object innerBlock) {
            mInnerBlock = innerBlock;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = null;
            String methodName = null;
            Class<?> returnType = null;
            try {
                methodName = method.getName();
                returnType = method.getReturnType();
                if (mInnerBlock != null) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    method = mInnerBlock.getClass().getMethod(methodName, parameterTypes);
                    result = method.invoke(mInnerBlock, args);
                } else {
                    Logger.d("method:" + methodName + " invoke failed, mBlockClient is null");
                }
            } catch (Throwable throwable) {
                Logger.w("In BlockClient: method:" + methodName + " invoke failed", throwable);
            }
            if (result == null && returnType != null) {
                result = mElementaryDefaultValue.get(returnType);
            }
            return result;
        }
    }

    static {
        HiddenApiExempt.unseal();
        disableHideApiDialog();
    }

    private static void disableHideApiDialog() {
        if (Build.VERSION.SDK_INT >= 28) {
            Object currentActivityThread = Reflect.from("android.app.ActivityThread")
                    .method("currentActivityThread")
                    .invoke(null);
            if (currentActivityThread != null) {
                Reflect.from("android.app.ActivityThread")
                        .filed("mHiddenApiWarningShown")
                        .set(currentActivityThread, true);
            }
        }
    }
}