package com.hyh.plg.android;

import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2019/9/3
 */

public class ProxyActivityInfo {

    public final List<ProxyActivity> standardProxyActivities;
    public final List<ProxyActivity> singleInstanceProxyActivities;

    public ProxyActivityInfo(List<ProxyActivity> standardProxyActivities, List<ProxyActivity> singleInstanceProxyActivities) {
        this.standardProxyActivities = standardProxyActivities;
        this.singleInstanceProxyActivities = singleInstanceProxyActivities;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("standardProxyActivities:{");
        if (standardProxyActivities != null && !standardProxyActivities.isEmpty()) {
            int size = standardProxyActivities.size();
            for (int index = 0; index < size; index++) {
                ProxyActivity proxyActivity = standardProxyActivities.get(index);
                if (index < size - 1) {
                    sb.append("[name:").append(proxyActivity.activityName).append(", ")
                            .append("registered:").append(proxyActivity.isManifestRegistered).append("]")
                            .append(", ");
                } else {
                    sb.append("[name:").append(proxyActivity.activityName).append(", ")
                            .append("registered:").append(proxyActivity.isManifestRegistered).append("]");
                }
            }
        }
        sb.append("};").append("\r\n");
        sb.append("singleInstanceProxyActivities:{");
        if (singleInstanceProxyActivities != null && !singleInstanceProxyActivities.isEmpty()) {
            int size = singleInstanceProxyActivities.size();
            for (int index = 0; index < size; index++) {
                ProxyActivity proxyActivity = singleInstanceProxyActivities.get(index);
                if (index < size - 1) {
                    sb.append("[name:").append(proxyActivity.activityName).append(", ")
                            .append("registered:").append(proxyActivity.isManifestRegistered).append("]")
                            .append(", ");
                } else {
                    sb.append("[name:").append(proxyActivity.activityName).append(", ")
                            .append("registered:").append(proxyActivity.isManifestRegistered).append("]");
                }
            }
        }
        sb.append("}.");
        return sb.toString();
    }

    public static class ProxyActivity {

        public String activityName;
        public boolean isManifestRegistered;

        public ProxyActivity(String activityName) {
            this(activityName, false);
        }

        public ProxyActivity(String activityName, boolean isManifestRegistered) {
            this.activityName = activityName;
            this.isManifestRegistered = isManifestRegistered;
        }
    }
}