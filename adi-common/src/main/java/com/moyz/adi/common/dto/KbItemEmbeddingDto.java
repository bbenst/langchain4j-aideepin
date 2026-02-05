package com.moyz.adi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * KbItem向量数据传输对象
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class KbItemEmbeddingDto {
    /**
     * 向量ID
     */
    private String embeddingId;
    /**
     * 向量
     */
    private float[] embedding;
    /**
     * text
     */
    private String text;
}
