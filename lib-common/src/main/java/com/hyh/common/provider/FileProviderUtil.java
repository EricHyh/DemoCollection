/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.hyh.common.provider;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.START_TAG;


public class FileProviderUtil {

    private static final String
            META_DATA_FILE_PROVIDER_PATHS = "android.support.FILE_PROVIDER_PATHS";

    private static final String TAG_ROOT_PATH = "root-path";
    private static final String TAG_FILES_PATH = "files-path";
    private static final String TAG_CACHE_PATH = "cache-path";
    private static final String TAG_EXTERNAL = "external-path";
    private static final String TAG_EXTERNAL_FILES = "external-files-path";
    private static final String TAG_EXTERNAL_CACHE = "external-cache-path";

    private static final String ATTR_NAME = "name";
    private static final String ATTR_PATH = "path";

    private static final File DEVICE_ROOT = new File("/");

    private static HashMap<String, PathStrategy> sCache = new HashMap<String, PathStrategy>();

    /**
     * Return a content URI for a given {@link File}. Specific temporary
     * permissions for the content URI can be set with
     * {@link Context#grantUriPermission(String, Uri, int)}, or added
     * to an {@link Intent} by calling {@link Intent#setData(Uri) setData()} and then
     * {@link Intent#setFlags(int) setFlags()}; in both cases, the applicable flags are
     * {@link Intent#FLAG_GRANT_READ_URI_PERMISSION} and
     * {@link Intent#FLAG_GRANT_WRITE_URI_PERMISSION}. A FileProviderUtil can only return a
     * <code>content</code> {@link Uri} for file paths defined in their <code>&lt;paths&gt;</code>
     * meta-data element. See the Class Overview for more information.
     *
     * @param context   A {@link Context} for the current component.
     * @param authority The authority of a {@link FileProviderUtil} defined in a
     *                  {@code <provider>} element in your app's manifest.
     * @param file      A {@link File} pointing to the filename for which you want a
     *                  <code>content</code> {@link Uri}.
     * @return A content URI for the file.
     * @throws IllegalArgumentException When the given {@link File} is outside
     *                                  the paths supported by the provider.
     */
    public static Uri getUriForFile(Context context, String authority, File file) {
        final PathStrategy strategy = getPathStrategy(context, authority);
        return strategy.getUriForFile(file);
    }

    /**
     * Return {@link PathStrategy} for given authority, either by parsing or
     * returning from cache.
     */
    private static PathStrategy getPathStrategy(Context context, String authority) {
        PathStrategy strat;
        synchronized (sCache) {
            strat = sCache.get(authority);
            if (strat == null) {
                try {
                    strat = parsePathStrategy(context, authority);
                } catch (IOException e) {
                    throw new IllegalArgumentException(
                            "Failed to parse " + META_DATA_FILE_PROVIDER_PATHS + " meta-data", e);
                } catch (XmlPullParserException e) {
                    throw new IllegalArgumentException(
                            "Failed to parse " + META_DATA_FILE_PROVIDER_PATHS + " meta-data", e);
                }
                sCache.put(authority, strat);
            }
        }
        return strat;
    }

    /**
     * Parse and return {@link PathStrategy} for given authority as defined in
     * {@link #META_DATA_FILE_PROVIDER_PATHS} {@code <meta-data>}.
     *
     * @see #getPathStrategy(Context, String)
     */
    private static PathStrategy parsePathStrategy(Context context, String authority)
            throws IOException, XmlPullParserException {
        final SimplePathStrategy strat = new SimplePathStrategy(authority);

        final ProviderInfo info = context.getPackageManager()
                .resolveContentProvider(authority, PackageManager.GET_META_DATA);
        final XmlResourceParser in = info.loadXmlMetaData(
                context.getPackageManager(), META_DATA_FILE_PROVIDER_PATHS);
        if (in == null) {
            throw new IllegalArgumentException(
                    "Missing " + META_DATA_FILE_PROVIDER_PATHS + " meta-data");
        }

        int type;
        while ((type = in.next()) != END_DOCUMENT) {
            if (type == START_TAG) {
                final String tag = in.getName();

                final String name = in.getAttributeValue(null, ATTR_NAME);
                String path = in.getAttributeValue(null, ATTR_PATH);

                File target = null;
                if (TAG_ROOT_PATH.equals(tag)) {
                    target = DEVICE_ROOT;
                } else if (TAG_FILES_PATH.equals(tag)) {
                    target = context.getFilesDir();
                } else if (TAG_CACHE_PATH.equals(tag)) {
                    target = context.getCacheDir();
                } else if (TAG_EXTERNAL.equals(tag)) {
                    target = Environment.getExternalStorageDirectory();
                } else if (TAG_EXTERNAL_FILES.equals(tag)) {
                    File[] externalFilesDirs = getExternalFilesDirs(context, null);
                    if (externalFilesDirs.length > 0) {
                        target = externalFilesDirs[0];
                    }
                } else if (TAG_EXTERNAL_CACHE.equals(tag)) {
                    File[] externalCacheDirs = getExternalCacheDirs(context);
                    if (externalCacheDirs.length > 0) {
                        target = externalCacheDirs[0];
                    }
                }

                if (target != null) {
                    strat.addRoot(name, buildPath(target, path));
                }
            }
        }

        return strat;
    }

