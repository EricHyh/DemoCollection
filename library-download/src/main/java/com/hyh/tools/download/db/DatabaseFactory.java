package com.hyh.tools.download.db;

import android.content.Context;

import com.hyh.tools.download.bean.Constants;
import com.hyh.tools.download.utils.FD_Utils;

/**
 * @author Administrator
 * @description
 * @data 2018/2/26
 */

public class DatabaseFactory {

    public static Database create(Context context) {
        if (FD_Utils.isClassFound(Constants.ThirdLibraryClassName.GREENDAO_CLASS_NAME)) {
            return new GreendaoDatabase(context);
        } else {
            return new DefaultDatabase(context);
        }
    }
}
