package com.moyz.adi.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 答案Meta对象
 */
@Data
@Builder
@AllArgsConstructor
public class AnswerMeta {
    /**
     * Token
     */
    private Integer tokens;
    /**
     * UUID
     */
    private String uuid;
    /**
     * 是否Ref向量
     */
    private Boolean isRefEmbedding;
    /**
     * 是否Ref图谱
     */
    private Boolean isRefGraph;
}
