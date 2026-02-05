package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("adi_knowledge_base")
@Schema(title = "知识库实体", description = "知识库表")
public class KnowledgeBase extends BaseEntity {
    /**
     * UUID
     */
    @Schema(title = "uuid")
    @TableField("uuid")
    private String uuid;
    /**
     * 名称
     */
    @Schema(title = "名称")
    @TableField("title")
    private String title;
    /**
     * 描述
     */
    @Schema(title = "描述")
    @TableField("remark")
    private String remark;
    /**
     * 是否公开
     */
    @Schema(title = "是否公开")
    @TableField("is_public")
    private Boolean isPublic;
    /**
     * 是否严格模式
     */
    @Schema(title = "是否严格模式")
    @TableField("is_strict")
    private Boolean isStrict;
    /**
     * 点赞数
     */
    @Schema(title = "点赞数")
    @TableField("star_count")
    private Integer starCount;
    /**
     * 知识点数量
     */
    @Schema(title = "知识点数量")
    @TableField("item_count")
    private Integer itemCount;
    /**
     * 向量数
     */
    @Schema(title = "向量数")
    @TableField("embedding_count")
    private Integer embeddingCount;
    /**
     * 所属人uuid
     */
    @Schema(title = "所属人uuid")
    @TableField("owner_uuid")
    private String ownerUuid;
    /**
     * 所属人id
     */
    @Schema(title = "所属人id")
    @TableField("owner_id")
    private Long ownerId;
    /**
     * 所属人名称
     */
    @Schema(title = "所属人名称")
    @TableField("owner_name")
    private String ownerName;
    /**
     * 文档切割时重叠数量(按token来计)
     */
    @Schema(title = "文档切割时重叠数量(按token来计)")
    @TableField("ingest_max_overlap")
    private Integer ingestMaxOverlap;
    /**
     * 索引(图谱化)文档时使用的LLM,如不指定的话则使用第1个可用的LLM
     */
    @Schema(title = "索引(图谱化)文档时使用的LLM,如不指定的话则使用第1个可用的LLM")
    @TableField("ingest_model_name")
    private String ingestModelName;
    /**
     * 索引(图谱化)文档时使用的LLM,如不指定的话则使用第1个可用的LLM
     */
    @Schema(title = "索引(图谱化)文档时使用的LLM,如不指定的话则使用第1个可用的LLM")
    @TableField("ingest_model_id")
    private Long ingestModelId;
    /**
     * token数量估计器,默认使用OpenAiTokenizer
     */
    @Schema(title = "token数量估计器,默认使用OpenAiTokenizer")
    @TableField("ingest_token_estimator")
    private String ingestTokenEstimator;
    /**
     * 文档召回最大数量
     */
    @Schema(title = "文档召回最大数量")
    @TableField("retrieve_max_results")
    private Integer retrieveMaxResults;
    /**
     * 文档召回最小分数
     */
    @Schema(title = "文档召回最小分数")
    @TableField("retrieve_min_score")
    private Double retrieveMinScore;
    /**
     * 请求LLM时的temperature
     */
    @Schema(title = "请求LLM时的temperature")
    @TableField("query_llm_temperature")
    private Double queryLlmTemperature;
    /**
     * 请求LLM时的系统提示词
     */
    @Schema(title = "请求LLM时的系统提示词")
    @TableField("query_system_message")
    private String querySystemMessage;
}
