package com.moyz.adi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.KbItemEmbeddingDto;
import com.moyz.adi.common.service.embedding.IEmbeddingService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * 知识点向量信息查询接口控制器。
 */
@RestController
@RequestMapping("/knowledge-base-embedding")
@Validated
public class KnowledgeBaseEmbeddingController {

    /**
     * 向量服务，提供知识点向量分页查询。
     */
    @Resource
    private IEmbeddingService iEmbeddingService;

    /**
     * 查询指定知识点的向量列表。
     *
     * @param kbItemUuid 知识点 UUID
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return 向量分页列表
     */
    @GetMapping("/list/{kbItemUuid}")
    public Page<KbItemEmbeddingDto> list(@PathVariable String kbItemUuid, int currentPage, int pageSize) {
        return iEmbeddingService.listByItemUuid(kbItemUuid, currentPage, pageSize);
    }
}
