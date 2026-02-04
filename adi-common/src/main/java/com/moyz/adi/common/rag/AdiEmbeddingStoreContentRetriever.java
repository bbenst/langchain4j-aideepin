package com.moyz.adi.common.rag;

import com.moyz.adi.common.exception.BaseException;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.spi.model.embedding.EmbeddingModelFactory;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.function.Function;

import static com.moyz.adi.common.enums.ErrorEnum.B_BREAK_SEARCH;
import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.*;
import static dev.langchain4j.spi.ServiceHelper.loadFactories;
import static java.util.stream.Collectors.toList;

/**
 * 向量检索内容召回器，基于 EmbeddingStoreContentRetriever 做了定制。
 * 主要改动：缓存命中的向量及分数，便于后续落库。
 */
@Slf4j
public class AdiEmbeddingStoreContentRetriever implements ContentRetriever {

    /**
     * 默认最大返回数量提供器。
     */
    public static final Function<Query, Integer> DEFAULT_MAX_RESULTS = (query) -> 3;
    /**
     * 默认最小相似度提供器。
     */
    public static final Function<Query, Double> DEFAULT_MIN_SCORE = (query) -> 0.0;
    /**
     * 默认过滤条件提供器。
     */
    public static final Function<Query, Filter> DEFAULT_FILTER = (query) -> null;

    /**
     * 默认显示名称。
     */
    public static final String DEFAULT_DISPLAY_NAME = "Default";

    /**
     * 向量存储实现。
     */
    private final EmbeddingStore<TextSegment> embeddingStore;
    /**
     * 向量化模型。
     */
    private final EmbeddingModel embeddingModel;

    /**
     * 最大返回数量提供器。
     */
    private final Function<Query, Integer> maxResultsProvider;
    /**
     * 最小相似度提供器。
     */
    private final Function<Query, Double> minScoreProvider;
    /**
     * 过滤条件提供器。
     */
    private final Function<Query, Filter> filterProvider;

    /**
     * 显示名称。
     */
    private final String displayName;

    /**
     * 命中的向量及对应的分数。
     */
    private final Map<String, Double> embeddingToScore = new HashMap<>();

    /**
     * 是否在未命中时中断流程。
     */
    private final boolean breakIfSearchMissed;

    /**
     * 构建默认检索器。
     *
     * @param embeddingStore 向量存储实现
     * @param embeddingModel 向量化模型
     */
    public AdiEmbeddingStoreContentRetriever(EmbeddingStore<TextSegment> embeddingStore,
                                             EmbeddingModel embeddingModel) {
        this(
                DEFAULT_DISPLAY_NAME,
                embeddingStore,
                embeddingModel,
                DEFAULT_MAX_RESULTS,
                DEFAULT_MIN_SCORE,
                DEFAULT_FILTER,
                false
        );
    }

    /**
     * 构建指定最大返回数量的检索器。
     *
     * @param embeddingStore 向量存储实现
     * @param embeddingModel 向量化模型
     * @param maxResults 最大返回数量
     */
    public AdiEmbeddingStoreContentRetriever(EmbeddingStore<TextSegment> embeddingStore,
                                             EmbeddingModel embeddingModel,
                                             int maxResults) {
        this(
                DEFAULT_DISPLAY_NAME,
                embeddingStore,
                embeddingModel,
                (query) -> maxResults,
                DEFAULT_MIN_SCORE,
                DEFAULT_FILTER,
                false
        );
    }

    /**
     * 构建指定最大返回数量与最小相似度的检索器。
     *
     * @param embeddingStore 向量存储实现
     * @param embeddingModel 向量化模型
     * @param maxResults 最大返回数量
     * @param minScore 最小相似度
     */
    public AdiEmbeddingStoreContentRetriever(EmbeddingStore<TextSegment> embeddingStore,
                                             EmbeddingModel embeddingModel,
                                             Integer maxResults,
                                             Double minScore) {
        this(
                DEFAULT_DISPLAY_NAME,
                embeddingStore,
                embeddingModel,
                (query) -> maxResults,
                (query) -> minScore,
                DEFAULT_FILTER,
                false
        );
    }

