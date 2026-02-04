package com.moyz.adi.common.annotation;

import com.moyz.adi.common.validator.ElementInArrayValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 校验字段值是否在指定数组中的约束注解。
 */
@Documented
@Constraint(validatedBy = ElementInArrayValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ElementInArray {
    /**
     * 校验失败时的提示信息。
     *
     * @return 提示信息
     */
    String message() default "Element not found in the array";

    /**
     * 分组校验标识。
     *
     * @return 分组类型
     */
    Class<?>[] groups() default {};

    /**
     * 负载信息。
     *
     * @return 负载类型
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * 允许的取值集合。
     *
     * @return 允许的取值数组
     */
    String[] acceptedValues();

    /**
     * 是否必填。
     *
     * @return 是否必须传入
     */
    boolean required() default false;
}
