package com.moyz.adi.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.workflow.WfSearchReq;
import com.moyz.adi.common.dto.workflow.WorkflowResp;
import com.moyz.adi.common.service.WorkflowService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 工作流后台管理接口控制器。
 */
@RestController
@RequestMapping("/admin/workflow")
@Validated
public class AdminWorkflowController {

    /**
     * 工作流服务，用于后台检索与启停管理。
     */
    @Resource
    private WorkflowService workflowService;

    /**
     * 搜索工作流并分页返回。
     *
     * @param req 搜索条件
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return 工作流分页结果
     */
    @PostMapping("/search")
    public Page<WorkflowResp> search(@RequestBody WfSearchReq req,
                                     @RequestParam @NotNull @Min(1) Integer currentPage,
                                     @RequestParam @NotNull @Min(10) Integer pageSize) {
        return workflowService.search(req.getTitle(), req.getIsPublic(), req.getIsEnable(), currentPage, pageSize);
    }

    /**
     * 启用或停用工作流。
     *
     * @param uuid 工作流 UUID
     * @param isEnable 是否启用
     */
    @PostMapping("/enable")
    public void enable(@RequestParam String uuid, @RequestParam Boolean isEnable) {
        workflowService.enable(uuid, isEnable);
    }
}
