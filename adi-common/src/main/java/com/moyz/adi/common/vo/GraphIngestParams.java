package com.moyz.adi.common.vo;

import com.moyz.adi.common.entity.User;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.model.chat.ChatModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 图谱Ingest参数对象
 */
@Data
@Builder
public class GraphIngestParams {
    /**
     * 用户
     */
    private User user;
    /**
     * document
     */
    private Document document;
    /**
     * overlap
     */
    private int overlap;
    /**
     * TokenEstimator
     */
    private String tokenEstimator;
    /**
     * 聊天模型
     */
    private ChatModel ChatModel;
    /**
     * identifyColumns
     */
    private List<String> identifyColumns;
    /**
     * appendColumns
     */
    private List<String> appendColumns;
    /**
     * 是否免费Token
     */
    private boolean isFreeToken;
}
