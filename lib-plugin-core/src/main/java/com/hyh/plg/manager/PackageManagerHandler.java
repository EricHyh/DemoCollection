package com.hyh.plg.manager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.TextUtils;

import com.hyh.plg.api.BlockEnv;
import com.hyh.plg.reflect.Reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Administrator
 * @description
 * @data 2020/4/27
 */
public class PackageManagerHandler implements InvocationHandler {

    private final Context mContext;
    private final Object mPackageManager;

    public PackageManagerHandler(Context context, Object iPackageManager) {
        this.mContext = context;
        this.mPackageManager = iPackageManager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        Object result = method.invoke(mPackageManager, args);
        if (result == null) {
            return Reflect.getDefaultValue(method.getReturnType());
        }
        switch (name) {
            case "getPackageInfo": {
                String packageName = findTypedParam(method.getParameterTypes(), args, String.class);
                if (TextUtils.equals(mContext.getPackageName(), packageName)) {
                    PackageInfo packageInfo = (PackageInfo) result;
                    PackageInfo blockPackageInfo = BlockEnv.sBlockPackageInfo;
                    packageInfo.versionCode = blockPackageInfo.versionCode;
                    Reflect.copyField(blockPackageInfo, packageInfo, "versionCodeMajor");
                    packageInfo.versionName = BlockEnv.sBlockPackageInfo.versionName;


                    ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                    ApplicationInfo blockApplicationInfo = blockPackageInfo.applicationInfo;

                    Bundle blockMetaData = blockApplicationInfo.metaData;
                    if (blockMetaData != null) {
                        if (applicationInfo.metaData == null) {
                            applicationInfo.metaData = blockMetaData;
                        } else {
                            applicationInfo.metaData.putAll(blockMetaData);
                        }
                    }

                    applicationInfo.labelRes = blockApplicationInfo.labelRes;
                    applicationInfo.icon = blockApplicationInfo.icon;
                    applicationInfo.logo = blockApplicationInfo.logo;
                }
                break;
            }
            case "getApplicationInfo": {
                String packageName = findTypedParam(method.getParameterTypes(), args, String.class);
                if (TextUtils.equals(mContext.getPackageName(), packageName)) {
                    ApplicationInfo applicationInfo = (ApplicationInfo) result;
                    ApplicationInfo blockApplicationInfo = BlockEnv.sBlockPackageInfo.applicationInfo;

                    Bundle blockMetaData = blockApplicationInfo.metaData;
                    if (blockMetaData != null) {
                        if (applicationInfo.metaData == null) {
                            applicationInfo.metaData = blockMetaData;
                        } else {
                            applicationInfo.metaData.putAll(blockMetaData);
                        }
                    }

                    applicationInfo.labelRes = blockApplicationInfo.labelRes;
                    applicationInfo.icon = blockApplicationInfo.icon;
                    applicationInfo.logo = blockApplicationInfo.logo;
                }
                break;
            }

        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> T findTypedParam(Class<?>[] parameterTypes, Object[] args, Class<T> type) {
        return findTypedParam(parameterTypes, args, type, 0);
    }

    @SuppressWarnings("unchecked")
    private <T> T findTypedParam(Class<?>[] parameterTypes, Object[] args, Class<T> type, int skip) {
        if (args == null || args.length == 0) return null;
        for (int index = 0; index < args.length; index++) {
            Class<?> parameterType = parameterTypes[index];
            if (Reflect.isAssignableFrom(parameterType, type)) {
                if (skip <= 0) {
                    return (T) args[index];
                } else {
                    skip--;
                }
            }
        }
        return null;
    }
}


/*
interface IPackageManager {
    PackageInfo getPackageInfo(String packageName, int flags, int userId);
    int getPackageUid(String packageName, int userId);
    int[] getPackageGids(String packageName);

    String[] currentToCanonicalPackageNames(in String[] names);
    String[] canonicalToCurrentPackageNames(in String[] names);
    PermissionInfo getPermissionInfo(String name, int flags);

    List<PermissionInfo> queryPermissionsByGroup(String group, int flags);

    PermissionGroupInfo getPermissionGroupInfo(String name, int flags);

    List<PermissionGroupInfo> getAllPermissionGroups(int flags);

    ApplicationInfo getApplicationInfo(String packageName, int flags , int userId);
    ActivityInfo getActivityInfo(in ComponentName className, int flags, int userId);
    ActivityInfo getReceiverInfo(in ComponentName className, int flags, int userId);
    ServiceInfo getServiceInfo(in ComponentName className, int flags, int userId);
    ProviderInfo getProviderInfo(in ComponentName className, int flags, int userId);
    int checkPermission(String permName, String pkgName);

    int checkUidPermission(String permName, int uid);

    boolean addPermission(in PermissionInfo info);

    void removePermission(String name);
    void grantPermission(String packageName, String permissionName);
    void revokePermission(String packageName, String permissionName);
    boolean isProtectedBroadcast(String actionName);

    int checkSignatures(String pkg1, String pkg2);

    int checkUidSignatures(int uid1, int uid2);

    String[] getPackagesForUid(int uid);

    String getNameForUid(int uid);

    int getUidForSharedUser(String sharedUserName);

    ResolveInfo resolveIntent(in Intent intent, String resolvedType, int flags, int userId);
    List<ResolveInfo> queryIntentActivities(in Intent intent,
                                            String resolvedType, int flags, int userId);
    List<ResolveInfo> queryIntentActivityOptions(
            in ComponentName caller, in Intent[] specifics,
            in String[] specificTypes, in Intent intent,
            String resolvedType, int flags, int userId);
    List<ResolveInfo> queryIntentReceivers(in Intent intent,
                                           String resolvedType, int flags, int userId);
    ResolveInfo resolveService(in Intent intent,
                               String resolvedType, int flags, int userId);
    List<ResolveInfo> queryIntentServices(in Intent intent,
                                          String resolvedType, int flags, int userId);
    ParceledListSlice getInstalledPackages(int flags, in String lastRead);

    ParceledListSlice getInstalledApplications(int flags, in String lastRead, int userId);

    List<ApplicationInfo> getPersistentApplications(int flags);
    ProviderInfo resolveContentProvider(String name, int flags, int userId);

    void querySyncProviders(inout List<String> outNames,
                            inout List<ProviderInfo> outInfo);
    List<ProviderInfo> queryContentProviders(
            String processName, int uid, int flags);
    InstrumentationInfo getInstrumentationInfo(
            in ComponentName className, int flags);
    List<InstrumentationInfo> queryInstrumentation(
            String targetPackage, int flags);

    void installPackage(in Uri packageURI, IPackageInstallObserver observer, int flags,
                        in String installerPackageName);
    void finishPackageInstall(int token);
    void setInstallerPackageName(in String targetPackage, in String installerPackageName);

    void deletePackage(in String packageName, IPackageDeleteObserver observer, int flags);
    String getInstallerPackageName(in String packageName);
    void addPackageToPreferred(String packageName);

    void removePackageFromPreferred(String packageName);

    List<PackageInfo> getPreferredPackages(int flags);
    void addPreferredActivity(in IntentFilter filter, int match,
                              in ComponentName[] set, in ComponentName activity);
    void replacePreferredActivity(in IntentFilter filter, int match,
                                  in ComponentName[] set, in ComponentName activity);
    void clearPackagePreferredActivities(String packageName);
    int getPreferredActivities(out List<IntentFilter> outFilters,
                               out List<ComponentName> outActivities, String packageName);

    void setComponentEnabledSetting(in ComponentName componentName, in int newState, in int flags, int userId);

    int getComponentEnabledSetting(in ComponentName componentName, int userId);

    void setApplicationEnabledSetting(in String packageName, in int newState, int flags, int userId);

    int getApplicationEnabledSetting(in String packageName, int userId);

    void setPackageStoppedState(String packageName, boolean stopped, int userId);

    void freeStorageAndNotify(in long freeStorageSize,
                              IPackageDataObserver observer);

    void freeStorage(in long freeStorageSize,
                     in IntentSender pi);

    void deleteApplicationCacheFiles(in String packageName, IPackageDataObserver observer);

    void clearApplicationUserData(in String packageName, IPackageDataObserver observer, int userId);

    void getPackageSizeInfo(in String packageName, IPackageStatsObserver observer);

    String[] getSystemSharedLibraryNames();

    FeatureInfo[] getSystemAvailableFeatures();
    boolean hasSystemFeature(String name);

    void enterSafeMode();
    boolean isSafeMode();
    void systemReady();
    boolean hasSystemUidErrors();

    void performBootDexOpt();

    boolean performDexOpt(String packageName);

    void updateExternalMediaStatus(boolean mounted, boolean reportStatus);
    String nextPackageToClean(String lastPackage);
    void movePackage(String packageName, IPackageMoveObserver observer, int flags);

    boolean addPermissionAsync(in PermissionInfo info);
    boolean setInstallLocation(int loc);
    int getInstallLocation();
    UserInfo createUser(in String name, int flags);
    boolean removeUser(int userId);
    void installPackageWithVerification(in Uri packageURI, in IPackageInstallObserver observer,
                                        int flags, in String installerPackageName, in Uri verificationURI,
                                        in ManifestDigest manifestDigest);
    void verifyPendingInstall(int id, int verificationCode);
    VerifierDeviceIdentity getVerifierDeviceIdentity();
    boolean isFirstBoot();
    List<UserInfo> getUsers();
    void setPermissionEnforcement(String permission, int enforcement);
    int getPermissionEnforcement(String permission);
}*/
