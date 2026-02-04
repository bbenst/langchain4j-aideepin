package com.moyz.adi.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.workflow.WfComponentReq;
import com.moyz.adi.common.dto.workflow.WfComponentSearchReq;
import com.moyz.adi.common.entity.WorkflowComponent;
import com.moyz.adi.common.service.WorkflowComponentService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 工作流组件后台管理接口控制器。
 */
@RestController
@RequestMapping("/admin/workflow/component")
@Validated
public class AdminWfComponentController {
    /**
     * 工作流组件服务，用于组件检索与管理。
     */
    @Resource
    private WorkflowComponentService workflowComponentService;

    /**
     * 搜索工作流组件并分页返回。
     *
     * @param searchReq 搜索条件
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return 组件分页结果
     */
    @PostMapping("/search")
    public Page<WorkflowComponent> search(@RequestBody WfComponentSearchReq searchReq, @NotNull @Min(1) Integer currentPage, @NotNull @Min(10) Integer pageSize) {
        return workflowComponentService.search(searchReq, currentPage, pageSize);
    }

    /**
     * 启用或停用组件。
     *
     * @param uuid 组件 UUID
     * @param isEnable 是否启用
     */
    @PostMapping("/enable")
    public void enable(@RequestParam String uuid, @RequestParam Boolean isEnable) {
        workflowComponentService.enable(uuid, isEnable);
    }

    /**
     * 删除组件。
     *
     * @param uuid 组件 UUID
     */
    @PostMapping("/del/{uuid}")
    public void del(@PathVariable String uuid) {
        workflowComponentService.deleteByUuid(uuid);
    }

    /**
     * 新增或更新组件。
     *
     * @param req 组件请求
     * @return 保存后的组件
     */
    @PostMapping("/addOrUpdate")
    public WorkflowComponent addOrUpdate(@Validated @RequestBody WfComponentReq req) {
        return workflowComponentService.addOrUpdate(req);
    }

}
