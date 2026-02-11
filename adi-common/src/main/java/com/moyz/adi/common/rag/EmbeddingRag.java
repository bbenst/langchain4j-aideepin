package com.moyz.adi.common.rag;

import com.moyz.adi.common.interfaces.IRAGService;
import com.moyz.adi.common.util.InputAdaptor;
import com.moyz.adi.common.vo.InputAdaptorMsg;
import com.moyz.adi.common.vo.RetrieverCreateParam;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.moyz.adi.common.cosntant.AdiConstant.*;
import static com.moyz.adi.common.vo.InputAdaptorMsg.TOKEN_TOO_MUCH_QUESTION;

/**
 * 基于向量检索的 RAG 实现。
 */
@Slf4j
public class EmbeddingRag implements IRAGService {

    /**
     * RAG 名称，用于区分不同的实例。
     */
    @Getter
    private final String name;

    /**
     * 向量化模型。
     */
    private final EmbeddingModel embeddingModel;

    /**
     * 向量存储实现。
     */
    private final EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 构建向量 RAG 实例。
     *
     * @param name 实例名称
     * @param embeddingModel 向量化模型
     * @param embeddingStore 向量存储实现
     */
    public EmbeddingRag(String name, EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        this.name = name;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    /**
     * 对文档切块、向量化并存储到数据库。
     *
     * @param document 知识库文档
     * @param overlap 重叠 token 数
     * @param tokenEstimator token 估算器名称
     * @param ChatModel ChatModel 实例（兼容接口）
     * @return 无
     */
    @Override
    public void ingest(Document document, int overlap, String tokenEstimator, ChatModel ChatModel) {
        log.info("EmbeddingRag ingest,TokenCountEstimator:{}", tokenEstimator);
        // 使用递归分块策略，兼顾段落完整性与 token 上限
        DocumentSplitter documentSplitter = DocumentSplitters.recursive(RAG_MAX_SEGMENT_SIZE_IN_TOKENS, overlap, TokenEstimatorFactory.create(tokenEstimator));
        // 通过统一的 Ingestor 完成分块、向量化与入库
        EmbeddingStoreIngestor embeddingStoreIngestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(documentSplitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        embeddingStoreIngestor.ingest(document);
    }

    /**
     * 创建召回器。
     *
     * @param param 条件
     * @return ContentRetriever
     */
    @Override
    public AdiEmbeddingStoreContentRetriever createRetriever(RetrieverCreateParam param) {
        return AdiEmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(param.getMaxResults() <= 0 ? 3 : param.getMaxResults())
                .minScore(param.getMinScore() <= 0 ? RAG_MIN_SCORE : param.getMinScore())
                .filter(param.getFilter())
                .breakIfSearchMissed(param.isBreakIfSearchMissed())
                .build();
    }

    /**
     * 根据模型的 contentWindow 计算最多召回的文档数量。
     * <br/>以分块时的最大文本段对应的 token 数量 {maxSegmentSizeInTokens} 为计算因子。
     *
     * @param userQuestion 用户的问题
     * @param maxInputTokens AI 模型所能容纳的窗口大小
     * @return 召回的文档数量上限
     */
    public static int getRetrieveMaxResults(String userQuestion, int maxInputTokens) {
        // 模型未限制窗口时，使用系统默认上限
        if (maxInputTokens == 0) {
            return RAG_RETRIEVE_NUMBER_MAX;
        }
        InputAdaptorMsg inputAdaptorMsg = InputAdaptor.isQuestionValid(userQuestion, maxInputTokens);
        if (inputAdaptorMsg.getTokenTooMuch() == TOKEN_TOO_MUCH_QUESTION) {
            // 问题本身已超出窗口时，返回 0 表示不再召回
            log.warn("用户问题太长了，没有足够的token数量留给召回的内容");
            return 0;
        } else {
            // 用剩余 token 预算估算可容纳的文档段数量
            int maxRetrieveDocLength = maxInputTokens - inputAdaptorMsg.getUserQuestionTokenCount();
            if (maxRetrieveDocLength > RAG_RETRIEVE_NUMBER_MAX * RAG_MAX_SEGMENT_SIZE_IN_TOKENS) {
                // 超过系统上限时直接按最大值截断
                return RAG_RETRIEVE_NUMBER_MAX;
            } else {
                // 按块大小换算为可召回的段数量
                return maxRetrieveDocLength / RAG_MAX_SEGMENT_SIZE_IN_TOKENS;
            }
        }

    }
}
