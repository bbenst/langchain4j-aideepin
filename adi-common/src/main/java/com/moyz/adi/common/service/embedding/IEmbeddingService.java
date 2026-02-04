package com.moyz.adi.common.service.embedding;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.KbItemEmbeddingDto;
import com.moyz.adi.common.dto.RefEmbeddingDto;

import java.util.List;

/**
 * 向量数据服务接口。
 */
public interface IEmbeddingService {
    /**
     * 根据向量 ID 列表查询向量信息。
     *
     * @param embeddingIds 向量 ID 列表
     * @return 向量 DTO 列表
     */
    List<KbItemEmbeddingDto> listByEmbeddingIds(List<String> embeddingIds);

    /**
     * 根据知识点 UUID 分页查询向量。
     *
     * @param kbItemUuid  知识点 UUID
     * @param currentPage 当前页
     * @param pageSize    页大小
     * @return 分页结果
     */
    Page<KbItemEmbeddingDto> listByItemUuid(String kbItemUuid, int currentPage, int pageSize);

    /**
     * 删除指定知识点的向量。
     *
     * @param kbItemUuid 知识点 UUID
     * @return 是否删除成功
     */
    boolean deleteByItemUuid(String kbItemUuid);

    /**
     * 统计知识库下的向量数量。
     *
     * @param kbUuid 知识库 UUID
     * @return 数量
     */
    Integer countByKbUuid(String kbUuid);
}
