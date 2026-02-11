package com.moyz.adi.common.rag;

import com.moyz.adi.common.cosntant.AdiConstant;
import com.moyz.adi.common.enums.ErrorEnum;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.util.AdiStringUtil;
import com.moyz.adi.common.vo.*;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.DocumentTransformer;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.segment.TextSegmentTransformer;
import dev.langchain4j.spi.data.document.splitter.DocumentSplitterFactory;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.moyz.adi.common.cosntant.AdiConstant.MAX_METADATA_VALUE_LENGTH;
import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static dev.langchain4j.spi.ServiceHelper.loadFactories;
import static java.util.Collections.singletonList;

/**
 * 图谱入库器，将文档切分、抽取并写入图谱存储。
 */
@Builder
@AllArgsConstructor
@Slf4j
public class GraphStoreIngestor {

    /**
     * 文档转换器，用于入库前预处理。
     */
    private final DocumentTransformer documentTransformer;
    /**
     * 文本分段转换器，用于分段后处理。
     */
    private final TextSegmentTransformer textSegmentTransformer;
    /**
     * 图谱存储实现。
     */
    private final GraphStore graphStore;
    /**
     * 文档分段器。
     */
    private final DocumentSplitter documentSplitter;
    /**
     * 分段抽取函数，返回分段、分段 ID 与抽取结果。
     */
    private final Function<List<TextSegment>, List<Triple<TextSegment, String, String>>> segmentsFunction;

    /**
     * 查询时 where 语句的条件字段名。
     */
    private final List<String> identifyColumns;

    /**
     * 更新时 set 语句的追加字段名，值为数组类型。
     * 如已存在数据 kb_item_uuids=['ab']，则更新时对该字段追加新数据，最终结果为 kb_item_uuids=['ab','cd']。
     */
    private final List<String> appendColumns;

    /**
     * 构建图谱入库器。
     *
     * @param documentTransformer 文档转换器
     * @param documentSplitter 文档分段器
     * @param graphStore 图谱存储实现
     * @param textSegmentTransformer 文本分段转换器
     * @param segmentsFunction 分段抽取函数
     * @param identifyColumns 用于定位记录的字段名集合（逗号分隔）
     * @param appendColumns 追加字段名集合（逗号分隔）
     */
    public GraphStoreIngestor(DocumentTransformer documentTransformer,
                              DocumentSplitter documentSplitter,
                              GraphStore graphStore,
                              TextSegmentTransformer textSegmentTransformer,
                              Function<List<TextSegment>, List<Triple<TextSegment, String, String>>> segmentsFunction,
                              String identifyColumns,
                              String appendColumns) {
        this.graphStore = ensureNotNull(graphStore, "graphStore");
        this.documentTransformer = documentTransformer;
        this.documentSplitter = getOrDefault(documentSplitter, GraphStoreIngestor::loadDocumentSplitter);
        this.textSegmentTransformer = textSegmentTransformer;
        this.segmentsFunction = segmentsFunction;
        this.identifyColumns = Arrays.asList(identifyColumns.split(","));
        this.appendColumns = Arrays.asList(appendColumns.split(","));
    }

    /**
     * 通过 SPI 加载默认文档分段器。
     *
     * @return 文档分段器
     */
    private static DocumentSplitter loadDocumentSplitter() {
        Collection<DocumentSplitterFactory> factories = loadFactories(DocumentSplitterFactory.class);
        if (factories.size() > 1) {
            throw new RuntimeException("Conflict: multiple document splitters have been found in the classpath. " +
                                       "Please explicitly specify the one you wish to use.");
        }

        for (DocumentSplitterFactory factory : factories) {
            DocumentSplitter documentSplitter = factory.create();
            log.debug("Loaded the following document splitter through SPI: {}", documentSplitter);
            return documentSplitter;
        }

        return null;
    }

    /**
     * 入库单个文档。
     *
     * @param document 文档
     * @return 无
     */
    public void ingest(Document document) {
        ingest(singletonList(document));
    }

