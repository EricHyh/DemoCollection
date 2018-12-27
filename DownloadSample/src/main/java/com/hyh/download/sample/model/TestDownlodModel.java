package com.hyh.download.sample.model;

import com.hyh.download.sample.base.Constants;
import com.hyh.download.sample.bean.DownloadBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2018/12/27
 */

public class TestDownlodModel {

    public List<DownloadBean> getDownloadBeanList() {
        List<DownloadBean> downloadBeanList = new ArrayList<>();
        downloadBeanList.add(new DownloadBean("文件1", Constants.DownloadUrl.URL_1));
        downloadBeanList.add(new DownloadBean("文件2", Constants.DownloadUrl.URL_2));
        downloadBeanList.add(new DownloadBean("文件3", Constants.DownloadUrl.URL_3));
        downloadBeanList.add(new DownloadBean("文件4", Constants.DownloadUrl.URL_4));
        downloadBeanList.add(new DownloadBean("文件5", Constants.DownloadUrl.URL_5));
        downloadBeanList.add(new DownloadBean("文件6", Constants.DownloadUrl.URL_6));
        downloadBeanList.add(new DownloadBean("文件7", Constants.DownloadUrl.URL_7));
        downloadBeanList.add(new DownloadBean("文件8", Constants.DownloadUrl.URL_8));
        downloadBeanList.add(new DownloadBean("文件9", Constants.DownloadUrl.URL_9));
        downloadBeanList.add(new DownloadBean("文件10", Constants.DownloadUrl.URL_10));
        return downloadBeanList;
    }

}
