package com.moyz.adi.common.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.dto.workflow.WfRuntimeNodeDto;
import com.moyz.adi.common.entity.User;
import com.moyz.adi.common.entity.WorkflowRuntime;
import com.moyz.adi.common.entity.WorkflowRuntimeNode;
import com.moyz.adi.common.mapper.WorkflowRuntimeNodeMapper;
import com.moyz.adi.common.util.JsonUtil;
import com.moyz.adi.common.util.MPPageUtil;
import com.moyz.adi.common.util.PrivilegeUtil;
import com.moyz.adi.common.workflow.WfNodeState;
import com.moyz.adi.common.workflow.data.NodeIOData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.moyz.adi.common.enums.ErrorEnum.A_AI_IMAGE_NOT_FOUND;

/**
 * 工作流运行节点服务。
 */
@Slf4j
@Service
public class WorkflowRuntimeNodeService extends ServiceImpl<WorkflowRuntimeNodeMapper, WorkflowRuntimeNode> {


    /**
     * 查询运行实例下的节点列表。
     *
     * @param runtimeId 运行实例 ID
     * @return 节点 DTO 列表
     */
    public List<WfRuntimeNodeDto> listByWfRuntimeId(long runtimeId) {
        List<WorkflowRuntimeNode> workflowNodeList = ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(!ThreadContext.getCurrentUser().getIsAdmin(), WorkflowRuntimeNode::getUserId, ThreadContext.getCurrentUser().getId())
                .eq(WorkflowRuntimeNode::getWorkflowRuntimeId, runtimeId)
                .eq(WorkflowRuntimeNode::getIsDeleted, false)
                .orderByAsc(WorkflowRuntimeNode::getId)
                .list();
        List<WfRuntimeNodeDto> result = MPPageUtil.convertToList(workflowNodeList, WfRuntimeNodeDto.class);
        for (WfRuntimeNodeDto dto : result) {
            fillInputOutput(dto);
        }
        return result;
    }

    /**
     * 根据节点状态创建运行节点记录。
     *
     * @param user        用户
     * @param wfNodeId    节点 ID
     * @param wfRuntimeId 运行实例 ID
     * @param state       节点状态
     * @return 运行节点 DTO
     */
    public WfRuntimeNodeDto createByState(User user, long wfNodeId, long wfRuntimeId, WfNodeState state) {
        WorkflowRuntimeNode runtimeNode = new WorkflowRuntimeNode();
        runtimeNode.setUuid(state.getUuid());
        runtimeNode.setWorkflowRuntimeId(wfRuntimeId);
        runtimeNode.setStatus(state.getProcessStatus());
        runtimeNode.setUserId(user.getId());
        runtimeNode.setNodeId(wfNodeId);
        baseMapper.insert(runtimeNode);
        runtimeNode = baseMapper.selectById(runtimeNode.getId());

        WfRuntimeNodeDto result = new WfRuntimeNodeDto();
        BeanUtils.copyProperties(runtimeNode, result);
        fillInputOutput(result);
        return result;
    }

    /**
     * 更新节点输入数据。
     *
     * @param id    运行节点 ID
     * @param state 节点状态
     */
    public void updateInput(Long id, WfNodeState state) {
        if (CollectionUtils.isEmpty(state.getInputs())) {
            log.warn("没有输入数据,id:{}", id);
            return;
        }
        WorkflowRuntimeNode node = baseMapper.selectById(id);
        if (null == node) {
            log.error("节点实例不存在,id:{}", id);
            return;
        }
        WorkflowRuntimeNode updateOne = new WorkflowRuntimeNode();
        updateOne.setId(id);
        ObjectNode ob = JsonUtil.createObjectNode();
        for (NodeIOData data : state.getInputs()) {
            ob.set(data.getName(), JsonUtil.classToJsonNode(data.getContent()));
        }
        updateOne.setInput(ob);
        updateOne.setStatus(state.getProcessStatus());
        updateOne.setStatusRemark(state.getProcessStatusRemark());
        baseMapper.updateById(updateOne);
    }

    /**
     * 更新节点输出数据。
     *
     * @param id    运行节点 ID
     * @param state 节点状态
     */
    public void updateOutput(Long id, WfNodeState state) {
        WorkflowRuntimeNode node = baseMapper.selectById(id);
        if (null == node) {
            log.error("节点实例不存在,id:{}", id);
            return;
        }
        WorkflowRuntimeNode updateOne = new WorkflowRuntimeNode();
        updateOne.setId(id);
        if (!CollectionUtils.isEmpty(state.getOutputs())) {
            ObjectNode ob = JsonUtil.createObjectNode();
            for (NodeIOData data : state.getOutputs()) {
                ob.set(data.getName(), JsonUtil.classToJsonNode(data.getContent()));
            }
            updateOne.setOutput(ob);
        }
        updateOne.setStatus(state.getProcessStatus());
        updateOne.setStatusRemark(state.getProcessStatusRemark());
        baseMapper.updateById(updateOne);
    }

    /**
     * 填充输入输出默认值。
     *
     * @param dto 运行节点 DTO
     */
    private void fillInputOutput(WfRuntimeNodeDto dto) {
        if (null == dto.getInput()) {
            dto.setInput(JsonUtil.createObjectNode());
        }
        if (null == dto.getOutput()) {
            dto.setOutput(JsonUtil.createObjectNode());
        }
    }

}
