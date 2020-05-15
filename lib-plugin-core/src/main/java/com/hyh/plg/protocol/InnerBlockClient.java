package com.hyh.plg.protocol;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.hyh.plg.activity.ActivityBridgeManager;
import com.hyh.plg.activity.ActivityBridgeProxyInfo;
import com.hyh.plg.activity.ActivityProxyImpl;
import com.hyh.plg.activity.BridgeFactory;
import com.hyh.plg.activity.IActivityBridge;
import com.hyh.plg.activity.IBridgeFactory;
import com.hyh.plg.android.BlockApplication;
import com.hyh.plg.android.BlockEnv;
import com.hyh.plg.android.ProxyActivityInfo;
import com.hyh.plg.convert.ProtocolInterfaceConverter;
import com.hyh.plg.dispatch.BlockReceiver;
import com.hyh.plg.service.BlockServiceServer;
import com.hyh.plg.utils.FileUtil;
import com.hyh.plg.utils.Logger;
import com.hyh.plg.utils.PackageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2018/3/21
 */

public class InnerBlockClient implements IBlock {

    private BlockReceiver mBlockReceiver;

    private Context mContext;

    private ClassLoader mMasterClassLoader;

    private ProtocolInterfaceConverter mConverter = new ProtocolInterfaceConverter();

    private IProtocolInterface mProtocolInterface = new EmptyProtocolInterface();

    private IProtocolInterface mInnerProtocolInterface = new EmptyProtocolInterface();

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
        this.mMasterClassLoader = masterClassLoader;
        initCheckerFlag(params);
        Logger.d("current classLoader is " + getClass().getClassLoader());
        Logger.d("master classLoader is " + mMasterClassLoader);
        Logger.d("parent classLoader is " + mMasterClassLoader.getParent());
        PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(blockPath, PackageManager.GET_ACTIVITIES
                | PackageManager.GET_SERVICES | PackageManager.GET_RECEIVERS | PackageManager.GET_PROVIDERS | PackageManager.GET_META_DATA);
        if (packageInfo == null) {
            return false;
        }
        BlockEnv.sHostClassLoader = masterClassLoader;
        BlockEnv.sHostApplicationContext = context.getApplicationContext();
        BlockEnv.sBlockPath = blockPath;
        BlockEnv.sBlockPackageInfo = packageInfo;

        BlockApplication.hostApplicationContext = context.getApplicationContext();
        BlockApplication.hostClassLoader = masterClassLoader;

        ApplicationInfo applicationInfo = packageInfo.applicationInfo;

        BlockApplication blockApplication = new BlockApplication(context, blockPath);
        this.mContext = blockApplication;

        if (mContext == null || mContext.getAssets() == null) {
            return false;
        }
        if (applicationInfo != null) {
            Bundle metaData = applicationInfo.metaData;
            if (metaData != null) {
                String blockReceiverPath = metaData.getString("BLOCK_RECEIVER_PATH");
                Logger.d("BLOCK_RECEIVER_PATH:" + blockReceiverPath);
                try {
                    Class<?> blockReceiverClass = getClass().getClassLoader().loadClass(blockReceiverPath);
                    mBlockReceiver = (BlockReceiver) blockReceiverClass.newInstance();
                } catch (Throwable th) {
                    Logger.e("create BlockReceiver failed", th);
                }
            } else {
                Logger.w("block applicationInfo metaData is null, blockPath is " + blockPath);
            }
        } else {
            Logger.w("block applicationInfo is null, blockPath is " + blockPath);
        }
        if (mBlockReceiver == null) {
            return false;
        }

        BlockApplication.blockApplicationContext = mContext;
        BlockEnv.sBlockApplicationContext = mContext;

        //初始化用于动态代理的Activity路径
        initDynamicActivityPath(context, params);
        //初始化用于静态代理的Activity路径
        initStaticActivityPath(context, params);

        int theme = mBlockReceiver.getTheme();
        if (theme != 0) {
            blockApplication.setDefaultTheme(theme);
        } else {
            blockApplication.setDefaultTheme(BlockApplication.DEFAULT_THEME_RESOURCE);
        }

