package com.hyh.filedownloader.sample.preference;

import android.content.Context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Administrator
 * @description
 * @data 2019/4/26
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

    String value();

    int mode() default Context.MODE_PRIVATE;
}
