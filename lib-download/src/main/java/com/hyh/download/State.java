package com.hyh.download;

/**
 * Created by Administrator on 2017/3/10.
 */

public class State {

    public static final int NONE = 0;//初始状态，数据库中还未存储，不用处理

    public static final int PREPARE = 1;//准备状态，显示下载中

    public static final int WAITING_START = 2;//等待状态

    public static final int WAITING_END = 3;//等待状态

    public static final int CONNECTED = 4;//已连接文件服务器

    public static final int DOWNLOADING = 5;//下载中状态，显示下载中

    public static final int RETRYING = 6;//重试中状态，显示下载中

    public static final int PAUSE = 7;//下载任务被暂停

    public static final int DELETE = 8;//下载任务被删除

    public static final int SUCCESS = 9;//下载成功

    public static final int FAILURE = 10;//下载失败

}
