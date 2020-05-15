package com.hyh.plg.android.ams;

public class ProxyActivityMap {

    public String source;
    public Class map;
    public int mapLaunchMode;

    @Override
    public String toString() {
        return "ProxyActivityMap{" +
                "source='" + source + '\'' +
                ", map='" + map + '\'' +
                ", mapLaunchMode=" + mapLaunchMode +
                '}';
    }
}