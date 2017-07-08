package com.eric.hyh.tools.download.internal;

import android.content.Context;

import com.eric.hyh.tools.download.IClient;
import com.eric.hyh.tools.download.api.HttpClient;
import com.eric.hyh.tools.download.internal.db.bean.TaskDBInfo;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @author Administrator
 * @description
 * @data 2017/5/17
 */

public class OkhttpServiceProxy extends ServiceDownloadProxyImpl {

    OkhttpServiceProxy(Context context, Map<Integer, IClient> clients, Executor executor, Map<String, TaskDBInfo> taskDBInfoContainer) {
        super(context, clients, executor, taskDBInfoContainer);
    }

    @Override
    protected HttpClient getHttpClient() {
        return new HttpClient_Okhttp();
    }
}
