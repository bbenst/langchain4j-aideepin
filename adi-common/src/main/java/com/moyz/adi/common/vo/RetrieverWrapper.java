package com.moyz.adi.common.vo;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 检索器包装对象，用于标记来源与保留响应内容。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RetrieverWrapper {
    /**
     * 具体检索器实现。
     */
    private ContentRetriever retriever;
    /**
     * 检索来源：knowledge_base、conversation_memory、web 等。
     */
    /**
     * 内容来源标识。
     */
    private String contentFrom;
    /**
     * 检索响应内容。
     */
    private List<Content> response;
}
