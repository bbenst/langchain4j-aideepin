package com.moyz.adi.common.dto;

import com.moyz.adi.common.vo.AudioConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Conv编辑请求
 */
@Data
public class ConvEditReq {
    /**
     * 标题
     */
    private String title;
    /**
     * 描述
     */
    private String remark;
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
}
