package com.hyh.download.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optional: configures the mapped column for a persistent field.
 * This annotation is also applicable with @ToOne without additional foreign key property
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    /**
     * Name of the database column for this property. Default is field name.
     */
    String nameInDb() default "";
}