    /**
     * 入库文档列表。
     *
     * @param documents 文档列表
     * @return 无
     */
    public void ingest(List<Document> documents) {

        log.info("Starting to ingest {} documents", documents.size());

        if (documentTransformer != null) {
            // 先进行全量预处理，保证后续切分输入一致
            documents = documentTransformer.transformAll(documents);
            log.info("Documents were transformed into {} documents", documents.size());
        }
        List<TextSegment> segments;
        if (documentSplitter != null) {
            // 优先使用自定义分段器，确保切分策略可控
            segments = documentSplitter.splitAll(documents);
            log.info("Documents were split into {} text segments", segments.size());
        } else {
            // 无分段器时退化为单段处理
            segments = documents.stream()
                    .map(Document::toTextSegment)
                    .toList();
        }
        if (textSegmentTransformer != null) {
            // 分段后统一做二次处理（清洗、补元数据等）
            segments = textSegmentTransformer.transformAll(segments);
            log.info("Text segments were transformed into {} text segments", documents.size());
        }

        // 待办：失败重试与并行化优化。
        log.info("Starting to extract {} text segments", segments.size());
        // 交由业务侧抽取实体/关系并返回结构化结果
        List<Triple<TextSegment, String, String>> segmentIdToAiResponse = segmentsFunction.apply(segments);
        for (Triple<TextSegment, String, String> triple : segmentIdToAiResponse) {
            TextSegment segment = triple.getLeft();
            String textSegmentId = triple.getMiddle();
            String response = triple.getRight();
            Map<String, Object> metadata = segment.metadata().toMap();
            log.info("Finished extract {} text segments", segments.size());
            log.info("graph response:{}", response);
            // 待办：失败重试与并行化优化。
            log.info("Starting to store {} text segments into the graph store", segments.size());
            if (StringUtils.isBlank(response)) {
                // 抽取结果为空直接结束，避免写入无效图谱数据
                log.warn("Response is empty");
                log.info("Finished storing {} text segments into the graph store", segments.size());
                return;
            }

            Filter filter = null;
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                // 仅使用标识列构建过滤条件，避免跨知识库污染
                boolean contain = identifyColumns.contains(entry.getKey());
                if (contain) {
                    if (null == filter) {
                        filter = new IsEqualTo(entry.getKey(), entry.getValue());
                    } else {
                        filter = filter.and(new IsEqualTo(entry.getKey(), entry.getValue()));
                    }
                }
            }
            if (null == filter) {
                // 无过滤条件时无法定位图谱范围，直接拒绝写入
                throw new BaseException(ErrorEnum.B_GRAPH_FILTER_NOT_FOUND);
            }

            // 抽取结果为多行记录，需逐条解析后写入图谱。
            String[] rows = StringUtils.split(response, AdiConstant.GRAPH_RECORD_DELIMITER);
            for (String row : rows) {
                String graphRow = row;
                // 清理首尾括号，兼容模型输出格式
                graphRow = graphRow.replaceAll("^\\(|\\)$", "");
                String[] recordAttributes = StringUtils.split(graphRow, AdiConstant.GRAPH_TUPLE_DELIMITER);
                if (recordAttributes.length >= 4 && (recordAttributes[0].contains("\"entity\"") || recordAttributes[0].contains("\"实体\""))) {
                    // 实体记录：名称/类型/描述
                    String entityName = AdiStringUtil.clearStr(recordAttributes[1].toUpperCase());
                    String entityType = AdiStringUtil.clearStr(recordAttributes[2].toUpperCase()).replaceAll("[^a-zA-Z0-9\\s\\u4E00-\\u9FA5]+", "").replace(" ", "");
                    String entityDescription = AdiStringUtil.clearStr(recordAttributes[3]);
                    log.info("entityName:{},entityType:{},entityDescription:{}", entityName, entityType, entityDescription);
                    // 实体不存在则新增，存在则追加分段与描述等信息。
                    List<GraphVertex> existVertices = graphStore.searchVertices(
                            GraphVertexSearch.builder()
                                    .label(entityType)
                                    .limit(1)
                                    .names(List.of(entityName))
                                    .metadataFilter(filter)
                                    .build()
                    );
                    if (CollectionUtils.isNotEmpty(existVertices)) {
                        GraphVertex existVertex = existVertices.get(0);
                        // 追加分段与描述，保留历史信息
                        String newTextSegmentId = existVertex.getTextSegmentId() + "," + textSegmentId;
                        String newDesc = existVertex.getDescription() + "\n" + entityDescription;

                        // 合并可追加的元数据字段，保持可追溯
                        appendExistsToNewOne(existVertex.getMetadata(), metadata);
                        GraphVertex newData = GraphVertex.builder().textSegmentId(newTextSegmentId).description(newDesc).metadata(metadata).build();
                        graphStore.updateVertex(
                                GraphVertexUpdateInfo.builder()
                                        .name(entityName)
                                        .metadataFilter(filter)
                                        .newData(newData)
                                        .build()
                        );
                    } else {
                        // 新实体直接写入
                        graphStore.addVertex(
                                GraphVertex.builder()
                                        .label(entityType)
                                        .name(entityName)
                                        .textSegmentId(textSegmentId)
                                        .description(entityDescription)
                                        .metadata(metadata)
                                        .build()
                        );
                    }
                } else if (recordAttributes.length >= 4 && (recordAttributes[0].contains("\"relationship\"") || recordAttributes[0].contains("\"关系\""))) {
                    // 关系记录：起点/终点/描述/权重
                    String sourceName = AdiStringUtil.clearStr(recordAttributes[1].toUpperCase());
                    String targetName = AdiStringUtil.clearStr(recordAttributes[2].toUpperCase());
                    String edgeDescription = AdiStringUtil.clearStr(recordAttributes[3]);
                    log.info("Relationship sourceName:{},targetName:{},edgeDescription:{}", sourceName, targetName, edgeDescription);
                    String chunkId = AdiStringUtil.clearStr(textSegmentId);

                    double weight = 1.0;
                    if (recordAttributes.length > 4) {
                        // 提取权重字段，默认权重为 1
                        String tailRecord = recordAttributes[recordAttributes.length - 1];
                        weight = NumberUtils.toDouble(tailRecord, 1.0);
                    }

                    // 源节点
                    GraphVertex source = graphStore.getVertex(
                            GraphVertexSearch.builder()
                                    .names(List.of(sourceName))
                                    .metadataFilter(filter)
                                    .build()
                    );
                    if (null == source) {
                        // 源节点不存在则创建，确保边关系可用
                        graphStore.addVertex(
                                GraphVertex.builder()
                                        .name(sourceName)
                                        .textSegmentId(chunkId)
                                        .metadata(metadata)
                                        .build()
                        );
                    }
                    // 目标节点
                    GraphVertex target = graphStore.getVertex(
                            GraphVertexSearch.builder()
                                    .names(List.of(targetName))
                                    .metadataFilter(filter)
                                    .build()
                    );
                    if (null == target) {
                        // 目标节点不存在则创建，确保边关系可用
                        graphStore.addVertex(
                                GraphVertex.builder()
                                        .name(targetName)
                                        .textSegmentId(chunkId)
                                        .metadata(metadata)
                                        .build()
                        );
                    }
                    // 边关系
                    GraphEdgeSearch search = new GraphEdgeSearch();
                    search.setSource(GraphSearchCondition.builder()
                            .names(List.of(sourceName))
                            .metadataFilter(filter)
                            .build());
                    search.setTarget(GraphSearchCondition.builder()
                            .names(List.of(targetName))
                            .metadataFilter(filter)
                            .build());
                    Triple<GraphVertex, GraphEdge, GraphVertex> graphEdgeWithVertices = graphStore.getEdge(search);
                    if (null != graphEdgeWithVertices) {
                        // 已存在边则累加权重并追加描述
                        GraphEdge existGraphEdge = graphEdgeWithVertices.getMiddle();
                        weight += existGraphEdge.getWeight();
                        GraphEdgeEditInfo graphEdgeEditInfo = new GraphEdgeEditInfo();
                        graphEdgeEditInfo.setSourceFilter(GraphSearchCondition.builder()
                                .names(List.of(sourceName))
                                .metadataFilter(filter)
                                .build());
                        graphEdgeEditInfo.setTargetFilter(GraphSearchCondition.builder()
                                .names(List.of(targetName))
                                .metadataFilter(filter)
                                .build());
                        graphEdgeEditInfo.setEdge(GraphEdge.builder()
                                .textSegmentId(existGraphEdge.getTextSegmentId() + "," + chunkId)
                                .description(existGraphEdge.getDescription() + "\n" + edgeDescription)
                                .weight(weight)
                                .build());
                        graphStore.updateEdge(graphEdgeEditInfo);

                        // 合并可追加的元数据字段
                        appendExistsToNewOne(existGraphEdge.getMetadata(), metadata);
                    } else {
                        // 检查节点是否存在，不存在则创建。
                        checkOrCreateVertex("", sourceName, chunkId, filter, metadata);
                        checkOrCreateVertex("", targetName, chunkId, filter, metadata);
                        // 新增边关系
                        GraphEdgeAddInfo addInfo = new GraphEdgeAddInfo();
                        addInfo.setEdge(GraphEdge.builder()
                                .sourceName(sourceName)
                                .targetName(targetName)
                                .weight(weight)
                                .metadata(metadata)
                                .textSegmentId(chunkId)
                                .description(edgeDescription)
                                .build());
                        addInfo.setSourceFilter(GraphSearchCondition.builder()
                                .names(List.of(sourceName))
                                .metadataFilter(filter)
                                .build());
                        addInfo.setTargetFilter(GraphSearchCondition.builder()
                                .names(List.of(targetName))
                                .metadataFilter(filter)
                                .build());
                        graphStore.addEdge(addInfo);
                    }
                }
            }
        }

