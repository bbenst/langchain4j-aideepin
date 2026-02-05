package com.moyz.adi.common.dto;

import com.moyz.adi.common.interfaces.AiModelAddGroup;
import com.moyz.adi.common.interfaces.AiModelEditGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

/**
 * AI模型数据传输对象
 */
@Validated
@Data
public class AiModelDto {
    /**
     * 主键ID
     */
    @NotNull(groups = AiModelEditGroup.class)
    private Long id;
    /**
     * 类型
     */
    @NotBlank(groups = AiModelAddGroup.class)
    private String type;
    /**
     * 名称
     */
    @NotBlank(groups = AiModelAddGroup.class)
    private String name;
    /**
     * 标题
     */
    private String title;
    /**
     * 配置
     */
    private String setting;
    /**
     * 平台
     */
    @NotBlank(groups = AiModelAddGroup.class)
    private String platform;
    /**
     * 描述
     */
    private String remark;
    /**
     * 是否启用
     */
    private Boolean isEnable;
    /**
     * 是否免费
     */
    private Boolean isFree;
    /**
     * 上下文窗口
     */
    private Integer contextWindow;
    /**
     * 最大输入Token数
     */
    private Integer maxInputTokens;
    /**
     * 最大输出Token数
     */
    private Integer maxOutputTokens;
    /**
     * 输入类型
     */
    private String inputTypes;
    /**
     * 是否推理模型
     */
    private Boolean isReasoner;
    /**
     * 是否可关闭思考
     */
    private Boolean isThinkingClosable;
    /**
     * 响应格式类型
     */
    private String responseFormatTypes;
    /**
     * 是否支持网页搜索
     */
    private Boolean isSupportWebSearch;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
