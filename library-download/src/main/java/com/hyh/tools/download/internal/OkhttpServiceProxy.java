package com.hyh.tools.download.internal;

import android.content.Context;

import com.hyh.tools.download.IClient;
import com.hyh.tools.download.api.HttpClient;
import com.hyh.tools.download.internal.db.bean.TaskDBInfo;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @author Administrator
 * @description
 * @data 2017/5/17
 */

public class OkhttpServiceProxy extends ServiceDownloadProxyImpl {

    OkhttpServiceProxy(Context context, Map<Integer, IClient> clients, Executor executor, Map<String, TaskDBInfo> taskDBInfoContainer
            , int maxSynchronousDownloadNum) {
        super(context, clients, executor, taskDBInfoContainer, maxSynchronousDownloadNum);
    }

    @Override
    protected HttpClient getHttpClient(Context context) {
        return new HttpClient_Okhttp(context);
    }
}
