package com.moyz.adi.common.dto;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moyz.adi.common.vo.AudioConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Conv数据传输对象
 */
@Data
public class ConvDto {
    /**
     * 主键ID
     */
    private Long id;
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
     * 描述
     */
    private String remark;
    /**
     * Token
     */
    private Integer tokens;
    /**
     * AI系统消息
     */
    @Schema(title = "set the system message to ai, ig: you are a lawyer")
    private String aiSystemMessage;
    /**
     * understand上下文启用
     */
    private Boolean understandContextEnable;
    /**
     * MCPIds
     */
    private List<Long> mcpIds;
    /**
     * kbIds
     */
    private List<Long> kbIds;
    /**
     * conv知识列表
     */
    private List<ConvKnowledge> convKnowledgeList;
    /**
     * 答案内容类型
     */
    private Integer answerContentType;
    /**
     * 是否Autoplay答案
     */
    private Boolean isAutoplayAnswer;
    /**
     * 是否启用思考
     */
    private Boolean isEnableThinking;
    /**
     * 是否启用网页搜索
     */
    private Boolean isEnableWebSearch;
    /**
     * 音频配置
     */
    private AudioConfig audioConfig;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
