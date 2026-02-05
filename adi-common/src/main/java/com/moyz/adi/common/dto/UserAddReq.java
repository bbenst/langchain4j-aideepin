package com.moyz.adi.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * 用户新增请求
 */
@Data
@Validated
public class UserAddReq {
    String name;

    @NotBlank
    String email;

    @NotBlank
    String password;
}
