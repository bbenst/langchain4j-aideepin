package com.moyz.adi.common.annotation;

import com.moyz.adi.common.validator.AskReqValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 校验对象字段不能全部为空的约束注解。
 */
@Constraint(validatedBy = {
        AskReqValidator.class,
})
@Target({TYPE, FIELD, PARAMETER, METHOD, CONSTRUCTOR, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
public @interface NotAllFieldsEmptyCheck {
    /**
     * 校验失败时的提示信息。
     *
     * @return 提示信息
     */
    String message() default "all filed is null";

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
}
