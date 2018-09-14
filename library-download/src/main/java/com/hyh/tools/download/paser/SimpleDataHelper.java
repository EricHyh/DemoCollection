package com.hyh.tools.download.paser;

/**
 * @author Administrator
 * @description
 * @data 2018/3/1
 */

public class SimpleDataHelper {


    public static boolean isSimpleData(Object object) {
        if (object == null) {
            return false;
        }
        Class[] simpleDataClasses = {String.class, Byte.class, Short.class, Integer.class, Long.class,
                Float.class, Double.class, Boolean.class, Character.class};
        for (Class simpleDataClass : simpleDataClasses) {
            if (simpleDataClass.equals(object.getClass())) {
                return true;
            }
        }
        return false;
    }

}
