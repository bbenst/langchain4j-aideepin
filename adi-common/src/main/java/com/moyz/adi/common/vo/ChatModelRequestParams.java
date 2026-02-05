package com.moyz.adi.common.vo;

import dev.langchain4j.mcp.client.McpClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 使用http与模型进行交互时需要用到的的参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatModelRequestParams {
    /**
     * memoryID
     */
    private String memoryId;
    /**
     * 系统消息
     */
    private String systemMessage;
    /**
     * 用户消息
     */
    private String userMessage;
    //图片地址，多模态LLM才生效
    /**
     * 图片Urls
     */
    private List<String> imageUrls;
    /**
     * MCPClients
     */
    private List<McpClient> mcpClients;
    /**
     * 响应格式
     */
    private String responseFormat;
    /**
     * return思考
     */
    private Boolean returnThinking;
    /**
     * 启用网页搜索
     */
    private Boolean enableWebSearch;
}
