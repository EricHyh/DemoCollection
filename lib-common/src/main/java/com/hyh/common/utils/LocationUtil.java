package com.hyh.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.hyh.common.log.Logger;


/**
 * @author Administrator
 * @description
 * @data 2018/6/8
 */
@SuppressLint("MissingPermission")
public class LocationUtil {

    private static LocationListener sLocationListener = new InnerLocationListener();

    public static Location getLocation(Context context) {
        Location location = null;
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {  //从gps获取经纬度
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location == null) {//当GPS信号弱没获取到位置的时候又从网络获取
                        location = getLocationFromNetwork(context);
                    }
                } else {//从网络获取经纬度
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            20 * 1000,
                            10,
                            sLocationListener,
                            ThreadUtil.getBackThreadLooper());
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    //从网络获取经纬度
    private static Location getLocationFromNetwork(Context context) {
        Location location = null;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    20 * 1000,
                    10,
                    sLocationListener,
                    ThreadUtil.getBackThreadLooper());
        }
        if (locationManager != null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return location;
    }

    private static class InnerLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Logger.d("LocationUtil onLocationChanged location = " + location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Logger.d("LocationUtil onStatusChanged provider = " + provider + ", status = " + status + ", extras = " + extras);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Logger.d("LocationUtil onProviderEnabled provider = " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Logger.d("LocationUtil onProviderDisabled provider = " + provider);
        }
    }
}