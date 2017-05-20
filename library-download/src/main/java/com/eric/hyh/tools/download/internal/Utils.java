package com.eric.hyh.tools.download.internal;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import com.eric.hyh.tools.download.bean.State;
import com.eric.hyh.tools.download.bean.TaskInfo;
import com.eric.hyh.tools.download.internal.db.bean.TaskDBInfo;
import com.eric.hyh.tools.download.internal.db.dao.DaoMaster;
import com.eric.hyh.tools.download.internal.db.dao.DaoSession;
import com.eric.hyh.tools.download.internal.db.dao.TaskDBInfoDao;

import org.greenrobot.greendao.query.Query;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static com.eric.hyh.tools.download.bean.State.INSTALL;
import static com.eric.hyh.tools.download.bean.State.SUCCESS;


/**
 * Created by Eric_He on 2017/3/11.
 */

public class Utils {

    /**
     * SDCARD是否存在
     */
    public static boolean externalMemoryAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


    /**
     * 获取SDCARD剩余存储空间
     *
     * @return
     */
    public static long getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize;
            long availableBlocks;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.getBlockSizeLong();
                availableBlocks = stat.getAvailableBlocksLong();
            } else {
                blockSize = stat.getBlockSize();
                availableBlocks = stat.getAvailableBlocks();
            }
            return availableBlocks * blockSize;
        } else {
            return Constans.MEMORY_SIZE_ERROR;
        }
    }


    /**
     * 获取手机内部剩余存储空间
     *
     * @return
     */
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize;
        long availableBlocks;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            availableBlocks = stat.getAvailableBlocksLong();
        } else {
            blockSize = stat.getBlockSize();
            availableBlocks = stat.getAvailableBlocks();
        }
        return availableBlocks * blockSize;
    }


    public static File getDownLoadFile(Context context, String resKey) {
        File dir;
        //has sdcard 优先存于sd卡中 如果没有就存于内部内存中
        if (externalMemoryAvailable()) {
            File filesDir = context.getExternalFilesDir(null);
            if (filesDir == null) {
                filesDir = new File(Environment.getExternalStorageDirectory() + File.separator
                        + "Android" + File.separator
                        + "data" + File.separator
                        + context.getPackageName() + File.separator
                        + "files");
            }
            dir = new File(filesDir, "download");
        } else {
            File filesDir = context.getFilesDir();
            if (filesDir == null) {
                filesDir = new File(context.getCacheDir().getParentFile(), "files");
            }
            dir = new File(filesDir, "download");
        }
        if (!dir.exists()) {
            //如果文件夹不存在 创建文件夹
            dir.mkdirs();
        }
        File file = new File(dir, string2MD5(resKey));
        return file;
    }

    static boolean isWifi(Context context) {
        try {
            ConnectivityManager connectMgr =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectMgr.getActiveNetworkInfo();
            if (networkInfo != null) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static int getVersionCode(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packages = packageManager.getInstalledPackages(0);
        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            if (TextUtils.equals(packageName, packageInfo.packageName)) {
                return packageInfo.versionCode;
            }
        }
        return -1;
    }


    /**
     * @return 手机中所有已安装的非系统应用程序的包名列表
     */
    static List<String> getInstalledApps(Context context) {
        List<PackageInfo> installedPackages = context.getPackageManager().getInstalledPackages(0);
        List<String> packageList = new ArrayList<>();
        if (installedPackages == null || installedPackages.isEmpty()) {
            return packageList;
        }
        for (int i = 0; i < installedPackages.size(); i++) {
            String packageName = installedPackages.get(i).packageName;
            /*如果是系統应用则不处理*/
            if (installedPackages.get(i).applicationInfo.sourceDir.contains("system/")) {
                continue;
            }
            packageList.add(packageName);
        }
        return packageList;
    }

    /**
     * @param context
     * @return true/false表示用户是否在应用中
     */
    public static boolean isAppExit(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null || appProcesses.size() == 0)
            return false;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE
                        || appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND
                        || appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_EMPTY
                        || appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_GONE) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isAppInstall(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            if (packageInfo != null) {
                return true;
            } else {
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    private static String string2MD5(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(content.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                String s = Integer.toHexString(0xff & bytes[i]);
                if (s.length() == 1) {
                    sb.append("0").append(s);
                } else {
                    sb.append(s);
                }
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return content;
    }

    public static boolean deleteDownloadFile(Context context, String resKey) {
        boolean delete = false;
        File file = getDownLoadFile(context, resKey);
        if (file != null) {
            final File to = new File(file, String.valueOf(System.currentTimeMillis()));
            if (file.renameTo(to)) {
                delete = to.delete();
            } else {
                delete = file.delete();
            }
        }
        return delete;
    }


    public static class DBUtil {

        private TaskDBInfoDao dao;

        private static volatile DBUtil sDBUtil;

        public static DBUtil getInstance(Context context) {
            if (sDBUtil != null) {
                return sDBUtil;
            }
            synchronized (DBUtil.class) {
                if (sDBUtil == null) {
                    sDBUtil = new DBUtil(context);
                }
                return sDBUtil;
            }
        }

        private DBUtil(Context context) {
            DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(context.getApplicationContext(), "taskdb", null);
            SQLiteDatabase db = devOpenHelper.getWritableDatabase();
            DaoMaster master = new DaoMaster(db);
            DaoSession session = master.newSession();
            this.dao = session.getTaskDBInfoDao();
        }


        public void operate(final TaskInfo taskInfo, final TaskDBInfo taskDBInfo, Executor executor) {
            if (taskInfo.getCurrentStatus() == State.DELETE || taskInfo.getCurrentStatus() == State.UNINSTALL) {
                delete(taskInfo, taskDBInfo, executor);
            } else {
                insertOrReplace(taskInfo, taskDBInfo, executor);
            }
        }


        private void insertOrReplace(final TaskInfo taskInfo, final TaskDBInfo taskDBInfo, Executor executor) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    TaskDBInfo newTaskDBInfo = taskInfo2TaskDBInfo(taskInfo, taskDBInfo);
                    dao.insertOrReplace(newTaskDBInfo);
                }
            });
        }

        synchronized void insertOrReplace(TaskDBInfo taskDBInfo) {
            dao.insertOrReplace(taskDBInfo);
        }


        private void delete(final TaskInfo taskInfo, final TaskDBInfo taskDBInfo, Executor executor) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    TaskDBInfo newTaskDBInfo = taskInfo2TaskDBInfo(taskInfo, taskDBInfo);
                    Query<TaskDBInfo> query = dao.queryBuilder()
                            .where(TaskDBInfoDao.Properties.ResKey.eq(newTaskDBInfo.getResKey()))
                            .orderDesc(TaskDBInfoDao.Properties.Time)
                            .build();
                    if (query != null && query.list().size() > 0) {
                        dao.delete(query.list().get(0));
                    }
                }
            });
        }

        synchronized void delete(final TaskDBInfo taskDBInfo) {
            dao.delete(taskDBInfo);
        }


        synchronized List<String> getSuccessList() {
            List<String> list = new ArrayList<>();
            Query<TaskDBInfo> query = dao.queryBuilder()
                    .where(TaskDBInfoDao.Properties.CurrentStatus.eq(SUCCESS))
                    /*.whereOr(TaskDBInfoDao.Properties.CurrentStatus.eq(State.SUCCESS), TaskDBInfoDao.Properties.CurrentStatus.eq(INSTALL))*/
                    .build();
            if (query != null && query.list() != null) {
                List<TaskDBInfo> taskDBInfos = query.list();
                for (TaskDBInfo taskDBInfo : taskDBInfos) {
                    list.add(taskDBInfo.getResKey());
                }
            }
            return list;
        }


        public synchronized Map<String, TaskDBInfo> getAllTasks(Map<String, TaskDBInfo> map) {
            List<TaskDBInfo> taskDBInfos = dao.loadAll();
            if (taskDBInfos != null && taskDBInfos.size() > 0) {
                for (TaskDBInfo taskDBInfo : taskDBInfos) {
                    map.put(taskDBInfo.getResKey(), taskDBInfo);
                }
            }
            return map;
        }


        public synchronized List<TaskDBInfo> getWaitingForWifiTasks() {
            return getTasks(State.WAITING_FOR_WIFI);
        }


        public synchronized List<TaskDBInfo> getSuccessTasks() {
            return getTasks(SUCCESS);
        }

        public synchronized List<TaskDBInfo> getInstalledTasks() {
            return getTasks(INSTALL);
        }

        public synchronized List<TaskDBInfo> getAllTasks() {
            List<TaskDBInfo> taskDBInfos = dao.loadAll();
            return taskDBInfos == null ? new ArrayList<TaskDBInfo>() : taskDBInfos;
        }

        public synchronized boolean correctDBErroStatus(Context context) {
            List<TaskDBInfo> taskDBInfos = dao.loadAll();
            if (taskDBInfos.isEmpty()) {
                return true;
            }
            for (TaskDBInfo taskDBInfo : taskDBInfos) {
                File file = new File(taskDBInfo.getFilePath());
                if (!file.exists()) {//可能是文件被删除了
                    if (TextUtils.isEmpty(taskDBInfo.getPackageName())
                            || !Utils.isAppInstall(context, taskDBInfo.getPackageName())) {//不是因为应用安装后删除文件的情况
                        Long currentSize = taskDBInfo.getCurrentSize();
                        if (currentSize != null && currentSize > 0) {
                            taskDBInfo.setCurrentSize(0L);
                            taskDBInfo.setProgress(0);
                            taskDBInfo.setCurrentStatus(State.PAUSE);
                            dao.insertOrReplace(taskDBInfo);
                            continue;
                        }
                    }
                }
                if (taskDBInfo.getCurrentStatus() == State.PREPARE || taskDBInfo.getCurrentStatus() == State.WAITING_IN_QUEUE
                        || taskDBInfo.getCurrentStatus() == State.START_WRITE || taskDBInfo.getCurrentStatus() == State.DOWNLOADING) {
                    taskDBInfo.setCurrentStatus(State.PAUSE);
                    dao.insertOrReplace(taskDBInfo);
                } else if (taskDBInfo.getCurrentStatus() == SUCCESS
                        && Utils.isAppInstall(context, taskDBInfo.getPackageName())
                        && ((taskDBInfo.getVersionCode() != null) && taskDBInfo.getVersionCode() == Utils.getVersionCode(context, taskDBInfo.getPackageName()))) {
                    taskDBInfo.setCurrentStatus(INSTALL);
                    Utils.deleteDownloadFile(context, taskDBInfo.getResKey());
                    dao.insertOrReplace(taskDBInfo);
                } else if (taskDBInfo.getCurrentStatus() == INSTALL && !Utils.isAppInstall(context, taskDBInfo.getPackageName())) {
                    dao.delete(taskDBInfo);
                }
            }
            return true;
        }

        private List<TaskDBInfo> getTasks(int status) {
            Query<TaskDBInfo> query = dao.queryBuilder()
                    .where(TaskDBInfoDao.Properties.CurrentStatus.eq(status))
                    .orderDesc(TaskDBInfoDao.Properties.Time)
                    .build();
            if (query != null && query.list() != null) {
                return query.list();
            }
            return new ArrayList<>();
        }

        public synchronized TaskDBInfo getTaskDBInfoByResKey(String resKey) {
            Query<TaskDBInfo> query = dao.queryBuilder().where(TaskDBInfoDao.Properties.ResKey.eq(resKey)).build();
            if (query != null && query.list() != null && query.list().size() > 0) {
                return query.list().get(0);
            } else {
                return null;
            }
        }

        synchronized TaskDBInfo getTaskDBInfoByPackageName(String packageName) {
            Query<TaskDBInfo> query = dao.queryBuilder().where(TaskDBInfoDao.Properties.PackageName.eq(packageName)).build();
            if (query != null && query.list() != null && query.list().size() > 0) {
                return query.list().get(0);
            } else {
                return null;
            }
        }


        synchronized boolean isSuccessOrInstall(String resKey) {
            Query<TaskDBInfo> query = dao.queryBuilder()
                    .where(TaskDBInfoDao.Properties.ResKey.eq(resKey))
                    .whereOr(TaskDBInfoDao.Properties.CurrentStatus.eq(State.SUCCESS), TaskDBInfoDao.Properties.CurrentStatus.eq(State.INSTALL))
                    .build();
            if (query == null) {
                return false;
            } else if (query.list() == null || query.list().size() == 0) {
                return false;
            } else {
                return true;
            }
        }

        private TaskDBInfo taskInfo2TaskDBInfo(TaskInfo taskInfo, TaskDBInfo taskDBInfo) {
            taskDBInfo.clear();
            //taskDBInfo = new TaskDBInfo();
            taskDBInfo.setResKey(taskInfo.getResKey());
            taskDBInfo.setUrl(taskInfo.getUrl());
            taskDBInfo.setFilePath(taskInfo.getFilePath());
            taskDBInfo.setExpand(taskInfo.getExpand());
            taskDBInfo.setCurrentSize(taskInfo.getCurrentSize());
            taskDBInfo.setCurrentStatus(taskInfo.getCurrentStatus());
            taskDBInfo.setTotalSize(taskInfo.getTotalSize());
            taskDBInfo.setByService(taskInfo.isByService());
            taskDBInfo.setPackageName(taskInfo.getPackageName());
            taskDBInfo.setTagClassName(taskInfo.getTagClassName());
            taskDBInfo.setProgress(taskInfo.getProgress());
            taskDBInfo.setWifiAutoRetry(taskInfo.isWifiAutoRetry());
            taskDBInfo.setTagJson(taskInfo.getTagJson());
            taskDBInfo.setTime(System.currentTimeMillis());
            taskDBInfo.setVersionCode(taskInfo.getVersionCode());
            taskDBInfo.setResponseCode(taskInfo.getCode());
            return taskDBInfo;
        }
    }
}
