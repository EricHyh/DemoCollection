package com.eric.hyh.tools.download.internal;

import android.content.Context;

import com.eric.hyh.tools.download.api.HttpClient;
import com.eric.hyh.tools.download.internal.db.bean.TaskDBInfo;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @author Administrator
 * @description
 * @data 2017/5/17
 */

public class OkhttpServiceAgent extends ServiceDownloadAgentImpl {

    OkhttpServiceAgent(Context context, Executor executor, Map<String, TaskDBInfo> taskDBInfoContainer) {
        super(context, executor, taskDBInfoContainer);
    }

    @Override
    protected HttpClient getHttpClient() {
        return new HttpClient_Okhttp();
    }
}
