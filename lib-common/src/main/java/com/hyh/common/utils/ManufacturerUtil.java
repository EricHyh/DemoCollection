package com.hyh.common.utils;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.hyh.common.log.Logger;


public class ManufacturerUtil {

    /**
     * //http://www.jianshu.com/p/ba9347a5a05a
     * //http://blog.csdn.net/doris_d/article/details/52998237
     * //https://www.cnblogs.com/bastard/archive/2012/10/11/2720314.html
     * 获取手机的平台或厂商
     *
     * @return please se{@link ManufacturerInfo.Type}
     */
    private static int getManufacturerType() {
        String yunOsVersion = PackageUtil.getSystemProperties("ro.yunos.version");
        if (!TextUtils.isEmpty(yunOsVersion)) {
            return ManufacturerInfo.Type.ALI_YUN;
        }
        String qikuVersion = PackageUtil.getSystemProperties("ro.qiku.version.release");
        if (!TextUtils.isEmpty(qikuVersion)) {
            return ManufacturerInfo.Type.QIKU_360;
        }
        String opporomVersion = PackageUtil.getSystemProperties("ro.build.version.opporom");
        if (!TextUtils.isEmpty(opporomVersion)) {
            return ManufacturerInfo.Type.OPPO;
        }
        String vivoVersion = PackageUtil.getSystemProperties("ro.vivo.os.version");
        if (!TextUtils.isEmpty(vivoVersion)) {
            return ManufacturerInfo.Type.VIVO;
        }
        String smartisanVersion = PackageUtil.getSystemProperties("ro.smartisan.version");
        if (!TextUtils.isEmpty(smartisanVersion)) {
            return ManufacturerInfo.Type.SMARTISAN;
        }
        String display = Build.DISPLAY;
        if (display.toUpperCase().contains("FLYME")) {
            return ManufacturerInfo.Type.MEIZU;
        }
        String miuiVersion = PackageUtil.getSystemProperties("ro.miui.ui.version.name");
        if (!TextUtils.isEmpty(miuiVersion)) {
            return ManufacturerInfo.Type.XIAO_MI;
        }
        String emuiVersion = PackageUtil.getSystemProperties("ro.build.version.emui");
        if (!TextUtils.isEmpty(emuiVersion)) {
            return ManufacturerInfo.Type.HUA_WEI;
        }
        String freemeosVersion = PackageUtil.getSystemProperties("ro.build.version.freemeos");
        if (!TextUtils.isEmpty(freemeosVersion)) {
            return ManufacturerInfo.Type.FREEME_OS;
        }
        return ManufacturerInfo.Type.UNKNOWN;
    }

    public static ManufacturerInfo getManufacturerInfo(Context context) {
        String yunOsVersion = PackageUtil.getSystemProperties("ro.yunos.version");
        if (!TextUtils.isEmpty(yunOsVersion)) {
            return new ManufacturerInfo(ManufacturerInfo.Type.ALI_YUN, "阿里云");
        }
        String qikuVersion = PackageUtil.getSystemProperties("ro.qiku.version.release");
        if (!TextUtils.isEmpty(qikuVersion)) {
            return new ManufacturerInfo(ManufacturerInfo.Type.QIKU_360, "360");
        }
        String opporomVersion = PackageUtil.getSystemProperties("ro.build.version.opporom");
        if (!TextUtils.isEmpty(opporomVersion)) {
            return new ManufacturerInfo(ManufacturerInfo.Type.OPPO, "OPPO");
        }
        String vivoVersion = PackageUtil.getSystemProperties("ro.vivo.os.version");
        if (!TextUtils.isEmpty(vivoVersion)) {
            return new ManufacturerInfo(ManufacturerInfo.Type.VIVO, "VIVO");
        }
        String smartisanVersion = PackageUtil.getSystemProperties("ro.smartisan.version");
        if (!TextUtils.isEmpty(smartisanVersion)) {
            return new ManufacturerInfo(ManufacturerInfo.Type.SMARTISAN, "锤子");
        }
        String display = Build.DISPLAY;
        if (display.toUpperCase().contains("FLYME")) {
            return new ManufacturerInfo(ManufacturerInfo.Type.MEIZU, "魅族");
        }
        String miuiVersion = PackageUtil.getSystemProperties("ro.miui.ui.version.name");
        if (!TextUtils.isEmpty(miuiVersion)) {
            return new ManufacturerInfo(ManufacturerInfo.Type.XIAO_MI, "小米");
        }
        String emuiVersion = PackageUtil.getSystemProperties("ro.build.version.emui");
        if (!TextUtils.isEmpty(emuiVersion)) {
            return new ManufacturerInfo(ManufacturerInfo.Type.HUA_WEI, "华为");
        }
        String freemeosVersion = PackageUtil.getSystemProperties("ro.build.version.freemeos");
        if (!TextUtils.isEmpty(freemeosVersion)) {
            return new ManufacturerInfo(ManufacturerInfo.Type.FREEME_OS, "FREEME");
        }
        return new ManufacturerInfo(ManufacturerInfo.Type.UNKNOWN, "未知");
    }

    public static boolean is360OS(Context context) {
        String qikuVersion = PackageUtil.getSystemProperties("ro.qiku.version.release");
        Logger.d("qikuVersion is " + qikuVersion);
        return !TextUtils.isEmpty(qikuVersion);
    }

    public static boolean isFreemeOS(Context context) {
        String freemeosVersion = PackageUtil.getSystemProperties("ro.build.version.freemeos");
        String freemeosLabel = PackageUtil.getSystemProperties("ro.build.freemeos_label");
        Logger.d("freemeosVersion:" + freemeosVersion + ", freemeosLabel:" + freemeosLabel);
        return !TextUtils.isEmpty(freemeosVersion) || !TextUtils.isEmpty(freemeosLabel);
    }

    public static boolean isAliyunOS(Context context) {
        //获取阿里云系统版本
        String aliyun_version = PackageUtil.getSystemProperties("ro.yunos.version");
        Logger.d("aliyun_version:" + aliyun_version);
        return !TextUtils.isEmpty(aliyun_version);
    }

    public static boolean isVIVO(Context context) {
        String vivoVersion = PackageUtil.getSystemProperties("ro.vivo.os.version");
        return !TextUtils.isEmpty(vivoVersion);
    }

    public static boolean isOPPO(Context context) {
        String opporomVersion = PackageUtil.getSystemProperties("ro.build.version.opporom");
        return !TextUtils.isEmpty(opporomVersion);
    }

    public static boolean isLetv(Context context) {
        String letvVersion = PackageUtil.getSystemProperties("ro.letv.release.version");
        return !TextUtils.isEmpty(letvVersion);
    }
}