package com.moyz.adi.common.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

/**
 * 注册请求参数
 */
@Schema(name = "注册请求参数")
@Data
@Validated
public class RegisterReq {
    /**
     * 邮箱
     */
    @Parameter(description = "邮箱")
    @Email
    private String email;
    /**
     * 密码
     */
    @Parameter(description = "密码")
    @Min(6)
    private String password;
    /**
     * captchaID
     */
    @Parameter(description = "验证码ID")
    @Length(min = 32)
    private String captchaId;
    /**
     * captchaCode
     */
    @Parameter(description = "验证码")
    @Length(min = 4, max = 4)
    private String captchaCode;
}
