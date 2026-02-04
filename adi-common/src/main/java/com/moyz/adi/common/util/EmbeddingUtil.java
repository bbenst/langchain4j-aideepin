package com.moyz.adi.common.util;

import com.moyz.adi.common.dto.KbItemEmbeddingDto;
import com.moyz.adi.common.dto.RefEmbeddingDto;

import java.util.ArrayList;
import java.util.List;
/**
 * 向量相关转换工具类。
 */
public class EmbeddingUtil {
    /**
     * 将知识库向量 DTO 转为引用向量 DTO。
     *
     * @param embeddings 向量列表
     * @return 引用向量列表
     */
    public static List<RefEmbeddingDto> itemToRefEmbeddingDto(List<KbItemEmbeddingDto> embeddings) {
        List<RefEmbeddingDto> result = new ArrayList<>();
        for (KbItemEmbeddingDto embedding : embeddings) {
            RefEmbeddingDto newOne = RefEmbeddingDto.builder()
                    .embeddingId(embedding.getEmbeddingId())
                    .text(embedding.getText())
                    .build();
            result.add(newOne);
        }
        return result;
    }

}