        log.info("Finished storing {} text segments into the graph store", segments.size());
    }

    /**
     * metadata 记录的值为 Map，如：kb_uuid=>123,kb_item_uuid=>22222,3333，其中类似 3333 的值是追加的，
     * 超过最大限度时丢弃最早的数据。
     * 待办：重构以记录所有追加的值。
     *
     * @param existMetadata 已存在的 metadata
     * @param newMetadata 新的 metadata
     * @return 无
     */
    private void appendExistsToNewOne(Map<String, Object> existMetadata, Map<String, Object> newMetadata) {
        for (String columnName : appendColumns) {
            String existValue = String.valueOf(existMetadata.get(columnName));
            String newValue = String.valueOf(newMetadata.get(columnName));
            if (StringUtils.isNotBlank(existValue) && !existValue.contains(newValue)) {
                // 追加时先清洗无效字符，再检查长度上限
                String cleanedTxt = existValue.replaceAll("[\\s\"/\\\\]", "");
                newMetadata.put(columnName, checkAndRemoveOldest(cleanedTxt) + "," + newValue);
            }
        }
    }

    /**
     * 当追加值过长时移除最早的数据，保证长度不超过上限。
     *
     * @param cleanedTxt 清洗后的文本
     * @return 处理后的文本
     */
    private String checkAndRemoveOldest(String cleanedTxt) {
        if (StringUtils.isBlank(cleanedTxt)) {
            return cleanedTxt;
        }
        String result = cleanedTxt;
        while (result.length() > MAX_METADATA_VALUE_LENGTH) {
            String[] existValues = result.split(",", 2); // 仅拆分为两部分，保留后续所有内容。
            if (existValues.length <= 1) {
                return result.substring(0, MAX_METADATA_VALUE_LENGTH);
            }
            result = existValues[1]; // 保留第一个逗号后的内容。
        }
        return result;
    }

    /**
     * 检查节点是否存在，不存在则创建。
     *
     * @param label 节点标签
     * @param name 节点名称
     * @param textSegmentId 分段 ID
     * @param metadataFilter 元数据过滤条件
     * @param metadata 元数据
     * @return 无
     */
    private void checkOrCreateVertex(String label, String name, String textSegmentId, Filter metadataFilter, Map<String, Object> metadata) {
        List<GraphVertex> existVertices = graphStore.searchVertices(
                GraphVertexSearch.builder()
                        .label(label)
                        .limit(1)
                        .names(List.of(name))
                        .metadataFilter(metadataFilter)
                        .build()
        );
        if (CollectionUtils.isEmpty(existVertices)) {
            graphStore.addVertex(
                    GraphVertex.builder()
                            .label(label)
                            .name(name)
                            .textSegmentId(textSegmentId)
                            .metadata(metadata)
                            .build()
            );
        }
    }
}
