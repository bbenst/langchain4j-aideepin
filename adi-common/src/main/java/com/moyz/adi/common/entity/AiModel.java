package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moyz.adi.common.base.ObjectNodeTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

/**
 * AiModel对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "adi_ai_model", autoResultMap = true)
@Schema(title = "AiModel对象", description = "AI模型表")
public class AiModel extends BaseEntity {
    /**
     * 模型类型:text,image,embedding,rerank
     */
    @Schema(title = "模型类型:text,image,embedding,rerank")
    @TableField("type")
    private String type;
    /**
     * 模型名称
     */
    @Schema(title = "模型名称")
    @TableField("name")
    private String name;
    /**
     * 模型标题(更易理解记忆的名称)
     */
    @Schema(title = "模型标题(更易理解记忆的名称)")
    @TableField("title")
    private String title;
    /**
     * 模型所属平台
     */
    @Schema(title = "模型所属平台")
    @TableField("platform")
    private String platform;
    /**
     * 模型配置
     */
    @Schema(title = "模型配置")
    @TableField("setting")
    private String setting;
    /**
     * 说明
     */
    @Schema(title = "说明")
    @TableField("remark")
    private String remark;
    /**
     * 是否免费(true:免费,false:收费)
     */
    @Schema(title = "是否免费(true:免费,false:收费)")
    @TableField("is_free")
    private Boolean isFree;
    /**
     * 状态(1:正常使用,0:不可用)
     */
    @Schema(title = "状态(1:正常使用,0:不可用)")
    @TableField("is_enable")
    private Boolean isEnable;
    /**
     * 上下文长度
     */
    @Schema(title = "上下文长度")
    @TableField("context_window")
    private Integer contextWindow;
    /**
     * 最大输入长度
     */
    @Schema(title = "最大输入长度")
    @TableField("max_input_tokens")
    private Integer maxInputTokens;
    /**
     * 最大输出长度
     */
    @Schema(title = "最大输出长度")
    @TableField("max_output_tokens")
    private Integer maxOutputTokens;
    /**
     * 输入类型
     */
    @Schema(title = "输入类型")
    @TableField("input_types")
    private String inputTypes;
    /**
     * 支持的输出格式: text,json_object
     */
    @Schema(title = "支持的输出格式: text,json_object")
    @TableField("response_format_types")
    private String responseFormatTypes;
    /**
     * 属性
     */
    @Schema(title = "属性")
    @TableField(value = "properties", jdbcType = JdbcType.JAVA_OBJECT, typeHandler = ObjectNodeTypeHandler.class)
    private ObjectNode properties;
    /**
     * 是否推理模型
     */
    @Schema(title = "是否推理模型")
    @TableField("is_reasoner")
    private Boolean isReasoner;
    /**
     * 思考过程是否可以关闭
     */
    @Schema(title = "思考过程是否可以关闭")
    @TableField("is_thinking_closable")
    private Boolean isThinkingClosable;
    /**
     * 是否支持web搜索
     */
    @Schema(title = "是否支持web搜索")
    @TableField("is_support_web_search")
    private Boolean isSupportWebSearch;
}
