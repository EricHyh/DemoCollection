package com.hyh.common.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Administrator
 * @description
 * @data 2019/6/17
 */

public class FileUtil {

    private static final String TAG = "FileUtil";

    public static boolean externalMemoryAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && !Environment.isExternalStorageRemovable();
    }

    public static File generateEmptyFile(File dir) {
        String format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());
        return generateEmptyFile(dir, format);
    }

    public static File generateEmptyFile(String dir) {
        String format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());
        return generateEmptyFile(dir, format);
    }

    public static File generateEmptyFile(File dir, String prefix, String suffix) {
        ensureCreated(dir);
        String format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());
        return generateEmptyFile(dir, prefix + format + suffix);
    }

    public static File generateEmptyFile(File dir, String expectedName) {
        if (TextUtils.isEmpty(expectedName)) {
            expectedName = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());
        }
        ensureCreated(dir);
        File file = new File(dir, expectedName);
        if (!file.exists()) return file;

        int index = expectedName.lastIndexOf(".");
        String toPrefix;
        String toSuffix;
        if (index < 0) {
            toPrefix = expectedName;
            toSuffix = "";
        } else {
            toPrefix = expectedName.substring(0, index);
            toSuffix = expectedName.substring(index);
        }
        BigInteger fileIndex = new BigInteger("0");
        BigInteger one = new BigInteger("1");
        File newFile;
        do {
            fileIndex = fileIndex.add(one);
            newFile = new File(dir, toPrefix + '(' + fileIndex + ')' + toSuffix);
        } while (newFile.exists());
        return newFile;
    }

    public static File generateEmptyFile(String dir, String expectedName) {
        return generateEmptyFile(new File(dir), expectedName);
    }

    public static boolean isFileExists(String path) {
        return !TextUtils.isEmpty(path) && new File(path).exists();
    }

    public static File ensureCreated(File fileDir) {
        if (fileDir != null && !fileDir.exists() && !fileDir.mkdirs()) {
            Log.w(TAG, "Unable to create the directory:" + fileDir.getPath());
        }
        return fileDir;
    }

    public static File ensureCreated(String fileDirPath) {
        return ensureCreated(new File(fileDirPath));
    }

    public static boolean isChildFile(File parent, File child) {
        if (parent == null || child == null) return false;
        return child.getAbsolutePath().startsWith(parent.getAbsolutePath());
    }

    public static boolean isChildFile(File parent, String childPath) {
        if (parent == null || childPath == null) return false;
        return childPath.startsWith(parent.getAbsolutePath());
    }

    public static File getChildFile(File file1, File file2) {
        if (isChildFile(file1, file2)) {
            return file2;
        }
        if (isChildFile(file2, file1)) {
            return file1;
        }
        return null;
    }

    public static boolean isInAppDir(Context context, String filePath) {
        return isChildFile(getDataDir(context), filePath)
                || isChildFile(context.getExternalCacheDir(), filePath)
                || isChildFile(context.getExternalFilesDir(null), filePath);
    }

    public static File getDataDir(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getDataDir();
        } else {
            return new File(context.getApplicationInfo().dataDir);
        }
    }

    public static boolean copyFile(Context context, Uri uri, File target) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            return StreamUtil.streamToFile(inputStream, target.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean copyFile(Context context, Uri uri, String targetPath) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            return StreamUtil.streamToFile(inputStream, targetPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean copyFile(String sourcePath, File destDir) {
        return copyFile(sourcePath, destDir, null) != null;
    }

    public static String copyFile(String sourcePath, File destDir, String defaultPath) {
        FileUtil.ensureCreated(destDir);
        if (isChildFile(destDir, sourcePath)) {
            return sourcePath;
        }
        File file = new File(sourcePath);
        if (file.isFile() && file.exists()) {
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                String destPath = new File(destDir, file.getName()).getAbsolutePath();
                bis = new BufferedInputStream(new FileInputStream(file));
                bos = new BufferedOutputStream(new FileOutputStream(destPath, false));
                int len;
                byte[] buffer = new byte[1024 * 1024];
                while ((len = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);
                }
                bos.flush();
                return destPath;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                StreamUtil.close(bos, bis);
            }
        }
        return defaultPath;
    }


    public static boolean copyFile(String sourcePath, String destPath) {
        File file = new File(sourcePath);
        if (file.isFile() && file.exists()) {
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                ensureCreated(new File(destPath).getParentFile());
                bis = new BufferedInputStream(new FileInputStream(file));
                bos = new BufferedOutputStream(new FileOutputStream(destPath, false));
                int len;
                byte[] buffer = new byte[1024 * 1024];
                while ((len = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);
                }
                bos.flush();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                StreamUtil.close(bos, bis);
            }
        }
        return false;
    }

    public static boolean delete(String path) {
        if (TextUtils.isEmpty(path)) return false;
        return delete(new File(path));
    }

    public static boolean delete(File file) {
        if (!file.exists()) return true;
        try {
            return file.delete();
        } catch (Exception ignore) {
        }
        return false;
    }


    @Deprecated
    public static String copyFileToTargetDir(String filePath, File dir) {
        return copyFile(filePath, dir, filePath);
    }

    public static String getExternalFilesDir(Context context) {
        File externalFilesDir = context.getExternalFilesDir(null);
        if (externalFilesDir == null) {
            externalFilesDir = new File(Environment.getExternalStorageDirectory() + File.separator
                    + "Android" + File.separator
                    + "data" + File.separator
                    + context.getPackageName() + File.separator
                    + "files");
        }
        return externalFilesDir.getAbsolutePath();
    }

    public static String getExternalFilesDir(Context context, String type) {
        if (TextUtils.isEmpty(type)) return getExternalFilesDir(context);
        File externalFilesDir = context.getExternalFilesDir(type);
        if (externalFilesDir == null) {
            externalFilesDir = new File(Environment.getExternalStorageDirectory() + File.separator
                    + "Android" + File.separator
                    + "data" + File.separator
                    + context.getPackageName() + File.separator
                    + "files" + File.separator
                    + type);
        }
        return externalFilesDir.getAbsolutePath();
    }

    public static String getExternalRootFilesDir(String type) {
        if (TextUtils.isEmpty(type)) return Environment.getExternalStorageDirectory().getAbsolutePath();
        File externalFilesDir = new File(Environment.getExternalStorageDirectory() + File.separator + type);
        return externalFilesDir.getAbsolutePath();
    }

    public static String getFileName(Context context, Uri uri, DocumentUriParser... parser) {
        String mimeType = context.getContentResolver().getType(uri);
        String filename = null;

        try {
            if (mimeType != null) {
                Cursor returnCursor = context.getContentResolver().query(uri, null,
                        null, null, null);
                if (returnCursor != null) {
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    filename = returnCursor.getString(nameIndex);
                    returnCursor.close();
                }
            }
        } catch (Exception ignore) {
        }

        if (filename == null) {
            String path = getPathFromUri(context, uri, parser);
            if (path == null) {
                filename = uri.getLastPathSegment();
            } else {
                File file = new File(path);
                filename = file.getName();
            }
        }
        return filename;
    }

    public static String getPathFromUri(Context context, Uri uri, DocumentUriParser... parser) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            /* List<DocumentUriParser> list = Arrays.asList(parser, new ExternalStorageParser(), new DownloadsStorageParser(), new MediaStorageParser());*/
            List<DocumentUriParser> parsers = new ArrayList<>();
            if (parser != null && parser.length > 0) {
                parsers.addAll(Arrays.asList(parser));
            }
            parsers.add(new ExternalStorageParser());
            parsers.add(new DownloadsStorageParser());
            parsers.add(new MediaStorageParser());
            for (DocumentUriParser documentUriParser : parsers) {
                if (documentUriParser == null) continue;
                if (documentUriParser.isMatched(context, uri)) {
                    return documentUriParser.getPath(context, uri);
                }
            }
        } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }


    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = MediaStore.Files.FileColumns.DATA;
        final String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            StreamUtil.close(cursor);
        }
        return null;
    }

    public interface DocumentUriParser {

        boolean isMatched(Context context, Uri uri);

        String getPath(Context context, Uri uri);

    }

    private static class ExternalStorageParser implements DocumentUriParser {

        @Override
        public boolean isMatched(Context context, Uri uri) {
            return "com.android.externalstorage.documents".equals(uri.getAuthority());
        }

        @Override
        public String getPath(Context context, Uri uri) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            if ("primary".equalsIgnoreCase(type)) {
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if ("home".equalsIgnoreCase(type)) {
                return Environment.getExternalStorageDirectory() + "/documents/" + split[1];
            }
            return null;
        }
    }

    private static class DownloadsStorageParser implements DocumentUriParser {

        @Override
        public boolean isMatched(Context context, Uri uri) {
            return "com.android.providers.downloads.documents".equals(uri.getAuthority());
        }

        @Override
        public String getPath(Context context, Uri uri) {
            final String id = DocumentsContract.getDocumentId(uri);

            if (id != null && id.startsWith("raw:")) {
                return id.substring(4);
            }

            String[] contentUriPrefixes = new String[]{
                    "content://downloads/public_downloads",
                    "content://downloads/my_downloads"
            };

            for (String contentUriPrefix : contentUriPrefixes) {
                try {
                    assert id != null;
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));
                    String path = getDataColumn(context, contentUri, null, null);
                    if (path != null) {
                        return path;
                    }
                } catch (Exception ignore) {
                }
            }
            return null;
        }
    }

    private static class MediaStorageParser implements DocumentUriParser {

        @Override
        public boolean isMatched(Context context, Uri uri) {
            return "com.android.providers.media.documents".equals(uri.getAuthority());
        }

        @Override
        public String getPath(Context context, Uri uri) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }


            final String selection = BaseColumns._ID + "=?";
            final String[] selectionArgs = new String[]{
                    split[1]
            };

            return getDataColumn(context, contentUri, selection, selectionArgs);
        }
    }

    /**
     * @return The MIME type for the given file.
     */
    public static String getMimeType(File file) {

        String extension = getExtension(file.getName());

        if (extension.length() > 0)
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));

        return "application/octet-stream";
    }

    /**
     * Gets the extension of a file name, like ".png" or ".jpg".
     *
     * @param uri
     * @return Extension including the dot("."); "" if there is no extension;
     * null if uri was null.
     */
    public static String getExtension(String uri) {
        if (uri == null) {
            return null;
        }

        int dot = uri.lastIndexOf(".");
        if (dot >= 0) {
            return uri.substring(dot);
        } else {
            // No extension.
            return "";
        }
    }

    /**
     * @return The MIME type for the give Uri.
     */
    public static String getMimeType(Context context, Uri uri) {
        String path = getPathFromUri(context, uri);
        if (TextUtils.isEmpty(path)) return null;
        File file = new File(path);
        return getMimeType(file);
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

}