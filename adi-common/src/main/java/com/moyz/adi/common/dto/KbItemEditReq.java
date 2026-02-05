package com.moyz.adi.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * KbItem编辑请求
 */
@Data
@Validated
public class KbItemEditReq {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * kbID
     */
    @Min(1)
    private Long kbId;
    /**
     * kbUUID
     */
    private String kbUuid;
    /**
     * UUID
     */
    private String uuid;
    /**
     * 标题
     */
    @NotBlank
    private String title;
    /**
     * brief
     */
    private String brief;
    /**
     * 描述
     */
    @NotBlank
    private String remark;
}
