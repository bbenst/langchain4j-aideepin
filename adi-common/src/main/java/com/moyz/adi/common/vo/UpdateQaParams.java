package com.moyz.adi.common.vo;

import com.moyz.adi.common.entity.KnowledgeBaseQa;
import com.moyz.adi.common.entity.User;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 更新Qa参数对象
 */
@Data
@Builder
public class UpdateQaParams {
    /**
     * 用户
     */
    private User user;
    /**
     * qaRecord
     */
    private KnowledgeBaseQa qaRecord;
    /**
     * SSEAsk参数
     */
    private SseAskParams sseAskParams;
    /**
     * retrievers
     */
    @Nullable
    private List<ContentRetriever> retrievers;
    /**
     * 响应
     */
    private String response;
    /**
     * 是否Token免费
     */
    private boolean isTokenFree;
}
