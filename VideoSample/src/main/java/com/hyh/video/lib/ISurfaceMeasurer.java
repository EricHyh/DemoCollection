package com.hyh.video.lib;

/**
 * @author Administrator
 * @description 负责视频播放区域得测量
 * @data 2019/2/23
 */
public interface ISurfaceMeasurer {

    int[] onMeasure(int widthMeasureSpec, int heightMeasureSpec);

}
