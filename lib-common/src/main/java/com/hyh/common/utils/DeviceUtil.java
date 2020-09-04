package com.hyh.common.utils;


import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.LocaleList;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.webkit.WebSettings;

import com.hyh.common.log.Logger;

import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

/**
 * @author Administrator
 * @description
 * @data 2018/3/1
 */

public class DeviceUtil {


    //https://www.jianshu.com/p/c31e4f94d64c
    //https://blog.csdn.net/fengyifei11228/article/details/45919797
    @SuppressLint({"HardwareIds", "权限检查提醒", "MissingPermission"})
    public static String getImei(Context context) {
        String imei = getSystemImei(context);

        // 如果meid是 14 位，则将其计算成15位
        if (!TextUtils.isEmpty(imei)) {
            if (imei.length() == 14 && TextUtils.isDigitsOnly(imei)) {
                imei = convertMeid2Imei(imei);
            }
        }
        Logger.d("DeviceUtil getImei imei = " + imei);
        return imei;
    }

    @SuppressLint({"HardwareIds", "权限检查提醒", "MissingPermission"})
    public static String getSystemImei(Context context) {
        String imei = "";
        try {
            TelephonyManager telePhonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telePhonyManager == null) {
                return imei;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imei = getImei(telePhonyManager, 0, -1, 1);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                imei = getDeviceId(telePhonyManager, 0, -1, 1);
            } else {
                imei = telePhonyManager.getDeviceId();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        // 如果meid是 14 位，则将其计算成15位
        if (!TextUtils.isEmpty(imei)) {
            if (imei.length() == 14 && TextUtils.isDigitsOnly(imei)) {
                imei = convertMeid2Imei(imei);
            }
        }
        Logger.d("DeviceUtil getImei imei = " + imei);
        return imei;
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    private static String getImei(TelephonyManager telephonyManager, int... slotIndexs) {
        String imei = "";
        if (telephonyManager == null) {
            return imei;
        }
        for (int slotIndex : slotIndexs) {
            try {
                if (slotIndex < 0) {
                    imei = telephonyManager.getImei();
                } else {
                    imei = telephonyManager.getImei(slotIndex);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(imei) && imei.length() >= 14) {
                return imei;
            }
        }
        return imei;
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    private static String getDeviceId(TelephonyManager telephonyManager, int... slotIndexs) {
        String imei = "";
        for (int slotIndex : slotIndexs) {
            try {
                if (slotIndex < 0) {
                    imei = telephonyManager.getDeviceId();
                } else {
                    imei = telephonyManager.getDeviceId(slotIndex);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(imei) && imei.length() >= 14) {
                return imei;
            }
        }
        return imei;
    }

    private static String convertMeid2Imei(String imei) {
        char[] imeiChar = imei.toCharArray();
        int resultInt = 0;
        for (int i = 0; i < imeiChar.length; i++) {
            int a = Integer.parseInt(String.valueOf(imeiChar[i]));
            i++;
            final int temp = Integer.parseInt(String.valueOf(imeiChar[i])) * 2;
            final int b = temp < 10 ? temp : temp - 9;
            resultInt += a + b;
        }
        resultInt %= 10;
        resultInt = resultInt == 0 ? 0 : 10 - resultInt;
        return imei + resultInt;

    }


    /**
     * 获取mac地址
     *
     * @return String 如：aa:bb:cc:dd:ee:gg  不存在则返回空字符串
     */
    @SuppressLint({"HardwareIds", "MissingPermission"})
    public static String getMacAddress(Context context) {
        String macAddress = "";
        try {
            String wifiInterfaceName = "wlan0";
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.getName().equalsIgnoreCase(wifiInterfaceName)) {
                    byte[] addr = networkInterface.getHardwareAddress();
                    if (addr == null || addr.length == 0) {
                        return "";
                    }
                    StringBuilder buf = new StringBuilder();
                    for (byte b : addr) {
                        buf.append(String.format("%02X:", b));
                    }
                    if (buf.length() > 0) {
                        buf.deleteCharAt(buf.length() - 1);
                    }
                    macAddress = buf.toString();
                    break;
                }
            }
        } catch (Throwable e) {
            macAddress = "";
        }
        if (TextUtils.isEmpty(macAddress)) {
            try {
                WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo connectionInfo;
                if (wifi != null) {
                    connectionInfo = wifi.getConnectionInfo();
                    macAddress = connectionInfo.getMacAddress();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!"02:00:00:00:00:00".equals(macAddress)) {
        }
        return macAddress;
    }


    @SuppressLint("MissingPermission")
    public static String getWifiRouterMac(Context context) {
        String wifiRouterMac = null;
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> wifiList;
            if (wifiManager != null) {
                wifiList = wifiManager.getScanResults();
                WifiInfo info = wifiManager.getConnectionInfo();
                if (wifiList != null && !wifiList.isEmpty() && info != null && info.getBSSID() != null) {
                    for (int i = 0; i < wifiList.size(); i++) {
                        ScanResult result = wifiList.get(i);
                        if (result == null || result.BSSID == null) {
                            continue;
                        }
                        if (TextUtils.equals(info.getBSSID(), result.BSSID)) {
                            wifiRouterMac = result.BSSID;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wifiRouterMac;
    }


    public static String getAndroidId(Context context) {
        try {
            return Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }


    @SuppressLint({"权限检查提醒", "MissingPermission"})
    public static int getLac(Context context) {
        int lac = -1;
        try {
            TelephonyManager telePhonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
            CellLocation cellLocation = null;
            if (telePhonyManager != null) {
                cellLocation = telePhonyManager.getCellLocation();
            }
            if (cellLocation instanceof GsmCellLocation) {
                lac = ((GsmCellLocation) cellLocation).getLac();
            } else if (cellLocation instanceof CdmaCellLocation) {
                lac = ((CdmaCellLocation) cellLocation).getNetworkId();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return lac;
    }

    @SuppressLint({"权限检查提醒", "MissingPermission"})
    public static int getCid(Context context) {
        int cid = -1;
        try {
            TelephonyManager telePhonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
            CellLocation cellLocation = null;
            if (telePhonyManager != null) {
                cellLocation = telePhonyManager.getCellLocation();
            }
            if (cellLocation instanceof GsmCellLocation) {
                cid = ((GsmCellLocation) cellLocation).getCid();
            } else if (cellLocation instanceof CdmaCellLocation) {
                cid = ((CdmaCellLocation) cellLocation).getBaseStationId();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return cid;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static int getCarrierOperator(Context context) {
        int result = CarrierOperator.UNKNOWN;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                String subscriberId = telephonyManager.getSubscriberId();
                if (!TextUtils.isEmpty(subscriberId)) {
                    if (subscriberId.startsWith("46000") || subscriberId.startsWith("46002")) {
                        //因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号 //中国移动
                        result = CarrierOperator.CHINA_MOBILE;
                    } else if (subscriberId.startsWith("46001")) {
                        //中国联通
                        result = CarrierOperator.CHINA_UNICOM;
                    } else if (subscriberId.startsWith("46003")) {
                        //中国电信
                        result = CarrierOperator.CHINA_TELECOM;
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getUserAgent(Context context) {
        long startTime = System.currentTimeMillis();
        long endTime;
        String userAgent = null;
        if (!TextUtils.isEmpty(userAgent)) {
            endTime = System.currentTimeMillis();
            Logger.d("get cache ua, use time:" + (endTime - startTime) + "ms");
            return userAgent;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                userAgent = WebSettings.getDefaultUserAgent(context);
            } catch (Exception e) {
                userAgent = System.getProperty("http.agent");
            }
        } else {
            userAgent = System.getProperty("http.agent");
        }
        StringBuilder sb = new StringBuilder();
        try {
            for (int i = 0, length = userAgent.length(); i < length; i++) {
                char c = userAgent.charAt(i);
                if (c <= '\u001f' || c >= '\u007f') {
                    sb.append(String.format("\\u%04x", (int) c));
                } else {
                    sb.append(c);
                }
            }
            userAgent = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(userAgent)) {
        }
        endTime = System.currentTimeMillis();
        Logger.d("get ua, use time:" + (endTime - startTime) + "ms");
        return userAgent;
    }

    public static String getSSID(Context context) {
        String ssid = null;
        if (!PermissionUtil.checkPermission(context, "android.permission.ACCESS_WIFI_STATE")) {
            return null;
        }
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                @SuppressLint("MissingPermission") WifiInfo info = wifiManager.getConnectionInfo();
                if (info != null) {
                    ssid = info.getSSID();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ssid;
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
    public static String getICCID(Context context) {
        String iccid = null;
        if (!PermissionUtil.checkPermission(context, "android.permission.READ_PHONE_STATE")) {
            return null;
        }
        try {
            TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telManager != null) {
                iccid = telManager.getSimSerialNumber();  //取出ICCID:集成电路卡识别码（固化在手机SIM卡中,就是SIM卡的序列号）
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return iccid;
    }

    public static String getBSSID(Context context) {
        String bssid = null;
        if (!PermissionUtil.checkPermission(context, "android.permission.ACCESS_WIFI_STATE")) {
            return null;
        }
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            @SuppressLint("MissingPermission") WifiInfo info = wifiManager.getConnectionInfo();
            if (info != null) {
                bssid = info.getBSSID();
            }
        }
        return bssid;
    }

    public static int getOrientation(Context context) {
        return context.getResources().getConfiguration().orientation;
    }

    public static String getLanguage() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        return locale.getLanguage();
    }

    public static String getOSVersion() {
        return Build.VERSION.RELEASE;
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
    public static String getBuildSerial() {
        String buildSerial;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                buildSerial = Build.getSerial();
            } else {
                buildSerial = Build.SERIAL;
            }
        } catch (Exception e) {
            buildSerial = Build.SERIAL;
        }
        return buildSerial;
    }

    public static String getCPU_ABI() {
        String cpuAbi = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String[] supportedAbis = Build.SUPPORTED_ABIS;
            if (supportedAbis != null && supportedAbis.length > 0) {
                cpuAbi = supportedAbis[0];
            }
        }
        if (cpuAbi == null) {
            cpuAbi = Build.CPU_ABI;
        }
        return cpuAbi;
    }

    public static String getMccMnc(Context context) {
        String mccmnc = "46000";
        try {
            TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telManager != null) {
                String networkOperator = telManager.getNetworkOperator();
                if (!TextUtils.isEmpty(networkOperator) && networkOperator.length() >= 5) {
                    mccmnc = telManager.getNetworkOperator().substring(0, 5);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mccmnc;
    }

    public static String getMcc(Context context) {
        String mcc = "460";
        try {
            TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telManager != null) {
                String networkOperator = telManager.getNetworkOperator();
                if (!TextUtils.isEmpty(networkOperator) && networkOperator.length() >= 3) {
                    mcc = telManager.getNetworkOperator().substring(0, 3);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mcc;
    }

    public static String getMnc(Context context) {
        String mnc = "00";
        try {
            TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telManager != null) {
                String networkOperator = telManager.getNetworkOperator();
                if (!TextUtils.isEmpty(networkOperator) && networkOperator.length() >= 5) {
                    mnc = telManager.getNetworkOperator().substring(3, 5);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mnc;
    }

    public static class CarrierOperator {
        public static final int UNKNOWN = 0;
        public static final int CHINA_MOBILE = 1;
        public static final int CHINA_UNICOM = 2;
        public static final int CHINA_TELECOM = 3;
    }
}