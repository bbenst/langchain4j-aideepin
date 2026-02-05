package com.moyz.adi.common.dto;

import com.moyz.adi.common.vo.AudioConfig;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Conv新增请求
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Validated
public class ConvAddReq {
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
     * AI系统消息
     */
    private String aiSystemMessage;
    /**
     * MCPIds
     */
    private List<Long> mcpIds;
    /**
     * kbIds
     */
    private List<Long> kbIds;
    /**
     * 音频配置
     */
    private AudioConfig audioConfig;
    /**
     * 是否启用思考
     */
    private Boolean isEnableThinking;
    /**
     * 是否启用网页搜索
     */
    private Boolean isEnableWebSearch;
}
