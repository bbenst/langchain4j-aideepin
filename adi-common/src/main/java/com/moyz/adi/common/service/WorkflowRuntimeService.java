package com.moyz.adi.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.dto.workflow.WfRuntimeNodeDto;
import com.moyz.adi.common.dto.workflow.WfRuntimeResp;
import com.moyz.adi.common.entity.User;
import com.moyz.adi.common.entity.Workflow;
import com.moyz.adi.common.entity.WorkflowRuntime;
import com.moyz.adi.common.enums.ErrorEnum;
import com.moyz.adi.common.mapper.WorkflowRunMapper;
import com.moyz.adi.common.util.JsonUtil;
import com.moyz.adi.common.util.MPPageUtil;
import com.moyz.adi.common.util.PrivilegeUtil;
import com.moyz.adi.common.util.UuidUtil;
import com.moyz.adi.common.workflow.WfState;
import com.moyz.adi.common.workflow.data.NodeIOData;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.moyz.adi.common.cosntant.AdiConstant.WorkflowConstant.WORKFLOW_PROCESS_STATUS_DOING;

/**
 * 工作流运行实例服务。
 */
@Slf4j
@Service
public class WorkflowRuntimeService extends ServiceImpl<WorkflowRunMapper, WorkflowRuntime> {

    /**
     * 工作流服务。
     */
    @Resource
    private WorkflowService workflowService;

    /**
     * 工作流运行节点服务。
     */
    @Resource
    private WorkflowRuntimeNodeService workflowRuntimeNodeService;

    /**
     * 创建工作流运行实例。
     *
     * @param user       用户
     * @param workflowId 工作流 ID
     * @return 运行实例响应
     */
    public WfRuntimeResp create(User user, Long workflowId) {
        WorkflowRuntime one = new WorkflowRuntime();
        one.setUuid(UuidUtil.createShort());
        one.setUserId(user.getId());
        one.setWorkflowId(workflowId);
        baseMapper.insert(one);

        one = baseMapper.selectById(one.getId());
        return changeToDTO(one);
    }

    /**
     * 更新输入数据。
     *
     * @param id      运行实例 ID
     * @param wfState 工作流状态
     */
    public void updateInput(long id, WfState wfState) {
        if (CollectionUtils.isEmpty(wfState.getInput())) {
            log.warn("没有输入数据,id:{}", id);
            return;
        }
        WorkflowRuntime node = baseMapper.selectById(id);
        if (null == node) {
            log.error("工作流实例不存在,id:{}", id);
            return;
        }
        WorkflowRuntime updateOne = new WorkflowRuntime();
        updateOne.setId(id);
        ObjectNode ob = JsonUtil.createObjectNode();
        for (NodeIOData data : wfState.getInput()) {
            ob.set(data.getName(), JsonUtil.classToJsonNode(data.getContent()));
        }
        updateOne.setInput(ob);
        updateOne.setStatus(WORKFLOW_PROCESS_STATUS_DOING);
        baseMapper.updateById(updateOne);
    }

    /**
     * 更新输出数据。
     *
     * @param id      运行实例 ID
     * @param wfState 工作流状态
     * @return 更新后的实例
     */
    public WorkflowRuntime updateOutput(long id, WfState wfState) {
        WorkflowRuntime node = baseMapper.selectById(id);
        if (null == node) {
            log.error("工作流实例不存在,id:{}", id);
            return null;
        }
        WorkflowRuntime updateOne = new WorkflowRuntime();
        updateOne.setId(id);
        ObjectNode ob = JsonUtil.createObjectNode();
        for (NodeIOData data : wfState.getOutput()) {
            ob.set(data.getName(), JsonUtil.classToJsonNode(data.getContent()));
        }
        updateOne.setOutput(ob);
        updateOne.setStatus(wfState.getProcessStatus());
        baseMapper.updateById(updateOne);
        return updateOne;
    }

    /**
     * 更新运行状态。
     *
     * @param id           运行实例 ID
     * @param processStatus 状态值
     * @param statusRemark  状态说明
     */
    public void updateStatus(long id, int processStatus, String statusRemark) {
        WorkflowRuntime node = baseMapper.selectById(id);
        if (null == node) {
            log.error("工作流实例不存在,id:{}", id);
            return;
        }
        WorkflowRuntime updateOne = new WorkflowRuntime();
        updateOne.setId(id);
        updateOne.setStatus(processStatus);
        updateOne.setStatusRemark(StringUtils.substring(statusRemark, 0, 250));
        baseMapper.updateById(updateOne);
    }

