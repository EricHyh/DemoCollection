package com.hyh.download.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.WebSettings;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Administrator
 * @description
 * @data 2018/12/15
 */

public class NetworkHelper {

    private static final int CHUNKED_CONTENT_LENGTH = -1;

    private static final Pattern CONTENT_DISPOSITION_QUOTED_PATTERN = Pattern.compile("attachment;\\s*filename\\s*=\\s*\"([^\"]*)\"");
    // no note
    private static final Pattern CONTENT_DISPOSITION_NON_QUOTED_PATTERN = Pattern.compile("attachment;\\s*filename\\s*=\\s*(.*)");


    public static long parseContentLengthFromContentRange(String contentRange) {
        if (contentRange == null || contentRange.length() == 0) return CHUNKED_CONTENT_LENGTH;
        final String pattern = "bytes (\\d+)-(\\d+)/\\d+";
        try {
            final Pattern r = Pattern.compile(pattern);
            final Matcher m = r.matcher(contentRange);
            if (m.find()) {
                final long rangeStart = Long.parseLong(m.group(1));
                final long rangeEnd = Long.parseLong(m.group(2));
                return rangeEnd - rangeStart + 1;
            }
        } catch (Exception e) {
            L.w("parse content-length from content-range failed " + e);
        }
        return CHUNKED_CONTENT_LENGTH;
    }

    public static String parseContentDisposition(String contentDisposition) {
        if (contentDisposition == null) {
            return null;
        }
        try {
            String fileName = null;
            Matcher m = CONTENT_DISPOSITION_QUOTED_PATTERN.matcher(contentDisposition);
            if (m.find()) {
                fileName = m.group(1);
            } else {
                m = CONTENT_DISPOSITION_NON_QUOTED_PATTERN.matcher(contentDisposition);
                if (m.find()) {
                    fileName = m.group(1);
                }
            }
            if (fileName != null && fileName.contains("../")) {
                L.d("The filename [" + fileName + "] from"
                        + " the response is not allowable, because it contains '../', which "
                        + "can raise the directory traversal vulnerability");
                return null;
            }
            return fileName;
        } catch (IllegalStateException ex) {
            // This function is defined as returning null when it can't parse the header
        }
        return null;
    }

    public static boolean isWifiEnv(Context context) {
        try {
            ConnectivityManager connectMgr =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            if (connectMgr != null) {
                networkInfo = connectMgr.getActiveNetworkInfo();
            }
            if (networkInfo != null) {
                return networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isNetEnv(Context context) {
        try {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = null;
            if (manager != null) {
                info = manager.getActiveNetworkInfo();
            }
            return info != null && info.isAvailable();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getUserAgent(Context context) {
        String userAgent = null;
        SharedPreferences preferences = context.getSharedPreferences(Constants.Preference.SHARE_NAME, Context.MODE_PRIVATE);
        long cacheTimeMillis = preferences.getLong(Constants.Preference.Key.CACHE_USER_AGENT_TIME_MILLIS, 0);
        if (isSameDay(cacheTimeMillis, System.currentTimeMillis())) {
            userAgent = preferences.getString(Constants.Preference.Key.USER_AGENT, null);
        }
        if (!TextUtils.isEmpty(userAgent)) {
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
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.Preference.Key.USER_AGENT, userAgent);
            editor.putLong(Constants.Preference.Key.CACHE_USER_AGENT_TIME_MILLIS, System.currentTimeMillis());
            editor.apply();
        }
        return userAgent;
    }

    private static boolean isSameDay(long timeMillis1, long timeMillis2) {
        if (timeMillis1 > 0 && timeMillis2 > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeMillis1);
            int year1 = calendar.get(Calendar.YEAR);
            int month1 = calendar.get(Calendar.MONTH);
            int day1 = calendar.get(Calendar.DATE);

            calendar.setTimeInMillis(timeMillis2);
            int year2 = calendar.get(Calendar.YEAR);
            int month2 = calendar.get(Calendar.MONTH);
            int day2 = calendar.get(Calendar.DATE);
            return year1 == year2 && month1 == month2 && day1 == day2;
        } else {
            return false;
        }
    }

}