        List<String> protocolInterfaceNames = mBlockReceiver.getProtocolInterfaceNames();
        if (protocolInterfaceNames != null) {
            mProtocolInterface = new BaseProtocolInterface(protocolInterfaceNames);
        }

        params.put(ParamsKey.MASTER_CLASSLOADER, masterClassLoader);
        params.put(ParamsKey.BLOCK_PATH, blockPath);

        String proxyService = (String) params.get(BlockServiceServer.SERVICE_PROXY_CLASS_PATH);
        BlockEnv.sProxyService = proxyService;

        Object[] masterProxys = mConverter.convert(getClass().getClassLoader(), masterClients, mProtocolInterface);
        mBlockReceiver.onCreate(mContext, params, masterProxys);
        return true;
    }

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

    private void initDynamicActivityPath(Context hostContext, Map<Object, Object> params) {
        List<ProxyActivityInfo.ProxyActivity> standardProxyActivities = new ArrayList<>();
        List<ProxyActivityInfo.ProxyActivity> singleInstanceProxyActivities = new ArrayList<>();

        //普通模式Activity
        {
            Object defaultProxyActivityPathObj = params.get(ActivityProxyImpl.KEY_OF_DEFAULT_ACTIVITY_PROXY_CLASS_PATH);
            String defaultProxyActivityPath = null;
            boolean isDefaultProxyActivityExist = false;
            if (defaultProxyActivityPathObj instanceof CharSequence) {
                defaultProxyActivityPath = defaultProxyActivityPathObj.toString();
            }
            if (!TextUtils.isEmpty(defaultProxyActivityPath) && PackageUtils.isActivityExist(hostContext, defaultProxyActivityPath)) {
                standardProxyActivities.add(new ProxyActivityInfo.ProxyActivity(defaultProxyActivityPath, true));
                isDefaultProxyActivityExist = true;
            }

            Object configProxyActivityPathObj = params.get(ActivityProxyImpl.CONFIG_ACTIVITY_PROXY_CLASS_PATH);
            String configProxyActivityPath = null;
            boolean isConfigProxyActivityExist = false;
            if (configProxyActivityPathObj instanceof CharSequence) {
                configProxyActivityPath = configProxyActivityPathObj.toString();
            }
            if (!TextUtils.isEmpty(configProxyActivityPath) && PackageUtils.isActivityExist(hostContext, configProxyActivityPath)) {
                standardProxyActivities.add(new ProxyActivityInfo.ProxyActivity(configProxyActivityPath, true));
                isConfigProxyActivityExist = true;
            }

            if (!isDefaultProxyActivityExist && !TextUtils.isEmpty(defaultProxyActivityPath)) {
                standardProxyActivities.add(new ProxyActivityInfo.ProxyActivity(defaultProxyActivityPath));
            }
            if (!isConfigProxyActivityExist && !TextUtils.isEmpty(configProxyActivityPath)) {
                standardProxyActivities.add(new ProxyActivityInfo.ProxyActivity(configProxyActivityPath));
            }
            if (TextUtils.isEmpty(defaultProxyActivityPath) && TextUtils.isEmpty(configProxyActivityPath)) {
                String oldVersionProxyActivityPath = "com.yly.mob.activity.AppActivity";
                if (PackageUtils.isActivityExist(hostContext, oldVersionProxyActivityPath)) {
                    standardProxyActivities
                            .add(new ProxyActivityInfo.ProxyActivity(oldVersionProxyActivityPath, true));
                    params.put(ActivityProxyImpl.CONFIG_ACTIVITY_PROXY_CLASS_PATH, oldVersionProxyActivityPath);
                } else {
                    standardProxyActivities
                            .add(new ProxyActivityInfo.ProxyActivity(oldVersionProxyActivityPath));
                }
            }
            if (isDefaultProxyActivityExist) {
                params.put(ActivityProxyImpl.CONFIG_ACTIVITY_PROXY_CLASS_PATH, defaultProxyActivityPath);
            } else if (isConfigProxyActivityExist) {
                params.put(ActivityProxyImpl.CONFIG_ACTIVITY_PROXY_CLASS_PATH, configProxyActivityPathObj);
            }
        }

        //单例模式Activity
        {
            Object defaultProxyActivityPathObj = params.get(ActivityProxyImpl.KEY_OF_DEFAULT_SINGLE_INSTANCE_ACTIVITY_PROXY_CLASS_PATH);
            String defaultProxyActivityPath = null;
            boolean isDefaultProxyActivityExist = false;
            if (defaultProxyActivityPathObj instanceof CharSequence) {
                defaultProxyActivityPath = defaultProxyActivityPathObj.toString();
            }
            if (!TextUtils.isEmpty(defaultProxyActivityPath) && PackageUtils.isActivityExist(hostContext, defaultProxyActivityPath)) {
                singleInstanceProxyActivities.add(new ProxyActivityInfo.ProxyActivity(defaultProxyActivityPath, true));
                isDefaultProxyActivityExist = true;
            }

            Object configProxyActivityPathObj = params.get(ActivityProxyImpl.CONFIG_SINGLE_INSTANCE_ACTIVITY_PROXY_CLASS_PATH);
            String configProxyActivityPath = null;
            boolean isConfigProxyActivityExist = false;
            if (configProxyActivityPathObj instanceof CharSequence) {
                configProxyActivityPath = configProxyActivityPathObj.toString();
            }
            if (!TextUtils.isEmpty(configProxyActivityPath) && PackageUtils.isActivityExist(hostContext, configProxyActivityPath)) {
                singleInstanceProxyActivities.add(new ProxyActivityInfo.ProxyActivity(configProxyActivityPath, true));
                isConfigProxyActivityExist = true;
            }

            if (!isDefaultProxyActivityExist && !TextUtils.isEmpty(defaultProxyActivityPath)) {
                singleInstanceProxyActivities.add(new ProxyActivityInfo.ProxyActivity(defaultProxyActivityPath));
            }
            if (!isConfigProxyActivityExist && !TextUtils.isEmpty(configProxyActivityPath)) {
                singleInstanceProxyActivities.add(new ProxyActivityInfo.ProxyActivity(configProxyActivityPath));
            }
            if (TextUtils.isEmpty(defaultProxyActivityPath) && TextUtils.isEmpty(configProxyActivityPath)) {
                String oldVersionProxyActivityPath = "com.yly.mob.activity.ActivityProxy";
                if (PackageUtils.isActivityExist(hostContext, oldVersionProxyActivityPath)) {
                    singleInstanceProxyActivities
                            .add(new ProxyActivityInfo.ProxyActivity(oldVersionProxyActivityPath, true));
                    params.put(ActivityProxyImpl.CONFIG_SINGLE_INSTANCE_ACTIVITY_PROXY_CLASS_PATH, oldVersionProxyActivityPath);
                } else {
                    singleInstanceProxyActivities
                            .add(new ProxyActivityInfo.ProxyActivity(oldVersionProxyActivityPath));
                }
            }
            if (isDefaultProxyActivityExist) {
                params.put(ActivityProxyImpl.CONFIG_SINGLE_INSTANCE_ACTIVITY_PROXY_CLASS_PATH, defaultProxyActivityPath);
            } else if (isConfigProxyActivityExist) {
                params.put(ActivityProxyImpl.CONFIG_SINGLE_INSTANCE_ACTIVITY_PROXY_CLASS_PATH, configProxyActivityPathObj);
            }
        }
        BlockEnv.sProxyActivityInfo = new ProxyActivityInfo(standardProxyActivities, singleInstanceProxyActivities);
    }

    private void initStaticActivityPath(Context hostContext, Map params) {
        ActivityBridgeProxyInfo.sHostContext = hostContext;
        ActivityBridgeProxyInfo.sBlockContext = mContext;
        String path = null;
        Object pathObj;
        {
            pathObj = params.get(ActivityBridgeManager.NONTRANSPARENT_BRIDGE_ACTIVITY_PATH);
            if (pathObj instanceof CharSequence) {
                path = pathObj.toString();
            }
            if (path == null || !PackageUtils.isActivityExist(hostContext, path)) {
                pathObj = params.get(ActivityBridgeManager.DEFAULT_NONTRANSPARENT_BRIDGE_ACTIVITY_PATH);
                if (pathObj instanceof CharSequence) {
                    path = pathObj.toString();
                }
            }
            if (path != null && PackageUtils.isActivityExist(hostContext, path)) {
                ActivityBridgeProxyInfo.sNontransparentActivityPath = path;
            }
        }
        path = null;
        {
            pathObj = params.get(ActivityBridgeManager.TRANSPARENT_BRIDGE_ACTIVITY_PATH);
            if (pathObj instanceof CharSequence) {
                path = pathObj.toString();
            }
            if (path == null || !PackageUtils.isActivityExist(hostContext, path)) {
                pathObj = params.get(ActivityBridgeManager.DEFAULT_TRANSPARENT_BRIDGE_ACTIVITY_PATH);
                if (pathObj instanceof CharSequence) {
                    path = pathObj.toString();
                }
            }
            if (path != null && PackageUtils.isActivityExist(hostContext, path)) {
                ActivityBridgeProxyInfo.sTransparentActivityPath = path;
            }
        }
    }

    @Override
    public Object command(String command, Map params, Object... masterClients) {
        if (!TextUtils.isEmpty(command) && ActivityProxyImpl.isActivityProxyKey(command)) {
            Logger.v("IN BlockClient, getBlockProxy(), get command=" + command);
            try {
                return ActivityProxyImpl.scheduleHandleActivityProxy(mContext, mMasterClassLoader, command, params, masterClients);
            } catch (Exception e) {
                Logger.w("IN BlockClient, command(), scheduleHandleActivityProxy failed : " + e.toString());
                return null;
            }
        }
        if (mBlockReceiver != null) {
            //Object[] masterProxies = CastHandler.castObjectToTargetClassLoader(getClass().getClassLoader(), mProtocolInterface, masterClients);
            Object[] masterProxys = mConverter.convert(getClass().getClassLoader(), masterClients, mProtocolInterface);
            mBlockReceiver.command(command, params, masterProxys);
        }
        return null;
    }

    @Override
    public Object getBlockProxy(String proxyKey, Map params, Object... masterClients) {
        if (mBlockReceiver != null) {
            /*Object[] masterProxies = CastHandler
                    .castObjectToTargetClassLoaderKeepOriginal(getClass().getClassLoader(), mProtocolInterface, masterClients);*/
            Object[] masterProxys = mConverter.convert(getClass().getClassLoader(), masterClients, mProtocolInterface);
            masterProxys = keepOriginal(masterClients, masterProxys);
            Object blockProxy = mBlockReceiver.getBlockProxy(proxyKey, params, masterProxys);
            return mConverter.convert(mMasterClassLoader, blockProxy, mProtocolInterface);
            //return CastHandler.castObjectToTargetClassLoader(mMasterClassLoader, mProtocolInterface, blockProxy);
        }
        return null;
    }

    @Override
    public void setInnerProtocolInterfaceMap(Map<String, String> interfaceMap) {
        List<String> protocolInterfaceNames = Arrays.asList(IBridgeFactory.class.getName(), IActivityBridge.class.getName());
        mInnerProtocolInterface = new InnerProtocolInterface(mMasterClassLoader, protocolInterfaceNames, interfaceMap);
    }

    @Override
    public Object getInnerBlockProxy(String proxyKey, Map params, Object... masterClients) {
        if (TextUtils.isEmpty(proxyKey)) return null;
        switch (proxyKey) {
            case "activityBridgeFactory": {
                IBridgeFactory bridgeFactory = BridgeFactory.getInstance(mContext);
                return mConverter.convert(mMasterClassLoader, bridgeFactory, mInnerProtocolInterface);
            }
        }
        return null;
    }

    @Override
    public void onDestroy() {
        if (mBlockReceiver != null) {
            mBlockReceiver.onDestroy();
        }
    }


    private Object[] keepOriginal(Object[] masterClients, Object[] masterProxys) {
        if (masterClients == null || masterClients.length <= 0) {
            return masterProxys;
        }
        ParamsEntry[] paramsEntries = new ParamsEntry[masterClients.length];
        for (int index = 0; index < paramsEntries.length; index++) {
            ParamsEntry paramsEntry = new ParamsEntry();
            paramsEntry.original = masterClients[index];
            paramsEntry.proxy = masterProxys[index];
            paramsEntries[index] = paramsEntry;
        }
        return paramsEntries;
    }
}