package com.moyz.adi.common.rag;

import com.moyz.adi.common.cosntant.AdiConstant;
import com.moyz.adi.common.dto.RefGraphDto;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.util.AdiStringUtil;
import com.moyz.adi.common.vo.*;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.filter.Filter;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.moyz.adi.common.enums.ErrorEnum.B_BREAK_SEARCH;
import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureGreaterThanZero;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * 基于图谱的内容检索器。
 */
@Slf4j
public class GraphStoreContentRetriever implements ContentRetriever {
    /**
     * 默认最大返回数量提供器。
     */
    public static final Function<Query, Integer> DEFAULT_MAX_RESULTS = query -> 3;
    /**
     * 默认过滤条件提供器。
     */
    public static final Function<Query, Filter> DEFAULT_FILTER = query -> null;

    /**
     * 默认显示名称。
     */
    public static final String DEFAULT_DISPLAY_NAME = "Default";

    /**
     * 图谱存储实现。
     */
    private final GraphStore graphStore;
    /**
     * 用于抽取实体关系的聊天模型。
     */
    private final ChatModel chatModel;

    /**
     * 最大返回数量提供器。
     */
    private final Function<Query, Integer> maxResultsProvider;
    /**
     * 过滤条件提供器。
     */
    private final Function<Query, Filter> filterProvider;

    /**
     * 显示名称。
     */
    private final String displayName;

    /**
     * 是否在未命中时中断流程。
     */
    private final boolean breakIfSearchMissed;

    /**
     * 图谱引用信息，便于回传给前端。
     */
    private final RefGraphDto kbQaRecordRefGraphDto = RefGraphDto.builder().vertices(Collections.emptyList()).edges(Collections.emptyList()).entitiesFromQuestion(Collections.emptyList()).build();

    /**
     * 构建图谱内容检索器。
     *
     * @param displayName 显示名称
     * @param graphStore 图谱存储实现
     * @param chatModel 抽取模型
     * @param dynamicMaxResults 最大返回数量提供器
     * @param dynamicFilter 过滤条件提供器
     * @param breakIfSearchMissed 是否在未命中时中断
     */
    @Builder
    private GraphStoreContentRetriever(String displayName,
                                       GraphStore graphStore,
                                       ChatModel chatModel,
                                       Function<Query, Integer> dynamicMaxResults,
                                       Function<Query, Filter> dynamicFilter,
                                       Boolean breakIfSearchMissed) {
        this.displayName = getOrDefault(displayName, DEFAULT_DISPLAY_NAME);
        this.graphStore = ensureNotNull(graphStore, "graphStore");
        this.chatModel = ensureNotNull(chatModel, "ChatModel");
        this.maxResultsProvider = getOrDefault(dynamicMaxResults, DEFAULT_MAX_RESULTS);
        this.filterProvider = getOrDefault(dynamicFilter, DEFAULT_FILTER);
        this.breakIfSearchMissed = breakIfSearchMissed;
    }

    /**
     * 使用指定图谱存储创建检索器。
     *
     * @param graphStore 图谱存储实现
     * @return 检索器实例
     */
    public static GraphStoreContentRetriever from(GraphStore graphStore) {
        return builder().graphStore(graphStore).build();
    }

