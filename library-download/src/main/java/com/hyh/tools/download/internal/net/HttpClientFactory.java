package com.hyh.tools.download.internal.net;

import android.content.Context;

import com.hyh.tools.download.api.HttpClient;
import com.hyh.tools.download.internal.Constants;
import com.hyh.tools.download.internal.Utils;
import com.hyh.tools.download.internal.net.impl.NativeHttpClient;

/**
 * @author Administrator
 * @description
 * @data 2017/10/14
 */

public class HttpClientFactory {


    public static HttpClient produce(Context context) {
        if (Utils.isClassFound(Constants.ThirdLibraryClassName.OKHTTP_CLASS_NAME)) {
            return new OkHttp3Client(context);
        } else {
            return new NativeHttpClient(context);
        }
    }
}
