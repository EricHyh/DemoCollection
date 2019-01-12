/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyh.filedownloader.sample.image;

import android.net.NetworkInfo;

import com.yly.lib.picasso.net.Response;
import com.yly.lib.picasso.net.ResponseBody;

import java.io.IOException;

class NetworkRequestHandler extends RequestHandler {
    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";

    private final Downloader downloader;
    private final Stats stats;

    NetworkRequestHandler(Downloader downloader, Stats stats) {
        this.downloader = downloader;
        this.stats = stats;
    }

    @Override
    public boolean canHandleRequest(Request data) {
        String scheme = data.uri.getScheme();
        return (SCHEME_HTTP.equals(scheme) || SCHEME_HTTPS.equals(scheme));
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        Response response = downloader.load(request);
        ResponseBody body = response.body();

        if (!response.isSuccessful()) {
            body.close();
            throw new ResponseException(response.code(), request.networkPolicy);
        }

        // Cache response is only null when the response comes fully from the network. Both completely
        // cached and conditionally cached responses will have a non-null cache response.
        NativePicasso.LoadedFrom loadedFrom = response.cacheResponse() == null ? NativePicasso.LoadedFrom.NETWORK : NativePicasso.LoadedFrom.DISK;

        // Sometimes response content length is zero when requests are being replayed. Haven't found
        // root cause to this but retrying the request seems safe to do so.
        if (loadedFrom == NativePicasso.LoadedFrom.DISK && body.contentLength() == 0) {
            body.close();
            throw new ContentLengthException("Received response with 0 content-length header.");
        }
        if (loadedFrom == NativePicasso.LoadedFrom.NETWORK && body.contentLength() > 0) {
            stats.dispatchDownloadFinished(body.contentLength());
        }
        return new Result(body.byteStream(), loadedFrom);
    }

    @Override
    int getRetryCount() {
        return 2;
    }

    @Override
    boolean shouldRetry(boolean airplaneMode, NetworkInfo info) {
        return info == null || info.isConnected();
    }

    @Override
    boolean supportsReplay() {
        return true;
    }

    static class ContentLengthException extends IOException {
        ContentLengthException(String message) {
            super(message);
        }
    }

    static final class ResponseException extends IOException {
        final int code;
        final int networkPolicy;

        ResponseException(int code, int networkPolicy) {
            super("HTTP " + code);
            this.code = code;
            this.networkPolicy = networkPolicy;
        }
    }
}
