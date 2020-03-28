package com.hyh.common.utils;

/**
 * @author Administrator
 * @description
 * @data 2020/1/8
 */
public class ManufacturerInfo {

    public int type;

    public String name;

    public ManufacturerInfo(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public static class Type {

        public final static int UNKNOWN = 0; //未知

        public final static int ALI_YUN = 1; //阿里云

        public final static int QIKU_360 = 2; //奇虎360

        public final static int OPPO = 3; //oppo

        public final static int VIVO = 4; //vivo

        public final static int SMARTISAN = 5; //锤子

        public final static int MEIZU = 6; //魅族

        public final static int XIAO_MI = 7; //小米

        public final static int HUA_WEI = 8; //华为

        public final static int FREEME_OS = 9; //FREEME

    }
}