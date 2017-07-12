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
        entity.addIdProperty().autoincrement();
        entity.addStringProperty("resKey").unique();
        entity.addStringProperty("url");
        entity.addIntProperty("currentStatus");
        entity.addIntProperty("progress");
        entity.addIntProperty("versionCode");
        entity.addIntProperty("responseCode");
        entity.addIntProperty("rangeNum");
        entity.addLongProperty("totalSize");
        entity.addLongProperty("currentSize");
        entity.addLongProperty("time");
        entity.addStringProperty("packageName");
        entity.addStringProperty("filePath");
        entity.addStringProperty("expand");
        entity.addBooleanProperty("wifiAutoRetry");
        entity.addStringProperty("tagJson");
        entity.addStringProperty("tagClassName");
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
    private String expand;
    private Boolean byService;
    private Boolean wifiAutoRetry;
    private String tagJson;
    private String tagClassName;
     */
}
