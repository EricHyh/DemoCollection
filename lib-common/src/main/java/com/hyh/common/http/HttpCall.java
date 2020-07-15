package com.hyh.common.http;

import android.text.TextUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Administrator
 * @description
 * @data 2019/1/3
 */

public class HttpCall implements Cloneable {

    private final Object mLock = new Object();

    ThreadPoolExecutor executor;

    String url;

    String method;

    Map<String, String> urlParams;

    Map<String, List<String>> requestHeaders;

    RequestBody requestBody;

    int connectTimeout;

    int readTimeout;

    Object tag;

    private volatile int redirectTimes;

    private volatile boolean cancel;

    private volatile boolean executedOrEnqueued;

    public Response execute() throws IOException {
        synchronized (mLock) {
            if (cancel) {
                throw new IOException("call has been canceled");
            }
            if (executedOrEnqueued) {
                throw new IOException("call has been executed");
            }
            executedOrEnqueued = true;
        }
        String url = addParams(this.url, urlParams);
        String method = this.method;
        if (TextUtils.isEmpty(method)) {
            method = (requestBody == null) ? "GET" : "POST";
        }
        RawResponse rawResponse = getRawResponse(url, method, requestHeaders, null, requestBody);

        synchronized (mLock) {
            if (cancel) {
                throw new IOException("call has been canceled");
            }
        }

        return new Response(rawResponse, url);
    }

    public void enqueue(HttpCallback callback) {
        boolean isFailure = false;
        Throwable throwable = null;
        synchronized (mLock) {
            if (cancel) {
                isFailure = true;
                throwable = new IOException("call has been canceled");
            }
            if (!cancel && executedOrEnqueued) {
                isFailure = true;
                throwable = new IOException("call has been canceled");
            }
            executedOrEnqueued = true;
        }
        if (isFailure) {
            if (callback != null) {
                callback.onFailure(this, throwable);
            }
            return;
        }
        executor.execute(new HttpTask(callback));
    }

    public Object getTag() {
        return tag;
    }

    public void cancel() {
        synchronized (mLock) {
            cancel = true;
        }
    }

    @Override
    public HttpCall clone() {
        HttpCall httpCall = new HttpCall();
        httpCall.executor = this.executor;
        httpCall.url = this.url;
        httpCall.method = this.method;
        httpCall.urlParams = this.urlParams;
        httpCall.requestHeaders = this.requestHeaders;
        httpCall.requestBody = this.requestBody;
        httpCall.connectTimeout = this.connectTimeout;
        httpCall.readTimeout = this.readTimeout;
        httpCall.tag = this.tag;
        return httpCall;
    }