    /**
     * 内部构建方法。
     *
     * @param displayName 显示名称
     * @param embeddingStore 向量存储实现
     * @param embeddingModel 向量化模型
     * @param dynamicMaxResults 最大返回数量提供器
     * @param dynamicMinScore 最小相似度提供器
     * @param dynamicFilter 过滤条件提供器
     * @param breakIfSearchMissed 是否在未命中时中断
     */
    private AdiEmbeddingStoreContentRetriever(String displayName,
                                              EmbeddingStore<TextSegment> embeddingStore,
                                              EmbeddingModel embeddingModel,
                                              Function<Query, Integer> dynamicMaxResults,
                                              Function<Query, Double> dynamicMinScore,
                                              Function<Query, Filter> dynamicFilter,
                                              Boolean breakIfSearchMissed) {
        this.displayName = getOrDefault(displayName, DEFAULT_DISPLAY_NAME);
        this.embeddingStore = ensureNotNull(embeddingStore, "embeddingStore");
        this.embeddingModel = ensureNotNull(
                getOrDefault(embeddingModel, AdiEmbeddingStoreContentRetriever::loadEmbeddingModel),
                "embeddingModel"
        );
        this.maxResultsProvider = getOrDefault(dynamicMaxResults, DEFAULT_MAX_RESULTS);
        this.minScoreProvider = getOrDefault(dynamicMinScore, DEFAULT_MIN_SCORE);
        this.filterProvider = getOrDefault(dynamicFilter, DEFAULT_FILTER);
        this.breakIfSearchMissed = breakIfSearchMissed;
    }

    /**
     * 通过 SPI 加载默认向量化模型。
     *
     * @return 向量化模型
     */
    private static EmbeddingModel loadEmbeddingModel() {
        Collection<EmbeddingModelFactory> factories = loadFactories(EmbeddingModelFactory.class);
        if (factories.size() > 1) {
            throw new RuntimeException("Conflict: multiple embedding models have been found in the classpath. " +
                    "Please explicitly specify the one you wish to use.");
        }

        for (EmbeddingModelFactory factory : factories) {
            return factory.create();
        }

        return null;
    }

    /**
     * 创建构建器。
     *
     * @return 构建器
     */
    public static AdiEmbeddingStoreContentRetriever.AdiEmbeddingStoreContentRetrieverBuilder builder() {
        return new AdiEmbeddingStoreContentRetriever.AdiEmbeddingStoreContentRetrieverBuilder();
    }

    /**
     * 构建器。
     */
    public static class AdiEmbeddingStoreContentRetrieverBuilder {

        /**
         * 显示名称。
         */
        private String displayName;
        /**
         * 向量存储实现。
         */
        private EmbeddingStore<TextSegment> embeddingStore;
        /**
         * 向量化模型。
         */
        private EmbeddingModel embeddingModel;
        /**
         * 最大返回数量提供器。
         */
        private Function<Query, Integer> dynamicMaxResults;
        /**
         * 最小相似度提供器。
         */
        private Function<Query, Double> dynamicMinScore;
        /**
         * 过滤条件提供器。
         */
        private Function<Query, Filter> dynamicFilter;

        /**
         * 是否在未命中时中断流程。
         */
        private Boolean breakIfSearchMissed;

        AdiEmbeddingStoreContentRetrieverBuilder() {
        }

        /**
         * 设置最大返回数量。
         *
         * @param maxResults 最大数量
         * @return 构建器
         */
        public AdiEmbeddingStoreContentRetriever.AdiEmbeddingStoreContentRetrieverBuilder maxResults(Integer maxResults) {
            if (maxResults != null) {
                dynamicMaxResults = (query) -> ensureGreaterThanZero(maxResults, "maxResults");
            }
            return this;
        }

        /**
         * 设置最小相似度。
         *
         * @param minScore 最小相似度
         * @return 构建器
         */
        public AdiEmbeddingStoreContentRetriever.AdiEmbeddingStoreContentRetrieverBuilder minScore(Double minScore) {
            if (minScore != null) {
                dynamicMinScore = (query) -> ensureBetween(minScore, 0, 1, "minScore");
            }
            return this;
        }

        /**
         * 设置过滤条件。
         *
         * @param filter 过滤条件
         * @return 构建器
         */
        public AdiEmbeddingStoreContentRetriever.AdiEmbeddingStoreContentRetrieverBuilder filter(Filter filter) {
            if (filter != null) {
                dynamicFilter = (query) -> filter;
            }
            return this;
        }

        /**
         * 设置显示名称。
         *
         * @param displayName 显示名称
         * @return 构建器
         */
        public AdiEmbeddingStoreContentRetriever.AdiEmbeddingStoreContentRetrieverBuilder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * 设置向量存储实现。
         *
         * @param embeddingStore 向量存储实现
         * @return 构建器
         */
        public AdiEmbeddingStoreContentRetriever.AdiEmbeddingStoreContentRetrieverBuilder embeddingStore(EmbeddingStore<TextSegment> embeddingStore) {
            this.embeddingStore = embeddingStore;
            return this;
        }

