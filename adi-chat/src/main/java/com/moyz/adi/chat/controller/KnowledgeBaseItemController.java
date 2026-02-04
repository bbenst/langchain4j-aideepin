package com.moyz.adi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.KbItemDto;
import com.moyz.adi.common.dto.KbItemEditReq;
import com.moyz.adi.common.entity.KnowledgeBaseItem;
import com.moyz.adi.common.service.KnowledgeBaseItemService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
/**
 * 知识点（知识库条目）接口控制器。
 */
@RestController
@RequestMapping("/knowledge-base-item")
@Validated
public class KnowledgeBaseItemController {

    /**
     * 知识点服务，负责条目管理与查询。
     */
    @Resource
    private KnowledgeBaseItemService knowledgeBaseItemService;

    /**
     * 新增或更新知识点。
     *
     * @param itemEditReq 编辑请求
     * @return 保存后的知识点实体
     */
    @PostMapping("/saveOrUpdate")
    public KnowledgeBaseItem saveOrUpdate(@RequestBody KbItemEditReq itemEditReq) {
        return knowledgeBaseItemService.saveOrUpdate(itemEditReq);
    }

    /**
     * 搜索知识点并分页返回。
     *
     * @param kbUuid 知识库 UUID
     * @param keyword 搜索关键词
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return 知识点分页结果
     */
    @GetMapping("/search")
    public Page<KbItemDto> search(String kbUuid, String keyword, @NotNull @Min(1) Integer currentPage, @NotNull @Min(10) Integer pageSize) {
        return knowledgeBaseItemService.search(kbUuid, keyword, currentPage, pageSize);
    }

    /**
     * 获取知识点详情。
     *
     * @param uuid 知识点 UUID
     * @return 知识点详情
     */
    @GetMapping("/info/{uuid}")
    public KnowledgeBaseItem info(@PathVariable String uuid) {
        return knowledgeBaseItemService.lambdaQuery()
                .eq(KnowledgeBaseItem::getUuid, uuid)
                .eq(KnowledgeBaseItem::getIsDeleted, false)
                .one();
    }

    /**
     * 删除知识点（逻辑删除）。
     *
     * @param uuid 知识点 UUID
     * @return 是否删除成功
     */
    @PostMapping("/del/{uuid}")
    public boolean softDelete(@PathVariable String uuid) {
        return knowledgeBaseItemService.softDelete(uuid);
    }
}
