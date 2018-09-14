package com.hyh.tools.download.net;

import android.content.Context;

import com.hyh.tools.download.bean.Constants;
import com.hyh.tools.download.utils.FD_Utils;
import com.hyh.tools.download.net.nativeimpl.NativeHttpClient;

/**
 * @author Administrator
 * @description
 * @data 2017/10/14
 */

public class HttpClientFactory {


    public static HttpClient produce(Context context) {
        if (FD_Utils.isClassFound(Constants.ThirdLibraryClassName.OKHTTP_CLASS_NAME)) {
            return new OkHttp3Client(context);
        } else {
            return new NativeHttpClient(context);
        }
    }
}