    private String addParams(String url, Map<String, String> urlParams) {
        if (urlParams == null || urlParams.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder();
        Set<Map.Entry<String, String>> entrySet = urlParams.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value == null) {
                continue;
            }
            sb.append(key).append("=").append(value).append("&");
        }
        if (sb.length() == 0) {
            return url;
        }
        String paramStr = sb.substring(0, sb.length() - 1);
        if (url.endsWith("?")) {
            url = url + paramStr;
        } else {
            url = url + "?" + paramStr;
        }
        return url;
    }

    private RawResponse getRawResponse(String url, String method,
                                       Map<String, List<String>> requestHeaders,
                                       List<String> headerFilters,
                                       RequestBody requestBody) throws IOException {
        HttpURLConnection connection = getConnection(url, method, requestHeaders, headerFilters, requestBody);
        int responseCode = connection.getResponseCode();
        if (isSuccessful(responseCode)) {
            return new RawResponse(connection, url, responseCode, connection.getInputStream());
        } else if (isRedirect(responseCode)) {
            if (++redirectTimes >= HappyHttp.Net.MAX_REDIRECT_TIMES) {//如果重定向次数达到最大允许次数就不再继续处理下去了，直接抛异常
                throw new IOException("redirect times reach max");
            }
            RawResponse rawResponse = new RawResponse(connection, url, responseCode, null);
            String location = connection.getHeaderField("Location");
            if (TextUtils.isEmpty(location)) {
                String format = String.format(Locale.ENGLISH, "receive %d (redirect) but the location is null with " + "response [%s]",
                        responseCode, connection.getHeaderFields());
                throw new IOException(format);
            } else {
                return handleRedirect(rawResponse, location);
            }
        } else {
            return new RawResponse(connection, url, responseCode, null);
        }
    }

    private RawResponse handleRedirect(RawResponse lastResponse, String location) throws IOException {
        int code = lastResponse.code();
        RawResponse redirectResponse = lastResponse;
        switch (code) {
            case HttpURLConnection.HTTP_MULT_CHOICE:
            case HttpURLConnection.HTTP_MOVED_PERM:
            case HttpURLConnection.HTTP_MOVED_TEMP:
            case HttpURLConnection.HTTP_SEE_OTHER: {
                List<String> headerFilters = Arrays.asList("Transfer-Encoding", "Content-Length", "Content-Type");
                RawResponse response = getRawResponse(location, "GET", requestHeaders, headerFilters, null);
                response.lastResponse = lastResponse;
                redirectResponse = response;
                break;
            }
            case HappyHttp.Net.ResponseCode.HTTP_TEMPORARY_REDIRECT:
            case HappyHttp.Net.ResponseCode.HTTP_PERMANENT_REDIRECT: {
                RawResponse response = getRawResponse(location, lastResponse.method(), requestHeaders, null, requestBody);
                response.lastResponse = lastResponse;
                redirectResponse = response;
                break;
            }
        }
        return redirectResponse;
    }

    private HttpURLConnection getConnection(String url, String method,
                                            Map<String, List<String>> requestHeaders,
                                            List<String> headerFilters,
                                            RequestBody requestBody) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setDoInput(true);
        connection.setRequestMethod(method);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);

        addHeaders(connection, requestHeaders, headerFilters);

        if (requestBody != null) {
            connection.setDoOutput(true);
            connection.addRequestProperty("Content-Type", requestBody.contentType());
            requestBody.writeTo(connection.getOutputStream());
        }
        return connection;
    }

    private void addHeaders(HttpURLConnection connection, Map<String, List<String>> requestHeaders, List<String> headerFilters) {
        connection.addRequestProperty("Accept-Encoding", "identity");
        connection.addRequestProperty("Connection", "keep-alive");
        if (requestHeaders == null || requestHeaders.isEmpty()) {
            return;
        }
        Set<Map.Entry<String, List<String>>> entrySet = requestHeaders.entrySet();
        for (Map.Entry<String, List<String>> entry : entrySet) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            if (values == null || values.isEmpty()) {
                continue;
            }
            if (isFilter(headerFilters, key)) {
                continue;
            }
            for (String value : values) {
                connection.addRequestProperty(key, value);
            }
        }
    }

    private boolean isFilter(List<String> filterNames, String name) {
        if (filterNames == null || filterNames.isEmpty()) {
            return false;
        }
        for (String filterName : filterNames) {
            if (filterName.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSuccessful(int responseCode) {
        return responseCode >= 200 && responseCode < 300;
    }

    private boolean isRedirect(int responseCode) {
        return responseCode == HttpURLConnection.HTTP_MULT_CHOICE
                || responseCode == HttpURLConnection.HTTP_MOVED_PERM
                || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                || responseCode == HttpURLConnection.HTTP_SEE_OTHER
                || responseCode == HappyHttp.Net.ResponseCode.HTTP_TEMPORARY_REDIRECT
                || responseCode == HappyHttp.Net.ResponseCode.HTTP_PERMANENT_REDIRECT;
    }


    private class HttpTask implements Runnable {

        HttpCallback callback;

        HttpTask(HttpCallback callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            boolean isCancel = false;
            Throwable throwable = null;
            synchronized (mLock) {
                if (cancel) {
                    isCancel = true;
                    throwable = new IOException("call has been canceled");
                }
            }
            if (isCancel) {
                if (callback != null) {
                    callback.onFailure(HttpCall.this, throwable);
                }
                return;
            }

            String url = addParams(HttpCall.this.url, urlParams);
            String method = HttpCall.this.method;
            if (TextUtils.isEmpty(method)) {
                method = (requestBody == null) ? "GET" : "POST";
            }
            RawResponse rawResponse = null;
            try {
                rawResponse = getRawResponse(url, method, requestHeaders, null, requestBody);
            } catch (IOException e) {
                throwable = e;
            }

            if (callback != null) {
                synchronized (mLock) {
                    if (cancel) {
                        isCancel = true;
                        throwable = new IOException("call has been canceled");
                    }
                }
                if (isCancel) {
                    callback.onFailure(HttpCall.this, throwable);
                } else {
                    if (rawResponse != null) {
                        callback.onResponse(HttpCall.this, new Response(rawResponse, HttpCall.this.url));
                    } else {
                        callback.onFailure(HttpCall.this, throwable);
                    }
                }
            }
        }
    }
}