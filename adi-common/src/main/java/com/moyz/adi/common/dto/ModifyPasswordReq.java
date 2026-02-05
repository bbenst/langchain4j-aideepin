package com.moyz.adi.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

/**
 * Modify密码请求
 */
@Data
@Validated
public class ModifyPasswordReq {
    /**
     * old密码
     */
    @NotBlank
    @Length(min = 6)
    private String oldPassword;
    /**
     * new密码
     */
    @NotBlank
    @Length(min = 6)
    private String newPassword;
}