        /**
         * 设置向量化模型。
         *
         * @param embeddingModel 向量化模型
         * @return 构建器
         */
        public AdiEmbeddingStoreContentRetriever.AdiEmbeddingStoreContentRetrieverBuilder embeddingModel(EmbeddingModel embeddingModel) {
            this.embeddingModel = embeddingModel;
            return this;
        }

        /**
         * 设置最大返回数量提供器。
         *
         * @param dynamicMaxResults 最大返回数量提供器
         * @return 构建器
         */
        public AdiEmbeddingStoreContentRetriever.AdiEmbeddingStoreContentRetrieverBuilder dynamicMaxResults(Function<Query, Integer> dynamicMaxResults) {
            this.dynamicMaxResults = dynamicMaxResults;
            return this;
        }

        /**
         * 设置最小相似度提供器。
         *
         * @param dynamicMinScore 最小相似度提供器
         * @return 构建器
         */
        public AdiEmbeddingStoreContentRetriever.AdiEmbeddingStoreContentRetrieverBuilder dynamicMinScore(Function<Query, Double> dynamicMinScore) {
            this.dynamicMinScore = dynamicMinScore;
            return this;
        }

        /**
         * 设置过滤条件提供器。
         *
         * @param dynamicFilter 过滤条件提供器
         * @return 构建器
         */
        public AdiEmbeddingStoreContentRetriever.AdiEmbeddingStoreContentRetrieverBuilder dynamicFilter(Function<Query, Filter> dynamicFilter) {
            this.dynamicFilter = dynamicFilter;
            return this;
        }

        /**
         * 设置未命中时是否中断流程。
         *
         * @param breakIfSearchMissed 是否中断
         * @return 构建器
         */
        public AdiEmbeddingStoreContentRetrieverBuilder breakIfSearchMissed(boolean breakIfSearchMissed) {
            this.breakIfSearchMissed = breakIfSearchMissed;
            return this;
        }

        /**
         * 构建检索器。
         *
         * @return 检索器实例
         */
        public AdiEmbeddingStoreContentRetriever build() {
            return new AdiEmbeddingStoreContentRetriever(this.displayName, this.embeddingStore, this.embeddingModel, this.dynamicMaxResults, this.dynamicMinScore, this.dynamicFilter, this.breakIfSearchMissed);
        }


        /**
         * 输出调试信息。
         *
         * @return 描述字符串
         */
        public String toString() {
            return "AdiEmbeddingStoreContentRetriever.AdiEmbeddingStoreContentRetrieverBuilder(displayName=" + this.displayName + ", embeddingStore=" + this.embeddingStore + ", embeddingModel=" + this.embeddingModel + ", dynamicMaxResults=" + this.dynamicMaxResults + ", dynamicMinScore=" + this.dynamicMinScore + ", dynamicFilter=" + this.dynamicFilter + ", breakIfSearchMissed=" + this.breakIfSearchMissed + ")";
        }
    }

    /**
     * 使用指定的向量存储创建检索器，向量模型通过 SPI 加载。
     *
     * @param embeddingStore 向量存储实现
     * @return 检索器实例
     */
    public static AdiEmbeddingStoreContentRetriever from(EmbeddingStore<TextSegment> embeddingStore) {
        return builder().embeddingStore(embeddingStore).build();
    }

    /**
     * 执行向量检索。
     *
     * @param query 查询参数
     * @return 内容列表
     */
    @Override
    public List<Content> retrieve(Query query) {

        Embedding embeddedQuery = embeddingModel.embed(query.text()).content();

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(embeddedQuery)
                .maxResults(maxResultsProvider.apply(query))
                .minScore(minScoreProvider.apply(query))
                .filter(filterProvider.apply(query))
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);

        List<Content> result = searchResult.matches().stream()
                .peek(item -> {
                    embeddingToScore.put(item.embeddingId(), item.score());
                    log.info("embeddingToScore,embeddingId:{},score:{}", item.embeddingId(), item.score());
                })
                .map(EmbeddingMatch::embedded)
                .map(Content::from)
                .collect(toList());

        // 未命中时直接中断流程，避免继续调用下游模型。
        if (breakIfSearchMissed && CollectionUtils.isEmpty(result)) {
            log.warn("Embedding search missed,query:{}", query.text());
            throw new BaseException(B_BREAK_SEARCH);
        }
        return result;
    }

    /**
     * 获取检索命中的向量及分数。
     *
     * @return 向量 ID 与分数映射
     */
    public Map<String, Double> getRetrievedEmbeddingToScore() {
        return this.embeddingToScore;
    }

    @Override
    public String toString() {
        return "AdiEmbeddingStoreContentRetriever{" +
                "displayName='" + displayName + '\'' +
                '}';
    }
}
