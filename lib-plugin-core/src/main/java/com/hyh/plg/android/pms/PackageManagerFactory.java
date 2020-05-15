package com.hyh.plg.android.pms;

import android.app.Application;
import android.content.pm.PackageManager;

/**
 * Created by tangdongwei on 2018/12/11.
 */
public class PackageManagerFactory {

    public static PackageManager create(Application blockApplication, PackageManager realPackageManager) {
//        return new BlockPackageManagerStaticProxy(realPackageManager);
        return new BlockPackageManagerDynaProxy().create(blockApplication, realPackageManager);
    }

}
