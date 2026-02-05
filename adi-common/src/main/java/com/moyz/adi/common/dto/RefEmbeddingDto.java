package com.moyz.adi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ref向量数据传输对象
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefEmbeddingDto {
    /**
     * 向量ID
     */
    private String embeddingId;
    /**
     * text
     */
    private String text;
}
