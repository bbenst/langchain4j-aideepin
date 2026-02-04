package com.moyz.adi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.dto.KbStarInfoResp;
import com.moyz.adi.common.service.KnowledgeBaseStarService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * 知识库点赞与收藏查询接口控制器。
 */
@Tag(name = "知识库点赞controller")
@Validated
@RequestMapping("/knowledge-base/star")
@RestController
public class KnowledgeBaseStarController {

    /**
     * 知识库点赞服务，用于查询点赞信息。
     */
    @Resource
    private KnowledgeBaseStarService knowledgeBaseStarService;

    /**
     * 获取当前用户点赞的知识库分页列表。
     *
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return 点赞信息分页列表
     */
    @GetMapping("/mine")
    public Page<KbStarInfoResp> stars(int currentPage, int pageSize) {
        return knowledgeBaseStarService.listStarInfo(ThreadContext.getCurrentUser(), currentPage, pageSize);
    }
}
