package com.hyh.tools.download.paser;

import com.hyh.tools.download.bean.Constants;
import com.hyh.tools.download.utils.FD_Utils;

/**
 * @author Administrator
 * @description
 * @data 2017/10/14
 */

public class TagParserFactory {

    public static TagParser produce(TagParser tagParser) {
        if (tagParser != null) {
            return tagParser;
        } else if (FD_Utils.isClassFound(Constants.ThirdLibraryClassName.GSON_CLASS_NAME)) {
            tagParser = new GsonPaser();
        } else if (FD_Utils.isClassFound(Constants.ThirdLibraryClassName.FASTJSON_CLASS_NAME)) {
            tagParser = new FastTagParser();
        } else {
            tagParser = new EmptyTagParser();
        }
        return tagParser;
    }
}
