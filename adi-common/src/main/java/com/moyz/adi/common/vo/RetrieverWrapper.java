package com.moyz.adi.common.vo;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RetrieverWrapper对象
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RetrieverWrapper {
    /**
     * retriever
     */
    private ContentRetriever retriever;
    //Retrieve content from: knowledge_base conversation_memory web
    /**
     * 内容From
     */
    private String contentFrom;
    /**
     * 响应
     */
    private List<Content> response;
}
