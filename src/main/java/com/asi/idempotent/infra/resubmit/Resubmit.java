package com.asi.idempotent.infra.resubmit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author asi
 * @date 2023/10/7 16:03
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Resubmit {
    /**
     * 防重提交锁过期时间(秒)
     * 默认5秒内不允许重复提交
     */
    int expire() default 5;
}
