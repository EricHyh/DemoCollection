package com.hyh.video.lib;

/**
 * @author Administrator
 * @description 负责视频播放区域得测量
 * @data 2019/2/23
 */
public interface ISurfaceMeasurer {

    void setVideoWidth(int width, int height);

    int[] onMeasure(int maxWidth, int maxHeight);

}