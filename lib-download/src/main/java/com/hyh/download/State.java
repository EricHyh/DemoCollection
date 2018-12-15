package com.hyh.download;

/**
 * Created by Administrator on 2017/3/10.
 */

public class State {

    public static final int NONE = 0;//初始状态，数据库中还未存储，不用处理

    public static final int PREPARE = 1;//准备状态，显示下载中

    public static final int START_WRITE = 2;//开始写状态，显示下载中

    public static final int DOWNLOADING = 3;//下载中状态，显示下载中

    public static final int WAITING_IN_QUEUE = 4;//等待状态

    public static final int WAITING_FOR_WIFI = 5;//等待wifi状态

    public static final int DELETE = 6;//下载任务被删除

    public static final int PAUSE = 7;//下载任务被暂停

    public static final int SUCCESS = 8;//下载成功

    public static final int FAILURE = 9;//下载失败

}
