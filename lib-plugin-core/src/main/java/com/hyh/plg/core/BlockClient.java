package com.hyh.plg.core;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;

import com.hyh.plg.android.ResourceBlockApplication;
import com.hyh.plg.api.BlockEnv;
import com.hyh.plg.api.IBlockApplication;
import com.hyh.plg.convert.BaseProtocolInterface;
import com.hyh.plg.convert.EmptyProtocolInterface;
import com.hyh.plg.convert.IProtocolInterface;
import com.hyh.plg.convert.ProtocolInterfaceConverter;
import com.hyh.plg.manager.PackageManagerHandler;
import com.hyh.plg.reflect.RefResult;
import com.hyh.plg.reflect.Reflect;
import com.hyh.plg.utils.Logger;
import com.hyh.plg.utils.NativeLibraryHelperCompat;

import java.io.File;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2020/4/16
 */
public class BlockClient implements IBlock {

    private Application mHostApplication;
    private Context mBaseContext;
    private ClassLoader mHostClassLoader;
    private Map<String, Object> mParams;
    private String mBlockPath;

    private boolean mIndependentProcess;

    private Application mBlockApplication;
    private IBlockApplication mBlockApplicationInterface;
    private Object mLoadedApk;
    private Object mActivityThread;


    private final ProtocolInterfaceConverter mConverter = new ProtocolInterfaceConverter();
    private IProtocolInterface mProtocolInterface = new EmptyProtocolInterface();

    @Override
    public boolean prepare(Application application, ClassLoader hostClassLoader, Map<String, Object> params) {
        this.mHostApplication = application;
        this.mBaseContext = application.getBaseContext();
        this.mHostClassLoader = hostClassLoader;
        this.mParams = params;
        this.mBlockPath = (String) params.get(ParamsKey.BLOCK_PATH);
        Reflect.exemptAll();

        String librarySearchPath = (String) params.get(ParamsKey.LIBRARY_SEARCH_PATH);
        Logger.d("BlockClient prepare: librarySearchPath = " + librarySearchPath);
        if (!TextUtils.isEmpty(librarySearchPath)) {
            boolean isLibraryCopied = false;
            File librarySearchDir = new File(librarySearchPath);
            if (librarySearchDir.exists()) {
                String[] list = librarySearchDir.list();
                if (list != null && list.length > 0) {
                    isLibraryCopied = true;
                }
            }

            Logger.d("BlockClient prepare: isLibraryCopied = " + isLibraryCopied);
            if (!isLibraryCopied) {
                int copyNativeBinaries = NativeLibraryHelperCompat.copyNativeBinaries(new File(mBlockPath), librarySearchDir);
                Logger.d("BlockClient prepare: copyNativeBinaries = " + copyNativeBinaries);
            }
        }

        return true;
    }

    @Override
    public boolean attachBaseContext(boolean isIndependentProcess) {
        this.mIndependentProcess = isIndependentProcess;

        BlockEnv.sHostApplication = mHostApplication;
        BlockEnv.sHostResources = mHostApplication.getResources();

        return isIndependentProcess ? attachBaseContextIndependent() : attachBaseContextNotIndependent();
    }

