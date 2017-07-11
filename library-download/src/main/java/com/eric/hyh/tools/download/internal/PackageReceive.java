package com.eric.hyh.tools.download.internal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.eric.hyh.tools.download.api.FileDownloader;
import com.eric.hyh.tools.download.bean.State;
import com.eric.hyh.tools.download.internal.db.bean.TaskDBInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/16.
 */

public class PackageReceive extends BroadcastReceiver {

    /**
     * 卸载成功com.laser.gamecenter
     * 安装成功com.laser.gamecenter
     * 替换成功了啊com.laser.gamecenter
     * 替换成功了啊com.laser.gamecenter
     */
    private List<TaskDBInfo> removeTasks;


    @Override
    public void onReceive(final Context context, Intent intent) {

        final FileDownloader fileDownloader = FileDownloader.getInstance();
        if (fileDownloader == null) {
            return;
        }
        String packageName = intent.getData().getSchemeSpecificPart();
        if (!TextUtils.isEmpty(packageName)) {
            Utils.DBUtil dbUtil = Utils.DBUtil.getInstance(context);
            TaskDBInfo taskDBInfo = dbUtil.getTaskDBInfoByPackageName(packageName);
            if (taskDBInfo == null) {
                if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_REPLACED) && removeTasks != null && !removeTasks.isEmpty()) {
                    for (TaskDBInfo removeTask : removeTasks) {
                        if (TextUtils.equals(packageName, removeTask.getPackageName())) {
                            taskDBInfo = removeTask;
                            break;
                        }
                    }
                }
                if (taskDBInfo == null) {
                    return;
                } else {
                    removeTasks.remove(taskDBInfo);
                }
            }
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_ADDED)) {//安装成功
                taskDBInfo.setCurrentStatus(State.INSTALL);
                taskDBInfo.setVersionCode(Utils.getVersionCode(context, packageName));
                dbUtil.insertOrReplace(taskDBInfo);
                Utils.deleteDownloadFile(context, taskDBInfo.getResKey());
                fileDownloader.onInstall(taskDBInfo);
            } else if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_REPLACED)) {//替换成功
                taskDBInfo.setCurrentStatus(State.INSTALL);
                taskDBInfo.setVersionCode(Utils.getVersionCode(context, packageName));
                dbUtil.insertOrReplace(taskDBInfo);
                Utils.deleteDownloadFile(context, taskDBInfo.getResKey());
                fileDownloader.onInstall(taskDBInfo);
            } else if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_REMOVED)) {//卸载成功
                taskDBInfo.setCurrentStatus(State.UNINSTALL);
                dbUtil.delete(taskDBInfo);
                Utils.deleteDownloadFile(context, taskDBInfo.getResKey());
                fileDownloader.onUnInstall(taskDBInfo);
                if (removeTasks == null) {
                    removeTasks = new ArrayList<>();
                }
                removeTasks.add(taskDBInfo);
            }
        }
    }
}
