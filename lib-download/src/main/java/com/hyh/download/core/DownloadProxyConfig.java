package com.hyh.download.core;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Eric_He on 2018/12/23.
 */

public class DownloadProxyConfig implements Parcelable {

    private int maxSyncDownloadNum;

    public DownloadProxyConfig(int maxSyncDownloadNum) {
        this.maxSyncDownloadNum = maxSyncDownloadNum;
    }

    public int getMaxSyncDownloadNum() {
        return maxSyncDownloadNum;
    }

    protected DownloadProxyConfig(Parcel in) {
        maxSyncDownloadNum = in.readInt();
    }

    public static final Creator<DownloadProxyConfig> CREATOR = new Creator<DownloadProxyConfig>() {
        @Override
        public DownloadProxyConfig createFromParcel(Parcel in) {
            return new DownloadProxyConfig(in);
        }

        @Override
        public DownloadProxyConfig[] newArray(int size) {
            return new DownloadProxyConfig[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(maxSyncDownloadNum);
    }
}