    private boolean attachBaseContextIndependent() {
        PackageInfo blockPackageInfo = mHostApplication.getPackageManager().getPackageArchiveInfo(mBlockPath, PackageManager.GET_ACTIVITIES
                | PackageManager.GET_SERVICES | PackageManager.GET_RECEIVERS | PackageManager.GET_PROVIDERS | PackageManager.GET_META_DATA);

        if (blockPackageInfo == null) {
            Logger.e("BlockClient attachBaseContext: block packageInfo is null");
            Process.killProcess(Process.myPid());
            System.exit(0);
            return false;
        }

        blockPackageInfo.applicationInfo.sourceDir = mBlockPath;
        blockPackageInfo.applicationInfo.publicSourceDir = mBlockPath;

        final Application application = mHostApplication;

        BlockEnv.sBlockPackageInfo = blockPackageInfo;

        ApplicationInfo blockApplicationInfo = blockPackageInfo.applicationInfo;
        String name = blockApplicationInfo.name;
        if (TextUtils.isEmpty(name)) {
            name = blockApplicationInfo.className;
        }

        mBlockApplication = (Application) Reflect.from(name).constructor().newInstance();

        if (mBlockApplication == null) {
            Logger.e("BlockClient attachBaseContextIndependent: blockApplication is null");
            Process.killProcess(Process.myPid());
            System.exit(0);
            return false;
        }
        if (!(mBlockApplication instanceof IBlockApplication)) {
            Logger.e("BlockClient attachBaseContextIndependent: blockApplication is not IBlockApplication");
            Process.killProcess(Process.myPid());
            System.exit(0);
            return false;
        }
        mBlockApplicationInterface = (IBlockApplication) mBlockApplication;

        mLoadedApk = Reflect.from(mBaseContext.getClass()).filed("mPackageInfo").get(mBaseContext);
        if (mLoadedApk == null) {
            Logger.e("BlockClient attachBaseContextIndependent: loadedApk is null");
            Process.killProcess(Process.myPid());
            System.exit(0);
            return false;
        }

        Class<?> loadedApkClass = mLoadedApk.getClass();

        mActivityThread = Reflect.from(loadedApkClass).filed("mActivityThread").get(mLoadedApk);
        if (mActivityThread == null) {
            Logger.e("BlockClient attachBaseContextIndependent: mActivityThread is null");
            Process.killProcess(Process.myPid());
            System.exit(0);
            return false;
        }

        RefResult<Object> result = new RefResult<>();

        Reflect.from(loadedApkClass).filed("mClassLoader").saveResult(result).set(mLoadedApk, BlockClient.class.getClassLoader());
        if (!checkRefResult(result)) return false;

        Reflect.from(loadedApkClass).filed("mAppDir").saveResult(result).set(mLoadedApk, mBlockPath);
        if (!checkRefResult(result)) return false;

        Reflect.from(loadedApkClass).filed("mResDir").saveResult(result).set(mLoadedApk, mBlockPath);
        if (!checkRefResult(result)) return false;

        Reflect.from(loadedApkClass).filed("mResources").saveResult(result).set(mLoadedApk, null);
        if (!checkRefResult(result)) return false;

        if (Build.VERSION.SDK_INT >= 26) {
            Reflect.from(loadedApkClass).method("getResources")
                    .saveResult(result)
                    .invoke(mLoadedApk);
        } else {
            Reflect.from(loadedApkClass).method("getResources")
                    .saveResult(result)
                    .param(mActivityThread.getClass(), mActivityThread)
                    .invoke(mLoadedApk);
        }
        if (!checkRefResult(result)) return false;


        Resources resources = Reflect.from(loadedApkClass).filed("mResources", Resources.class).get(mLoadedApk);
        if (resources != null) {
            Reflect.from(mBaseContext.getClass()).filed("mResources").saveResult(result).set(mBaseContext, resources);
        }
        if (!checkRefResult(result)) return false;


        Reflect.from(Application.class).method("attach")
                .saveResult(result)
                .param(Context.class, mBaseContext)
                .invoke(mBlockApplication);
        if (!checkRefResult(result)) return false;


        mBlockApplicationInterface.afterAttach(application, mParams);

        Instrumentation instrumentation = Reflect.from(mActivityThread.getClass())
                .filed("mInstrumentation", Instrumentation.class)
                .saveResult(result)
                .get(mActivityThread);
        if (!checkRefResult(result)) return false;


        if (instrumentation != null) {
            /*BlockInstrumentation blockInstrumentation = new BlockInstrumentation(application, instrumentation, mParams);
            Reflect.from(mActivityThread.getClass())
                    .filed("mInstrumentation")
                    .saveResult(result)
                    .set(mActivityThread, blockInstrumentation);
            if (!checkRefResult(result)) return false;*/
        }

        Object iPackageManager = Reflect.from(mActivityThread.getClass())
                .method("getPackageManager")
                .invoke(null);
        if (iPackageManager != null) {
            Object iPackageManagerProxy = Proxy.newProxyInstance(
                    BlockClient.class.getClassLoader(),
                    iPackageManager.getClass().getInterfaces(),
                    new PackageManagerHandler(application, iPackageManager));
            Reflect.from(mActivityThread.getClass()).filed("sPackageManager").set(null, iPackageManagerProxy);
            PackageManager packageManager = application.getPackageManager();
            Reflect.from(packageManager.getClass()).filed("mPM").set(packageManager, iPackageManagerProxy);
        }

        return true;
    }