    /**
     * 执行图谱检索并返回内容列表。
     *
     * @param query 查询参数
     * @return 内容列表
     * @throws BaseException 未命中且配置中断时抛出异常
     */
    @Override
    public List<Content> retrieve(Query query) {
        log.info("Graph retrieve,query:{}", query);
        String response = "";
        try {
            // 先调用模型从问题中抽取实体与关系
            response = chatModel.chat(GraphExtractPrompt.GRAPH_EXTRACTION_PROMPT.replace("{input_text}", query.text()));
        } catch (Exception e) {
            log.error("Graph retrieve. extract graph error", e);
        }
        if (StringUtils.isBlank(response)) {
            // 抽取为空时直接返回，避免无意义查询
            return Collections.emptyList();
        }
        Set<String> entities = new HashSet<>();
        String[] records = response.split(AdiConstant.GRAPH_RECORD_DELIMITER);
        for (String record : records) {
            String newRecord = record.replaceAll("^\\(|\\)$", "");
            String[] recordAttributes = newRecord.split(AdiConstant.GRAPH_TUPLE_DELIMITER);
            if (recordAttributes.length >= 4 && (recordAttributes[0].contains("\"entity\"") || recordAttributes[0].contains("\"实体\""))) {
                entities.add(AdiStringUtil.clearStr(recordAttributes[1].toUpperCase()));
            } else if (recordAttributes.length >= 4 && (recordAttributes[0].contains("\"relationship\"") || recordAttributes[0].contains("\"关系\""))) {
                String sourceName = AdiStringUtil.clearStr(recordAttributes[1].toUpperCase());
                String targetName = AdiStringUtil.clearStr(recordAttributes[2].toUpperCase());
                entities.add(AdiStringUtil.clearStr(sourceName));
                entities.add(AdiStringUtil.clearStr(targetName));
            }
        }
        // 未命中时直接中断流程，避免继续调用下游模型。
        if (breakIfSearchMissed && entities.isEmpty()) {
            log.warn("Graph search missed");
            throw new BaseException(B_BREAK_SEARCH);
        }
        entities = entities.stream().map(AdiStringUtil::removeSpecialChar).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        if (entities.isEmpty()) {
            log.info("从用户查询中没有解析出实体");
            return Collections.emptyList();
        }

        List<String> entityNames = entities.stream().toList();
        // 先检索与实体相关的顶点
        List<GraphVertex> vertices = graphStore.searchVertices(
                GraphVertexSearch.builder()
                        .names(entityNames)
                        .metadataFilter(filterProvider.apply(query))
                        .limit(maxResultsProvider.apply(query))
                        .build()
        );
        // 再补充边关系，形成更完整的图谱上下文
        List<Triple<GraphVertex, GraphEdge, GraphVertex>> edgeWithVerticeList = graphStore.searchEdges(
                GraphEdgeSearch.builder()
                        .edge(GraphSearchCondition.builder().metadataFilter(filterProvider.apply(query)).build())
                        .limit(maxResultsProvider.apply(query))
                        .build()
        );

        Map<String, GraphVertex> allVertices = new HashMap<>();
        List<GraphEdge> allEdges = new ArrayList<>();
        for (Triple<GraphVertex, GraphEdge, GraphVertex> triple : edgeWithVerticeList) {
            allVertices.put(triple.getLeft().getId(), triple.getLeft());
            allVertices.put(triple.getRight().getId(), triple.getRight());
            allEdges.add(triple.getMiddle());
        }
        allVertices.putAll(vertices.stream().collect(toMap(GraphVertex::getId, Function.identity())));
        // 缓存图谱引用信息，便于结果回传
        kbQaRecordRefGraphDto.setEntitiesFromQuestion(entityNames);
        kbQaRecordRefGraphDto.setVertices(allVertices.values().stream().toList());
        kbQaRecordRefGraphDto.setEdges(allEdges);

        // 将顶点与边描述统一转为内容列表供 RAG 使用
        List<Content> vertexContents = vertices.stream().map(GraphVertex::getDescription).map(Content::from).collect(toList());
        List<Content> edgeContents = edgeWithVerticeList.stream().map(Triple::getMiddle).map(GraphEdge::getDescription).map(Content::from).toList();
        vertexContents.addAll(edgeContents);
        return vertexContents;
    }

    /**
     * 获取本次检索的图谱引用信息。
     *
     * @return 引用信息
     */
    public RefGraphDto getGraphRef() {
        return kbQaRecordRefGraphDto;
    }

    /**
     * 自定义构建器，提供便捷配置。
     */
    public static class GraphStoreContentRetrieverBuilder {

        /**
         * 设置最大返回数量。
         *
         * @param maxResults 最大数量
         * @return 构建器
         */
        public GraphStoreContentRetrieverBuilder maxResults(Integer maxResults) {
            if (maxResults != null) {
                dynamicMaxResults = (query) -> ensureGreaterThanZero(maxResults, "maxResults");
            }
            return this;
        }

        /**
         * 设置过滤条件。
         *
         * @param filter 过滤条件
         * @return 构建器
         */
        public GraphStoreContentRetrieverBuilder filter(Filter filter) {
            if (filter != null) {
                dynamicFilter = (query) -> filter;
            }
            return this;
        }

        /**
         * 设置未命中时是否中断流程。
         *
         * @param breakFlag 是否中断
         * @return 构建器
         */
        public GraphStoreContentRetrieverBuilder breakIfSearchMissed(boolean breakFlag) {
            breakIfSearchMissed = breakFlag;
            return this;
        }
    }

    /**
     * 返回检索器字符串描述。
     *
     * @return 描述字符串
     */
    @Override
    public String toString() {
        return "GraphStoreContentRetriever{" +
               "displayName='" + displayName + '\'' +
               '}';
    }

}
