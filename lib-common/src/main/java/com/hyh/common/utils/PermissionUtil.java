/*
 * 文件名: PermissionUtil.java
 * 描述: 判断文件写入权限
 * 修改人: [zhengge]
 * 修改日期: 2018-05-02
 * 修改内容: 新增
 */
package com.hyh.common.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.hyh.common.log.Logger;


/**
 * Class: PermissionUtil
 * 功能描述：日志权限工具类
 *
 * @author [zhengge]
 * @version 1.0, 2018.05.04
 * @see [相关的类或者方法]
 */
public class PermissionUtil {

    public static boolean hasWriteSdCardPermission(Context context) {
        int i = context.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return i == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkPermission(Context context, String permission) {
        if ((context == null) || (TextUtils.isEmpty(permission))) {
            return false;
        }
        try {
            PackageManager pm = context.getPackageManager();
            int result = pm.checkPermission(permission, context.getPackageName());
            return result == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Boolean isPermissionsRegistered(Context context, String... permissions) {
        if (permissions == null || permissions.length <= 0) return true;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] requestedPermissions = packageInfo.requestedPermissions;
            if (requestedPermissions == null || requestedPermissions.length < permissions.length) {
                return false;
            }
            for (String permission : permissions) {
                boolean isRegistered = false;
                for (String requestedPermission : requestedPermissions) {
                    if (TextUtils.equals(permission, requestedPermission)) {
                        isRegistered = true;
                        break;
                    }
                }
                if (!isRegistered) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            Logger.d("PermissionUtil isPermissionsRegistered execute failed ", e);
        }
        return null;
    }
}