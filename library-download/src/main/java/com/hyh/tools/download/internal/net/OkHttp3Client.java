package com.hyh.tools.download.internal.net;


import android.content.Context;

import com.hyh.tools.download.api.HttpCall;
import com.hyh.tools.download.api.HttpCallback;
import com.hyh.tools.download.api.HttpClient;
import com.hyh.tools.download.api.HttpResponse;
import com.hyh.tools.download.internal.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Administrator
 * @description
 * @data 2017/5/17
 */

public class OkHttp3Client implements HttpClient {


    private final OkHttpClient mClient;
    private String mUserAgent;

    public OkHttp3Client(Context context) {
        this.mUserAgent = Utils.getUserAgent(context);
        OkHttpClient.Builder builder = new OkHttpClient
                .Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS);
        this.mClient = builder.build();
    }

    @Override
    public HttpCall newCall(String tag, String url, long oldSize) {
        Request.Builder builder = new Request
                .Builder()
                .url(url)
                .tag(tag)
                .addHeader("User-Agent", mUserAgent);
        if (oldSize > 0) {
            builder.addHeader("RANGE", "bytes=" + oldSize + "-");//断点续传要用到的，指示下载的区间
        }
        Request okhttpRequest = builder.build();
        Call call = mClient.newCall(okhttpRequest);
        return new OkhttpCall(call);
    }

    @Override
    public HttpCall newCall(String tag, String url, long startPosition, long endPosition) {
        Request.Builder builder = new Request.Builder().url(url).tag(tag).addHeader("User-Agent", mUserAgent);

        builder.addHeader("RANGE", "bytes=" + startPosition + "-" + endPosition);//断点续传要用到的，指示下载的区间
        Request okhttpRequest = builder.build();
        Call call = mClient.newCall(okhttpRequest);
        return new OkhttpCall(call);
    }

    @Override
    public HttpResponse getHttpResponse(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Call call = mClient.newCall(request);
        Response response = call.execute();
        return new OkhttpResponse(response);
    }


    private static class OkhttpCall implements HttpCall {

        Call call;

        OkhttpCall(Call call) {
            this.call = call;
        }

        @Override
        public void enqueue(final HttpCallback httpCallback) {
            call.enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    httpCallback.onFailure(OkhttpCall.this, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    httpCallback.onResponse(OkhttpCall.this, new OkhttpResponse(response));
                }
            });
        }

        @Override
        public void cancel() {
            call.cancel();
        }

        @Override
        public boolean isExecuted() {
            return call.isExecuted();
        }

        @Override
        public boolean isCanceled() {
            return call.isCanceled();
        }
    }

    private static class OkhttpResponse implements HttpResponse {

        Response response;

        OkhttpResponse(Response response) {
            this.response = response;
        }

        @Override
        public int code() {
            return response.code();
        }

        @Override
        public InputStream inputStream() {
            return response.body().byteStream();
        }

        @Override
        public long contentLength() {
            return response.body().contentLength();
        }

        @Override
        public void close() throws IOException {
            response.close();
        }
    }
}