    /**
     * 按 UUID 获取运行实例。
     *
     * @param uuid 运行实例 UUID
     * @return 运行实例
     */
    public WorkflowRuntime getByUuid(String uuid) {
        return ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(!ThreadContext.getCurrentUser().getIsAdmin(), WorkflowRuntime::getUserId, ThreadContext.getCurrentUserId())
                .eq(WorkflowRuntime::getUuid, uuid)
                .eq(WorkflowRuntime::getIsDeleted, false)
                .last("limit 1")
                .one();
    }

    /**
     * 分页查询运行实例。
     *
     * @param wfUuid      工作流 UUID
     * @param currentPage 当前页
     * @param pageSize    页大小
     * @return 分页结果
     */
    public Page<WfRuntimeResp> page(String wfUuid, Integer currentPage, Integer pageSize) {
        Workflow workflow = workflowService.getOrThrow(wfUuid);
        User user = ThreadContext.getCurrentUser();
        Page<WorkflowRuntime> page = ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(WorkflowRuntime::getWorkflowId, workflow.getId())
                .eq(WorkflowRuntime::getIsDeleted, false)
                .eq(!user.getIsAdmin(), WorkflowRuntime::getUserId, user.getId())
                .orderByDesc(WorkflowRuntime::getUpdateTime)
                .page(new Page<>(currentPage, pageSize));
        Page<WfRuntimeResp> result = new Page<>();
        MPPageUtil.convertToPage(page, result, WfRuntimeResp.class, (source, target) -> {
            fillInputOutput(target);
            return target;
        });
        return result;
    }

    /**
     * 查询运行实例的节点列表。
     *
     * @param runtimeUuid 运行实例 UUID
     * @return 节点列表
     */
    public List<WfRuntimeNodeDto> listByRuntimeUuid(String runtimeUuid) {
        WorkflowRuntime runtime = PrivilegeUtil.checkAndGetByUuid(runtimeUuid, this.query(), ErrorEnum.A_WF_RUNTIME_NOT_FOUND);
        return workflowRuntimeNodeService.listByWfRuntimeId(runtime.getId());
    }

    /**
     * 删除工作流的全部运行实例。
     *
     * @param wfUuid 工作流 UUID
     * @return 是否删除成功
     */
    public boolean deleteAll(String wfUuid) {
        Workflow workflow = workflowService.getOrThrow(wfUuid);
        User user = ThreadContext.getCurrentUser();
        return ChainWrappers.lambdaUpdateChain(baseMapper)
                .eq(WorkflowRuntime::getWorkflowId, workflow.getId())
                .eq(!user.getIsAdmin(), WorkflowRuntime::getUserId, user.getId())
                .set(WorkflowRuntime::getIsDeleted, true)
                .update();
    }

    /**
     * 将运行实例转换为响应 DTO。
     *
     * @param runtime 运行实例
     * @return 响应 DTO
     */
    private WfRuntimeResp changeToDTO(WorkflowRuntime runtime) {
        WfRuntimeResp result = new WfRuntimeResp();
        BeanUtils.copyProperties(runtime, result);
        fillInputOutput(result);
        return result;
    }

    /**
     * 填充输入输出默认值。
     *
     * @param target 运行实例响应
     */
    private void fillInputOutput(WfRuntimeResp target) {
        if (null == target.getInput()) {
            target.setInput(JsonUtil.createObjectNode());
        }
        if (null == target.getOutput()) {
            target.setOutput(JsonUtil.createObjectNode());
        }
    }

    /**
     * 软删除运行实例。
     *
     * @param uuid 运行实例 UUID
     * @return 是否删除成功
     */
    public boolean softDelete(String uuid) {
        WorkflowRuntime workflowRuntime = PrivilegeUtil.checkAndGetByUuid(uuid, this.query(), ErrorEnum.A_WF_NOT_FOUND);
        return ChainWrappers.lambdaUpdateChain(baseMapper)
                .eq(WorkflowRuntime::getId, workflowRuntime.getId())
                .set(WorkflowRuntime::getIsDeleted, true)
                .update();
    }
}
