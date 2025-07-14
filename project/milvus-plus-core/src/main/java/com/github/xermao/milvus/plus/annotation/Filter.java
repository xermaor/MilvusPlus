package com.github.xermao.milvus.plus.annotation;

import com.github.xermao.milvus.plus.model.BuiltInFilterType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义内置过滤器的注解。
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Filter {

    BuiltInFilterType[] builtInFilters() default {};

    ; //内置过滤器

    CustomFilter[] customFilters() default {}; //自定义过滤器

}