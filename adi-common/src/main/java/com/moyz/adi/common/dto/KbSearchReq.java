package com.moyz.adi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kb搜索请求
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KbSearchReq {
    /**
     * 标题
     */
    private String title;
    /**
     * 是否公开
     */
    private Boolean isPublic;
    /**
     * 最小Item数量
     */
    private Integer minItemCount;
    /**
     * 最小向量数量
     */
    private Integer minEmbeddingCount;
    /**
     * 创建时间
     */
    private Long[] createTime;
    /**
     * 更新时间
     */
    private Long[] updateTime;
    /**
     * owner名称
     */
    private String ownerName;
}
