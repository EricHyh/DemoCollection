package com.hyh.tools.download.db.bean;

/**
 * @author Administrator
 * @description
 * @data 2018/2/26
 */

public class SQLiteConstants {

    public static String DB_FILE_NAME = "FileDownloader.db";

    public static String TABLE_NAME = "TaskDBInfo";

    public static int SCHEMA_VERSION = 1;

    /**
     private Long id;
     private String resKey;
     private String url;
     private Integer currentStatus;
     private Integer progress;
     private Integer versionCode;
     private Integer responseCode;
     private Integer rangeNum;
     private Long totalSize;
     private Long currentSize;
     private Long time;
     private String packageName;
     private String filePath;
     private Boolean wifiAutoRetry;
     private String tagJson;
     private String tagClassName;
     */
    public static String ID = "_id";
    public static String RESOUCE_KEY = "resKey";
    public static String URL = "url";
    public static String CURRENT_STATUS = "currentStatus";
    public static String PROGRESS = "progress";
    public static String VERSION_CODE = "versionCode";
    public static String RESPONSE_CODE = "responseCode";
    public static String RANGE_NUM = "rangeNum";
    public static String TOTAL_SIZE = "totalSize";
    public static String CURRENT_SIZE = "currentSize";
    public static String TIME_MILLIS = "timeMillis";
    public static String PACKAGE_NAME = "packageName";
    public static String FILE_PATH = "filePath";
    public static String WIFI_AUTO_RETRY = "wifiAutoRetry";
    public static String TAG_JSON = "tagJson";
    public static String TAG_CLASS_NAME = "tagClassName";


    public static String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," +
            RESOUCE_KEY + " TEXT UNIQUE ," +
            URL + " TEXT ," +
            CURRENT_STATUS + " INTEGER ," +
            PROGRESS + " INTEGER ," +
            VERSION_CODE + " INTEGER ," +
            RESPONSE_CODE + " INTEGER ," +
            RANGE_NUM + " INTEGER ," +
            TOTAL_SIZE + " INTEGER ," +
            CURRENT_SIZE + " INTEGER ," +
            TIME_MILLIS + " INTEGER ," +
            PACKAGE_NAME + " TEXT ," +
            FILE_PATH + " TEXT ," +
            WIFI_AUTO_RETRY + " INTEGER ," +
            TAG_JSON + " TEXT ," +
            TAG_CLASS_NAME + " TEXT);";


}
