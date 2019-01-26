package com.eebbk.bfc.bfclog.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于打印方法耗时
 *
 * Created by Simon on 2016/9/5.
 */
@Target({ ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface BfcLogTime {
}
