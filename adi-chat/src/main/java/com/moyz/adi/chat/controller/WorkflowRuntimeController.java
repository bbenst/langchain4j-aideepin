package com.moyz.adi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.workflow.WfRuntimeNodeDto;
import com.moyz.adi.common.dto.workflow.WfRuntimeResp;
import com.moyz.adi.common.dto.workflow.WorkflowResumeReq;
import com.moyz.adi.common.service.WorkflowRuntimeService;
import com.moyz.adi.common.workflow.WorkflowStarter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * 工作流运行记录接口控制器。
 */
@RestController
@RequestMapping("/workflow/runtime")
@Validated
public class WorkflowRuntimeController {

    /**
     * 工作流运行记录服务。
     */
    @Resource
    private WorkflowRuntimeService workflowRuntimeService;

    /**
     * 工作流执行器，用于恢复执行流程。
     */
    @Resource
    private WorkflowStarter workflowStarter;

    /**
     * 接收用户输入以继续执行剩余流程。
     *
     * @param runtimeUuid 运行实例 UUID
     * @param resumeReq 恢复请求
     */
    @Operation(summary = "接收用户输入以继续执行剩余流程")
    @PostMapping(value = "/resume/{runtimeUuid}")
    public void resume(@PathVariable String runtimeUuid, @RequestBody WorkflowResumeReq resumeReq) {
        workflowStarter.resumeFlow(runtimeUuid, resumeReq.getFeedbackContent());
    }

    /**
     * 分页查询工作流运行记录。
     *
     * @param wfUuid 工作流 UUID
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return 运行记录分页列表
     */
    @GetMapping("/page")
    public Page<WfRuntimeResp> search(@RequestParam String wfUuid,
                                      @NotNull @Min(1) Integer currentPage,
                                      @NotNull @Min(10) Integer pageSize) {
        return workflowRuntimeService.page(wfUuid, currentPage, pageSize);
    }

    /**
     * 查询运行记录的节点执行明细。
     *
     * @param runtimeUuid 运行实例 UUID
     * @return 节点执行列表
     */
    @GetMapping("/nodes/{runtimeUuid}")
    public List<WfRuntimeNodeDto> listByRuntimeId(@PathVariable String runtimeUuid) {
        return workflowRuntimeService.listByRuntimeUuid(runtimeUuid);
    }

    /**
     * 清空指定工作流的运行记录。
     *
     * @param wfUuid 工作流 UUID
     * @return 是否清理成功
     */
    @PostMapping("/clear")
    public boolean clear(@RequestParam(defaultValue = "") String wfUuid) {
        return workflowRuntimeService.deleteAll(wfUuid);
    }

    /**
     * 删除指定运行记录（逻辑删除）。
     *
     * @param wfRuntimeUuid 运行记录 UUID
     * @return 是否删除成功
     */
    @PostMapping("/del/{wfRuntimeUuid}")
    public boolean delete(@PathVariable String wfRuntimeUuid) {
        return workflowRuntimeService.softDelete(wfRuntimeUuid);
    }
}
