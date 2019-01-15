package com.hyh.download.core;

import android.os.Parcel;
import android.os.Parcelable;

import com.hyh.download.FileRequest;

/**
 * Created by Eric_He on 2019/1/15.
 */

public class RequestInfo implements Parcelable{

    String resKey;

    String url;

    boolean needVerifyUrl;

    boolean byMultiThread;

    boolean wifiAutoRetry;

    boolean permitRetryInMobileData;

    boolean permitRetryInvalidFileTask;

    boolean permitRecoverTask;

    boolean forceDownload;

    String fileDir;

    String fileName;

    String tag;

    private RequestInfo() {
    }

    protected RequestInfo(Parcel in) {
        resKey = in.readString();
        url = in.readString();
        needVerifyUrl = in.readByte() != 0;
        byMultiThread = in.readByte() != 0;
        wifiAutoRetry = in.readByte() != 0;
        permitRetryInMobileData = in.readByte() != 0;
        permitRetryInvalidFileTask = in.readByte() != 0;
        permitRecoverTask = in.readByte() != 0;
        forceDownload = in.readByte() != 0;
        fileDir = in.readString();
        fileName = in.readString();
        tag = in.readString();
    }

    public static final Creator<RequestInfo> CREATOR = new Creator<RequestInfo>() {
        @Override
        public RequestInfo createFromParcel(Parcel in) {
            return new RequestInfo(in);
        }

        @Override
        public RequestInfo[] newArray(int size) {
            return new RequestInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(resKey);
        dest.writeString(url);
        dest.writeByte((byte) (needVerifyUrl ? 1 : 0));
        dest.writeByte((byte) (byMultiThread ? 1 : 0));
        dest.writeByte((byte) (wifiAutoRetry ? 1 : 0));
        dest.writeByte((byte) (permitRetryInMobileData ? 1 : 0));
        dest.writeByte((byte) (permitRetryInvalidFileTask ? 1 : 0));
        dest.writeByte((byte) (permitRecoverTask ? 1 : 0));
        dest.writeByte((byte) (forceDownload ? 1 : 0));
        dest.writeString(fileDir);
        dest.writeString(fileName);
        dest.writeString(tag);
    }

    public static RequestInfo create(FileRequest fileRequest) {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.resKey = fileRequest.key();
        requestInfo.url = fileRequest.url();
        requestInfo.needVerifyUrl = fileRequest.needVerifyUrl();
        requestInfo.byMultiThread = fileRequest.byMultiThread();
        requestInfo.wifiAutoRetry = fileRequest.wifiAutoRetry();
        requestInfo.permitRetryInMobileData = fileRequest.permitRetryInMobileData();
        requestInfo.permitRetryInvalidFileTask = fileRequest.permitRetryInvalidFileTask();
        requestInfo.permitRecoverTask = fileRequest.permitRecoverTask();
        requestInfo.forceDownload = fileRequest.forceDownload();
        requestInfo.fileDir = fileRequest.fileDir();
        requestInfo.fileName = fileRequest.fileName();
        requestInfo.tag = fileRequest.tag();
        return requestInfo;
    }
}
