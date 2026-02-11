package com.moyz.adi.common.rag;

import com.moyz.adi.common.cosntant.AdiConstant;
import com.moyz.adi.common.entity.KnowledgeBaseGraphSegment;
import com.moyz.adi.common.entity.User;
import com.moyz.adi.common.enums.ErrorEnum;
import com.moyz.adi.common.helper.QuotaHelper;
import com.moyz.adi.common.service.KnowledgeBaseGraphSegmentService;
import com.moyz.adi.common.service.UserDayCostService;
import com.moyz.adi.common.util.SpringUtil;
import com.moyz.adi.common.util.UuidUtil;
import com.moyz.adi.common.vo.GraphIngestParams;
import com.moyz.adi.common.vo.RetrieverCreateParam;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;

import static com.moyz.adi.common.cosntant.AdiConstant.RAG_MAX_SEGMENT_SIZE_IN_TOKENS;

/**
 * 知识图谱 RAG，基于图谱存储进行问答增强。
 */
@Slf4j
public class GraphRag {

    /**
     * RAG 名称，用于区分不同实例。
     */
    @Getter
    private final String name;

    /**
     * 图谱存储适配器，负责读写图谱数据。
     */
    private final GraphStore graphStore;

    /**
     * 图谱分段持久化服务，使用时延迟获取。
     */
    private KnowledgeBaseGraphSegmentService knowledgeBaseGraphSegmentService;

    /**
     * 创建图谱 RAG 实例。
     *
     * @param name RAG 实例名称
     * @param kbGraphStore 图谱存储实现
     */
    public GraphRag(String name, GraphStore kbGraphStore) {
        this.name = name;
        this.graphStore = kbGraphStore;
    }

    /**
     * 获取图谱分段服务，采用懒加载。
     *
     * @return 图谱分段服务
     */
    public KnowledgeBaseGraphSegmentService getKnowledgeBaseGraphSegmentService() {
        if (null == knowledgeBaseGraphSegmentService) {
            knowledgeBaseGraphSegmentService = SpringUtil.getBean(KnowledgeBaseGraphSegmentService.class);
        }
        return knowledgeBaseGraphSegmentService;
    }

    /**
     * 将文档切分、抽取实体关系并写入图谱。
     *
     * @param graphIngestParams 抽取与入库参数
     * @return 无
     */
    public void ingest(GraphIngestParams graphIngestParams) {
        log.info("GraphRag ingest");
        User user = graphIngestParams.getUser();
        // 使用统一分块与 token 估算策略，保证后续抽取不会超限
        DocumentSplitter documentSplitter = DocumentSplitters.recursive(RAG_MAX_SEGMENT_SIZE_IN_TOKENS, graphIngestParams.getOverlap(), TokenEstimatorFactory.create(graphIngestParams.getTokenEstimator()));
        GraphStoreIngestor ingestor = GraphStoreIngestor.builder()
                .documentSplitter(documentSplitter)
                .segmentsFunction(segments -> {
                    List<Triple<TextSegment, String, String>> segmentIdToExtractContent = new ArrayList<>();
                    for (TextSegment segment : segments) {

                        String segmentId = UuidUtil.createShort();
                        log.info("Save segment to graph_segment,segmentId:{}", segmentId);
                        // 先持久化分段信息，便于抽取失败时仍可追溯
                        KnowledgeBaseGraphSegment graphSegment = new KnowledgeBaseGraphSegment();
                        graphSegment.setUuid(segmentId);
                        graphSegment.setRemark(segment.text());
                        graphSegment.setKbUuid(segment.metadata().getString(AdiConstant.MetadataKey.KB_UUID));
                        graphSegment.setKbItemUuid(segment.metadata().getString(AdiConstant.MetadataKey.KB_ITEM_UUID));
                        graphSegment.setUserId(user.getId());
                        getKnowledgeBaseGraphSegmentService().save(graphSegment);

                        String response = "";
                        if (StringUtils.isNotBlank(segment.text())) {
                            if (!graphIngestParams.isFreeToken()) {
                                // 额度不足时直接跳过抽取，避免继续扣费。
                                ErrorEnum errorMsg = SpringUtil.getBean(QuotaHelper.class).checkTextQuota(user);
                                if (null != errorMsg) {
                                    log.warn("抽取知识图谱时发现额度已超过限制,user:{},errorInfo:{}", user.getName(), errorMsg.getInfo());
                                    continue;
                                }
                            }
                            // 调用模型抽取实体关系，并记录返回内容
                            log.info("请求LLM从文本中抽取实体及关系,segmentId:{}", segmentId);
                            ChatResponse aiMessageResponse = graphIngestParams.getChatModel().chat(UserMessage.from(GraphExtractPrompt.GRAPH_EXTRACTION_PROMPT.replace("{input_text}", segment.text())));
                            response = aiMessageResponse.aiMessage().text();

                            // 记录 token 消耗，便于成本统计
                            SpringUtil.getBean(UserDayCostService.class).appendCostToUser(user, aiMessageResponse.tokenUsage().totalTokenCount(), graphIngestParams.isFreeToken());
                        }
                        // 将分段与抽取结果打包返回给入库流程
                        segmentIdToExtractContent.add(Triple.of(segment, segmentId, response));
                    }
                    return segmentIdToExtractContent;
                })
                .identifyColumns(graphIngestParams.getIdentifyColumns())
                .appendColumns(graphIngestParams.getAppendColumns())
                .graphStore(graphStore)
                .build();
        ingestor.ingest(graphIngestParams.getDocument());
    }

    /**
     * 创建图谱检索器，用于问答阶段的内容检索。
     *
     * @param param 检索器参数
     * @return 图谱内容检索器
     */
    public GraphStoreContentRetriever createRetriever(RetrieverCreateParam param) {
        return GraphStoreContentRetriever.builder()
                .graphStore(graphStore)
                .chatModel(param.getChatModel())
                .maxResults(param.getMaxResults())
                .filter(param.getFilter())
                .breakIfSearchMissed(param.isBreakIfSearchMissed())
                .build();
    }
}
