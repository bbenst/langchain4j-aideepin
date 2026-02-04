package com.moyz.adi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.dto.workflow.*;
import com.moyz.adi.common.entity.WorkflowComponent;
import com.moyz.adi.common.service.WorkflowComponentService;
import com.moyz.adi.common.service.WorkflowService;
import com.moyz.adi.common.workflow.WorkflowStarter;
import com.moyz.adi.common.workflow.node.switcher.OperatorEnum;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * 工作流相关接口控制器。
 */
@RestController
@RequestMapping("/workflow")
@Validated
public class WorkflowController {

    /**
     * 工作流执行器，用于流式运行工作流。
     */
    @Resource
    private WorkflowStarter workflowStarter;

    /**
     * 工作流服务，负责工作流的增删改查。
     */
    @Resource
    private WorkflowService workflowService;

    /**
     * 工作流组件服务，提供组件查询能力。
     */
    @Resource
    private WorkflowComponentService workflowComponentService;

    /**
     * 新建工作流。
     *
     * @param addReq 新建请求
     * @return 新建后的工作流信息
     */
    @PostMapping("/add")
    public WorkflowResp add(@RequestBody @Validated WfAddReq addReq) {
        return workflowService.add(addReq.getTitle(), addReq.getRemark(), addReq.getIsPublic());
    }

    /**
     * 复制工作流。
     *
     * @param wfUuid 工作流 UUID
     * @return 复制后的工作流信息
     */
    @PostMapping("/copy/{wfUuid}")
    public WorkflowResp copy(@PathVariable String wfUuid) {
        return workflowService.copy(wfUuid);
    }

    /**
     * 设置工作流公开状态。
     *
     * @param wfUuid 工作流 UUID
     * @param isPublic 是否公开
     */
    @PostMapping("/set-public/{wfUuid}")
    public void setPublic(@PathVariable String wfUuid, @RequestParam(defaultValue = "true") Boolean isPublic) {
        workflowService.setPublic(wfUuid, isPublic);
    }

    /**
     * 更新工作流。
     *
     * @param req 更新请求
     * @return 更新后的工作流信息
     */
    @PostMapping("/update")
    public WorkflowResp update(@RequestBody @Validated WorkflowUpdateReq req) {
        return workflowService.update(req);
    }

    /**
     * 删除工作流（逻辑删除）。
     *
     * @param uuid 工作流 UUID
     */
    @PostMapping("/del/{uuid}")
    public void delete(@PathVariable String uuid) {
        workflowService.softDelete(uuid);
    }

    /**
     * 启用或停用工作流。
     *
     * @param uuid 工作流 UUID
     * @param enable 是否启用
     */
    @PostMapping("/enable/{uuid}")
    public void enable(@PathVariable String uuid, @RequestParam Boolean enable) {
        workflowService.enable(uuid, enable);
    }

    /**
     * 更新工作流基础信息。
     *
     * @param req 基础信息更新请求
     * @return 更新后的工作流信息
     */
    @PostMapping("/base-info/update")
    public WorkflowResp updateBaseInfo(@RequestBody @Validated WfBaseInfoUpdateReq req) {
        return workflowService.updateBaseInfo(req.getUuid(), req.getTitle(), req.getRemark(), req.getIsPublic());
    }

    /**
     * 以 SSE 方式运行工作流。
     *
     * @param wfUuid 工作流 UUID
     * @param runReq 运行请求
     * @return SSE 事件流
     */
    @Operation(summary = "流式响应")
    @PostMapping(value = "/run/{wfUuid}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sseAsk(@PathVariable String wfUuid, @RequestBody WorkflowRunReq runReq) {
        return workflowStarter.streaming(ThreadContext.getCurrentUser(), wfUuid, runReq.getInputs());
    }

    /**
     * 搜索当前用户工作流并分页返回。
     *
     * @param keyword 搜索关键词
     * @param isPublic 是否公开
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return 工作流分页列表
     */
    @GetMapping("/mine/search")
    public Page<WorkflowResp> searchMine(@RequestParam(defaultValue = "") String keyword,
                                         @RequestParam(required = false) Boolean isPublic,
                                         @NotNull @Min(1) Integer currentPage,
                                         @NotNull @Min(10) Integer pageSize) {
        return workflowService.search(keyword, isPublic, null, currentPage, pageSize);
    }

    /**
     * 搜索公开工作流
     *
     * @param keyword     搜索关键词
     * @param currentPage 当前页数
     * @param pageSize    每页数量
     * @return 工作流列表
     */
    @GetMapping("/public/search")
    public Page<WorkflowResp> searchPublic(@RequestParam(defaultValue = "") String keyword,
                                           @NotNull @Min(1) Integer currentPage,
                                           @NotNull @Min(10) Integer pageSize) {
        return workflowService.searchPublic(keyword, currentPage, pageSize);
    }

    /**
     * 查询公开工作流条件算子列表。
     *
     * @return 算子名称与描述列表
     */
    @GetMapping("/public/operators")
    public List<Map<String, String>> searchPublic() {
        List<Map<String, String>> result = new ArrayList<>();
        for (OperatorEnum operator : OperatorEnum.values()) {
            result.add(Map.of("name", operator.getName(), "desc", operator.getDesc()));
        }
        return result;
    }

    /**
     * 查询可用的工作流组件列表。
     *
     * @return 组件列表
     */
    @GetMapping("/public/component/list")
    public List<WorkflowComponent> component() {
        return workflowComponentService.getAllEnable();
    }
}