    private boolean attachBaseContextNotIndependent() {
        PackageInfo blockPackageInfo = (PackageInfo) mParams.get(ParamsKey.BLOCK_PACKAGE_INFO);
        if (blockPackageInfo == null) return false;

        blockPackageInfo.applicationInfo.sourceDir = mBlockPath;
        blockPackageInfo.applicationInfo.publicSourceDir = mBlockPath;
        BlockEnv.sBlockPackageInfo = blockPackageInfo;


        ApplicationInfo blockApplicationInfo = blockPackageInfo.applicationInfo;
        String name = blockApplicationInfo.name;
        if (TextUtils.isEmpty(name)) {
            name = blockApplicationInfo.className;
        }

        mBlockApplication = (Application) Reflect.from(name).constructor().newInstance();

        if (mBlockApplication == null) {
            Logger.e("BlockClient attachBaseContextNotIndependent: resourceApplication is null");
            return false;
        }
        if (!(mBlockApplication instanceof IBlockApplication)) {
            Logger.e("BlockClient attachBaseContextNotIndependent: resourceApplication is not IBlockApplication");
            mBlockApplication = null;
            return false;
        }

        mBlockApplicationInterface = (IBlockApplication) mBlockApplication;

        ResourceBlockApplication resourceBlockApplication = new ResourceBlockApplication(mHostApplication, mBlockPath);

        int defaultTheme = mBlockApplicationInterface.getDefaultTheme();

        if (defaultTheme != 0) {
            resourceBlockApplication.setDefaultTheme(defaultTheme);
        } else {
            resourceBlockApplication.setDefaultTheme(ResourceBlockApplication.DEFAULT_THEME_RESOURCE);
        }

        Reflect.from(Application.class).method("attach")
                .param(Context.class, resourceBlockApplication)
                .invoke(mBlockApplication);

        mBlockApplicationInterface.afterAttach(mHostApplication, mParams);

        return false;
    }

    @Override
    public void onApplicationCreate() {
        if (mBlockApplication == null) return;
        if (mIndependentProcess) {
            Reflect.from(mBaseContext.getClass()).filed("mOuterContext").set(mBaseContext, mBlockApplication);
            Reflect.from(mLoadedApk.getClass()).filed("mApplication").set(mLoadedApk, mBlockApplication);
            Reflect.from(mActivityThread.getClass()).filed("mInitialApplication").set(mActivityThread, mBlockApplication);

            List allApplications = (List) Reflect
                    .from(mActivityThread.getClass())
                    .filed("mAllApplications")
                    .get(mActivityThread);

            if (allApplications != null) {
                for (int index = 0; index < allApplications.size(); index++) {
                    Object application = allApplications.get(index);
                    if (application == mHostApplication) {
                        allApplications.set(index, mBlockApplication);
                    }
                }
            }
        } else {
            List<String> protocolInterfaceNames = mBlockApplicationInterface.getProtocolInterfaceNames();
            mProtocolInterface = new BaseProtocolInterface(protocolInterfaceNames);
        }

        mBlockApplication.onCreate();
    }

    @Override
    public void setInnerProtocolInterfaceMap(Map<String, String> interfaceMap) {

    }

    @Override
    public Object command(String command, Map<String, Object> params, Object... masterClients) {
        return null;
    }

    @Override
    public Object getInnerBlockProxy(String proxyKey, Map<String, Object> params, Object... masterClients) {
        return null;
    }

    @Override
    public Object getBlockProxy(String proxyKey, Map<String, Object> params, Object... masterClients) {
        if (mBlockApplicationInterface != null) {
            Object[] masterProxies = mConverter.convert(getClass().getClassLoader(), masterClients, mProtocolInterface);
            ParamWrapper[] paramWrappers = keepOriginal(masterClients, masterProxies);
            Object blockProxy = mBlockApplicationInterface.getBlockProxy(proxyKey, params, paramWrappers);
            return mConverter.convert(mHostClassLoader, blockProxy, mProtocolInterface);
        }
        return null;
    }

    private boolean checkRefResult(RefResult<Object> result) {
        if (!result.isSuccess()) {
            Logger.e("BlockClient attachBaseContext: refAction action failed: ", result.getThrowable());
            Process.killProcess(Process.myPid());
            System.exit(0);
            return false;
        }
        result.clear();
        return true;
    }

    private ParamWrapper[] keepOriginal(Object[] masterClients, Object[] masterProxies) {
        if (masterClients == null || masterClients.length <= 0) {
            return null;
        }
        ParamWrapper[] paramWrappers = new ParamWrapper[masterClients.length];
        for (int index = 0; index < paramWrappers.length; index++) {
            ParamWrapper paramsEntry = new ParamWrapper(masterClients[index], masterProxies[index]);
            paramWrappers[index] = paramsEntry;
        }
        return paramWrappers;
    }
}