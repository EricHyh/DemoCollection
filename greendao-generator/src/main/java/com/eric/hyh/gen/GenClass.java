package com.eric.hyh.gen;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Schema;

public class GenClass {

    public static final String GEN_PATH = "E:\\githubproject\\Filedownloader-master\\greendao-generator\\src\\main\\java-gen";

    public static void main(String[] args) {
        Schema schema = new Schema(1, "com.eric.hyh.tools.download.internal.db.bean");
        addTaskDBInfo(schema);
        schema.setDefaultJavaPackageDao("com.eric.hyh.tools.download.internal.db.dao");
        //将生成的内容放在指定的路径下C:\Users\admin\Desktop\shujuku\MyApplication\app\src\main\java-gen
        try {
            new DaoGenerator().generateAll(schema, GEN_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void addTaskDBInfo(Schema schema) {
        Entity entity = schema.addEntity("TaskDBInfo");
        entity.setTableName("TaskDBInfo");
        entity.addIdProperty().autoincrement();
        entity.addStringProperty("resKey").columnName("resKey").unique();
        entity.addStringProperty("url").columnName("url");
        entity.addIntProperty("currentStatus").columnName("currentStatus");
        entity.addIntProperty("progress").columnName("progress");
        entity.addIntProperty("versionCode").columnName("versionCode");
        entity.addIntProperty("responseCode").columnName("responseCode");
        entity.addIntProperty("rangeNum").columnName("rangeNum");
        entity.addLongProperty("totalSize").columnName("totalSize");
        entity.addLongProperty("currentSize").columnName("currentSize");
        entity.addLongProperty("timeMillis").columnName("timeMillis");
        entity.addStringProperty("packageName").columnName("packageName");
        entity.addStringProperty("filePath").columnName("filePath");
        entity.addBooleanProperty("wifiAutoRetry").columnName("wifiAutoRetry");
        entity.addStringProperty("tagStr").columnName("tagStr");
        entity.addStringProperty("tagClassName").columnName("tagClassName");
    }
    /*
        private String resKey;
    private String url;
    private Integer currentStatus;
    private Integer progress;
    private Integer versionCode;
    private Integer responseCode;
    private Long totalSize;
    private Long currentSize;
    private Long time;
    private String packageName;
    private String filePath;
    private Boolean byService;
    private Boolean wifiAutoRetry;
    private String tagJson;
    private String tagClassName;
     */
}
