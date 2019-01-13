package com.hyh.download.sample.bean;

/**
 * @author Administrator
 * @description
 * @data 2018/12/25
 */

public class DownloadBean {

    public String title;

    public String url;

    public String fileName;

    public DownloadBean(String title, String url, String fileName) {
        this.title = title;
        this.url = url;
        this.fileName = fileName;
    }
}