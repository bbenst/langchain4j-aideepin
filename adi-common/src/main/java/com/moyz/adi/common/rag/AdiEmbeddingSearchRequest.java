package com.moyz.adi.common.rag;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.filter.Filter;

import java.util.List;

/**
 * 扩展向量检索请求，支持指定 ID 列表过滤。
 */
public class AdiEmbeddingSearchRequest extends EmbeddingSearchRequest {

    /**
     * 需要限定的分段或记录 ID 列表。
     */
    private List<String> ids;

    /**
     * 构建检索请求。
     *
     * @param ids ID 列表
     * @param queryEmbedding 查询向量
     * @param maxResults 最大返回数量
     * @param minScore 最小相似度
     * @param filter 过滤条件
     */
    public AdiEmbeddingSearchRequest(List<String> ids, Embedding queryEmbedding, Integer maxResults, Double minScore, Filter filter) {
        super(queryEmbedding, maxResults, minScore, filter);
        this.ids = ids;
    }

    /**
     * 获取 ID 列表。
     *
     * @return ID 列表
     */
    public List<String> getIds() {
        return ids;
    }

    /**
     * 设置 ID 列表。
     *
     * @param ids ID 列表
     */
    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    /**
     * 创建构建器。
     *
     * @return 构建器
     */
    public static AdiEmbeddingSearchRequestBuilder adiBuilder() {
        return new AdiEmbeddingSearchRequestBuilder();
    }

    /**
     * 构建器。
     */
    public static class AdiEmbeddingSearchRequestBuilder {
        /**
         * 需要限定的 ID 列表。
         */
        private List<String> ids;
        /**
         * 查询向量。
         */
        private Embedding queryEmbedding;
        /**
         * 最大返回数量。
         */
        private Integer maxResults;
        /**
         * 最小相似度阈值。
         */
        private Double minScore;
        /**
         * 过滤条件。
         */
        private Filter filter;

        AdiEmbeddingSearchRequestBuilder() {
        }

        /**
         * 设置 ID 列表。
         *
         * @param ids ID 列表
         * @return 构建器
         */
        public AdiEmbeddingSearchRequestBuilder ids(List<String> ids) {
            this.ids = ids;
            return this;
        }

        /**
         * 设置查询向量。
         *
         * @param queryEmbedding 查询向量
         * @return 构建器
         */
        public AdiEmbeddingSearchRequestBuilder queryEmbedding(Embedding queryEmbedding) {
            this.queryEmbedding = queryEmbedding;
            return this;
        }

        /**
         * 设置最大返回数量。
         *
         * @param maxResults 最大数量
         * @return 构建器
         */
        public AdiEmbeddingSearchRequestBuilder maxResults(Integer maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        /**
         * 设置最小相似度。
         *
         * @param minScore 最小相似度
         * @return 构建器
         */
        public AdiEmbeddingSearchRequestBuilder minScore(Double minScore) {
            this.minScore = minScore;
            return this;
        }

        /**
         * 设置过滤条件。
         *
         * @param filter 过滤条件
         * @return 构建器
         */
        public AdiEmbeddingSearchRequestBuilder filter(Filter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * 构建请求对象。
         *
         * @return 请求对象
         */
        public AdiEmbeddingSearchRequest build() {
            return new AdiEmbeddingSearchRequest(this.ids, this.queryEmbedding, this.maxResults, this.minScore, this.filter);
        }

        /**
         * 输出调试信息。
         *
         * @return 描述字符串
         */
        public String toString() {
            return "AdiEmbeddingSearchRequest.AdiEmbeddingSearchRequestBuilder(queryEmbedding=" + this.queryEmbedding + ", maxResults=" + this.maxResults + ", minScore=" + this.minScore + ", filter=" + this.filter + ")";
        }
    }
}
