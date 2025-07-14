package com.github.xermao.milvus.plus.annotation;


import io.milvus.v2.common.ConsistencyLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xermao
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MilvusCollection {
    /**
     * 集合的名称
     */
    String name();

    /**
     * 集合的说明
     */
    String description() default "";

    /**
     *  别名
     */
    String[] alias() default {};

    /**
     * 一致性级别
     */

    ConsistencyLevel level() default ConsistencyLevel.BOUNDED;

    /**
     * 禁用动态字段
     */
    boolean enableDynamicField() default false;

}