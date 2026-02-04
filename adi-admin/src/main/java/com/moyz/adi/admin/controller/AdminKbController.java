package com.moyz.adi.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.KbEditReq;
import com.moyz.adi.common.dto.KbInfoResp;
import com.moyz.adi.common.dto.KbSearchReq;
import com.moyz.adi.common.service.KnowledgeBaseService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 知识库管理接口控制器。
 */
@RestController
@RequestMapping("/admin/kb")
@Validated
public class AdminKbController {

    /**
     * 知识库服务，负责后台检索与维护。
     */
    @Resource
    private KnowledgeBaseService knowledgeBaseService;

    /**
     * 搜索知识库并分页返回。
     *
     * @param kbSearchReq 搜索条件
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return 知识库分页结果
     */
    @PostMapping("/search")
    public Page<KbInfoResp> search(@RequestBody KbSearchReq kbSearchReq, @NotNull @Min(1) Integer currentPage, @NotNull @Min(10) Integer pageSize) {
        return knowledgeBaseService.search(kbSearchReq, currentPage, pageSize);
    }

    /**
     * 删除知识库（逻辑删除）。
     *
     * @param uuid 知识库 UUID
     * @return 是否删除成功
     */
    @PostMapping("/del/{uuid}")
    public boolean delete(@PathVariable String uuid) {
        return knowledgeBaseService.softDelete(uuid);
    }

    /**
     * 编辑知识库信息。
     *
     * @param kbEditReq 编辑请求
     * @return 是否编辑成功
     */
    @PostMapping("/edit")
    public boolean edit(@RequestBody KbEditReq kbEditReq) {
        knowledgeBaseService.saveOrUpdate(kbEditReq);
        return true;
    }
}