    public static File[] getExternalFilesDirs(Context context, String type) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            return context.getExternalFilesDirs(type);
        } else {
            return new File[]{context.getExternalFilesDir(type)};
        }
    }

    public static File[] getExternalCacheDirs(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            return context.getExternalCacheDirs();
        } else {
            return new File[]{context.getExternalCacheDir()};
        }
    }

    /**
     * Strategy for mapping between {@link File} and {@link Uri}.
     * <p>
     * Strategies must be symmetric so that mapping a {@link File} to a
     * {@link Uri} and then back to a {@link File} points at the original
     * target.
     * <p>
     * Strategies must remain consistent across app launches, and not rely on
     * dynamic state. This ensures that any generated {@link Uri} can still be
     * resolved if your process is killed and later restarted.
     *
     * @see SimplePathStrategy
     */
    interface PathStrategy {
        /**
         * Return a {@link Uri} that represents the given {@link File}.
         */
        public Uri getUriForFile(File file);

        /**
         * Return a {@link File} that represents the given {@link Uri}.
         */
        public File getFileForUri(Uri uri);
    }

    /**
     * Strategy that provides access to files living under a narrow whitelist of
     * filesystem roots. It will throw {@link SecurityException} if callers try
     * accessing files outside the configured roots.
     * <p>
     * For example, if configured with
     * {@code addRoot("myfiles", context.getFilesDir())}, then
     * {@code context.getFileStreamPath("foo.txt")} would map to
     * {@code content://myauthority/myfiles/foo.txt}.
     */
    static class SimplePathStrategy implements PathStrategy {
        private final String mAuthority;
        private final HashMap<String, File> mRoots = new HashMap<String, File>();

        public SimplePathStrategy(String authority) {
            mAuthority = authority;
        }

        /**
         * Add a mapping from a name to a filesystem root. The provider only offers
         * access to files that live under configured roots.
         */
        public void addRoot(String name, File root) {
            if (TextUtils.isEmpty(name)) {
                throw new IllegalArgumentException("Name must not be empty");
            }

            try {
                // Resolve to canonical path to keep path checking fast
                root = root.getCanonicalFile();
            } catch (IOException e) {
                throw new IllegalArgumentException(
                        "Failed to resolve canonical path for " + root, e);
            }

            mRoots.put(name, root);
        }

        @Override
        public Uri getUriForFile(File file) {
            String path;
            try {
                path = file.getCanonicalPath();
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to resolve canonical path for " + file);
            }

            // Find the most-specific root path
            Map.Entry<String, File> mostSpecific = null;
            for (Map.Entry<String, File> root : mRoots.entrySet()) {
                final String rootPath = root.getValue().getPath();
                if (path.startsWith(rootPath) && (mostSpecific == null
                        || rootPath.length() > mostSpecific.getValue().getPath().length())) {
                    mostSpecific = root;
                }
            }

            if (mostSpecific == null) {
                throw new IllegalArgumentException(
                        "Failed to find configured root that contains " + path);
            }

            // Start at first char of path under root
            final String rootPath = mostSpecific.getValue().getPath();
            if (rootPath.endsWith("/")) {
                path = path.substring(rootPath.length());
            } else {
                path = path.substring(rootPath.length() + 1);
            }

            // Encode the tag and path separately
            path = Uri.encode(mostSpecific.getKey()) + '/' + Uri.encode(path, "/");
            return new Uri.Builder().scheme("content")
                    .authority(mAuthority).encodedPath(path).build();
        }

        @Override
        public File getFileForUri(Uri uri) {
            String path = uri.getEncodedPath();

            final int splitIndex = path.indexOf('/', 1);
            final String tag = Uri.decode(path.substring(1, splitIndex));
            path = Uri.decode(path.substring(splitIndex + 1));

            final File root = mRoots.get(tag);
            if (root == null) {
                throw new IllegalArgumentException("Unable to find configured root for " + uri);
            }

            File file = new File(root, path);
            try {
                file = file.getCanonicalFile();
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to resolve canonical path for " + file);
            }

            if (!file.getPath().startsWith(root.getPath())) {
                throw new SecurityException("Resolved path jumped beyond configured root");
            }

            return file;
        }
    }

    private static File buildPath(File base, String... segments) {
        File cur = base;
        for (String segment : segments) {
            if (segment != null) {
                cur = new File(cur, segment);
            }
        }
        return cur;
    }
}
