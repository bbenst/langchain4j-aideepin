package com.moyz.adi.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.cosntant.RedisKeyConstant;
import com.moyz.adi.common.dto.TmpNode;
import com.moyz.adi.common.dto.workflow.WfEdgeReq;
import com.moyz.adi.common.dto.workflow.WfNodeDto;
import com.moyz.adi.common.dto.workflow.WorkflowResp;
import com.moyz.adi.common.dto.workflow.WorkflowUpdateReq;
import com.moyz.adi.common.entity.*;
import com.moyz.adi.common.enums.ErrorEnum;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.mapper.WorkflowMapper;
import com.moyz.adi.common.util.*;
import com.moyz.adi.common.workflow.WfComponentNameEnum;
import com.moyz.adi.common.workflow.def.WfNodeParamRef;
import com.moyz.adi.common.workflow.node.classifier.ClassifierNodeConfig;
import com.moyz.adi.common.workflow.node.switcher.SwitcherCase;
import com.moyz.adi.common.workflow.node.switcher.SwitcherNodeConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.moyz.adi.common.enums.ErrorEnum.A_OPT_TOO_FREQUENTLY;

/**
 * 工作流管理服务。
 */
@Slf4j
@Service
public class WorkflowService extends ServiceImpl<WorkflowMapper, Workflow> {

    /**
     * 自身代理对象（用于事务方法）。
     */
    @Lazy
    @Resource
    private WorkflowService self;

    /**
     * 工作流节点服务。
     */
    @Resource
    private WorkflowNodeService workflowNodeService;

    /**
     * 工作流连线服务。
     */
    @Resource
    private WorkflowEdgeService workflowEdgeService;

    /**
     * 工作流组件服务。
     */
    @Resource
    private WorkflowComponentService workflowComponentService;

    /**
     * 用户服务。
     */
    @Resource
    private UserService userService;

    /**
     * Redis 分布式锁工具。
     */
    @Resource
    private RedisTemplateUtil redisTemplateUtil;

    /**
     * 新增工作流。
     *
     * @param title    标题
     * @param remark   备注
     * @param isPublic 是否公开
     * @return 工作流响应
     */
    @Transactional
    public WorkflowResp add(String title, String remark, Boolean isPublic) {
        String uuid = UuidUtil.createShort();
        Workflow one = new Workflow();
        one.setUuid(uuid);
        one.setTitle(title);
        one.setUserId(ThreadContext.getCurrentUserId());
        one.setRemark(remark);
        one.setIsEnable(true);
        one.setIsPublic(isPublic);
        baseMapper.insert(one);

        workflowNodeService.createStartNode(one);
        return changeWorkflowToDTO(one);
    }

    /**
     * 复制工作流。
     *
     * @param wfUuid 工作流 UUID
     * @return 工作流响应
     */
    @Transactional
    public WorkflowResp copy(String wfUuid) {
        String redisKey = MessageFormat.format(RedisKeyConstant.WORKFLOW_COPY_DOING, ThreadContext.getCurrentUserId());
        if (!redisTemplateUtil.lock(redisKey, UuidUtil.createShort(), 10)) {
            throw new BaseException(A_OPT_TOO_FREQUENTLY);
        }

        Workflow sourceWorkflow = getOrThrow(wfUuid);
        Workflow newWorkflow = new Workflow();
        newWorkflow.setUuid(UuidUtil.createShort());
        newWorkflow.setTitle(sourceWorkflow.getTitle() + "-copy");
        newWorkflow.setUserId(ThreadContext.getCurrentUserId());
        newWorkflow.setRemark(sourceWorkflow.getRemark());
        newWorkflow.setIsEnable(true);
        baseMapper.insert(newWorkflow);

        List<WorkflowNode> newNodes = workflowNodeService.copyByWorkflowId(sourceWorkflow.getId(), newWorkflow.getId());
        List<WorkflowEdge> newEdges = workflowEdgeService.copyByWorkflowId(sourceWorkflow.getId(), newWorkflow.getId());

        // 节点及连线的 UUID 替换为新值
        List<TmpNode> tmpNodes = newNodes.stream().map(node -> {
            TmpNode tmpNode = new TmpNode();
            tmpNode.setId(node.getId());
            tmpNode.setComponentId(node.getWorkflowComponentId());
            tmpNode.setNewUuid(UuidUtil.createShort());
            tmpNode.setOldUuid(node.getUuid());
            tmpNode.setInputConfig(node.getInputConfig());
            tmpNode.setNodeConfig(node.getNodeConfig());
            return tmpNode;
        }).toList();
        List<WorkflowEdge> updateEdges = newEdges.stream().map(edge -> {
            tmpNodes.forEach(tmp -> {
                String oldNodeUuid = tmp.getOldUuid();
                String newNodeUuid = tmp.getNewUuid();
                if (oldNodeUuid.equals(edge.getSourceNodeUuid())) {
                    edge.setSourceNodeUuid(newNodeUuid);
                } else if (oldNodeUuid.equals(edge.getSourceHandle())) {
                    edge.setSourceHandle(newNodeUuid);
                } else if (oldNodeUuid.equals(edge.getTargetNodeUuid())) {
                    edge.setTargetNodeUuid(newNodeUuid);
                }
            });

            WorkflowEdge forUpdateEdge = new WorkflowEdge();
            forUpdateEdge.setId(edge.getId());
            forUpdateEdge.setUuid(edge.getUuid());
            forUpdateEdge.setSourceNodeUuid(edge.getSourceNodeUuid());
            forUpdateEdge.setTargetNodeUuid(edge.getTargetNodeUuid());
            forUpdateEdge.setSourceHandle(edge.getSourceHandle());
            return forUpdateEdge;
        }).toList();
        List<WorkflowNode> updateNodes = tmpNodes.stream().map(item -> {
            String newNodeUuid = item.getNewUuid();
            List<WfNodeParamRef> refInputs = item.getInputConfig().getRefInputs();
            //更新引用输入的节点uuid
            refInputs.forEach(refInput -> {
                tmpNodes.stream()
                        .filter(tmpNode -> tmpNode.getOldUuid().equals(refInput.getNodeUuid()))
                        .findFirst()
                        .ifPresent(item1 -> refInput.setNodeUuid(item1.getNewUuid()));
            });
            updateNodeConfigToNewNode(item, tmpNodes);
            WorkflowNode forUpdateNode = new WorkflowNode();
            forUpdateNode.setId(item.getId());
            forUpdateNode.setUuid(newNodeUuid);
            forUpdateNode.setInputConfig(item.getInputConfig());
            forUpdateNode.setNodeConfig(item.getNodeConfig());
            return forUpdateNode;
        }).toList();
        workflowNodeService.updateBatchById(updateNodes);
        workflowEdgeService.updateBatchById(updateEdges);

        return changeWorkflowToDTO(newWorkflow);
    }

    /**
     * 设置工作流公开状态。
     *
     * @param wfUuid   工作流 UUID
     * @param isPublic 是否公开
     */
    public void setPublic(String wfUuid, Boolean isPublic) {
        Workflow workflow = PrivilegeUtil.checkAndGetByUuid(wfUuid, this.query(), ErrorEnum.A_WF_NOT_FOUND);
        ChainWrappers.lambdaUpdateChain(baseMapper)
                .eq(Workflow::getId, workflow.getId())
                .set(Workflow::getIsPublic, isPublic)
                .update();
    }

    /**
     * 更新工作流基础信息。
     *
     * @param wfUuid   工作流 UUID
     * @param title    标题
     * @param remark   备注
     * @param isPublic 是否公开
     * @return 工作流响应
     */
    public WorkflowResp updateBaseInfo(String wfUuid, String title, String remark, Boolean isPublic) {
        if (StringUtils.isAnyBlank(wfUuid, title)) {
            throw new BaseException(ErrorEnum.A_PARAMS_ERROR);
        }
        ChainWrappers.lambdaUpdateChain(baseMapper)
                .eq(Workflow::getUuid, wfUuid)
                .eq(!ThreadContext.getCurrentUser().getIsAdmin(), Workflow::getUserId, ThreadContext.getCurrentUserId())
                .set(Workflow::getTitle, title)
                .set(Workflow::getRemark, remark)
                .set(null != isPublic, Workflow::getIsPublic, isPublic)
                .update();
        Workflow workflow = getOrThrow(wfUuid);
        return changeWorkflowToDTO(workflow);
    }

    /**
     * 更新工作流结构（节点与连线）。
     *
     * @param req 更新请求
     * @return 工作流响应
     */
    @Transactional
    public WorkflowResp update(WorkflowUpdateReq req) {
        Workflow workflow = PrivilegeUtil.checkAndGetByUuid(req.getUuid(), this.query(), ErrorEnum.A_WF_NOT_FOUND);
        long workflowId = workflow.getId();
        workflowNodeService.createOrUpdateNodes(workflowId, req.getNodes());
        workflowEdgeService.createOrUpdateEdges(workflowId, req.getEdges());
        workflowNodeService.deleteNodes(workflowId, req.getDeleteNodes());
        workflowEdgeService.deleteEdges(workflowId, req.getDeleteEdges());

        Workflow workflow2 = getOrThrow(req.getUuid());
        return changeWorkflowToDTO(workflow2);
    }

    /**
     * 按 UUID 获取工作流。
     *
     * @param uuid 工作流 UUID
     * @return 工作流实体
     */
    public Workflow getByUuid(String uuid) {
        return ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(Workflow::getUuid, uuid)
                .eq(Workflow::getIsDeleted, false)
                .last("limit 1")
                .one();
    }

    /**
     * 按 UUID 获取工作流 DTO。
     *
     * @param uuid 工作流 UUID
     * @return 工作流响应
     */
    public WorkflowResp getDtoByUuid(String uuid) {
        Workflow wf = ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(Workflow::getUuid, uuid)
                .eq(Workflow::getIsDeleted, false)
                .last("limit 1")
                .one();
        return changeWorkflowToDTO(wf);
    }

    /**
     * 获取工作流，不存在则抛异常。
     *
     * @param uuid 工作流 UUID
     * @return 工作流实体
     */
    public Workflow getOrThrow(String uuid) {
        Workflow workflow = ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(Workflow::getUuid, uuid)
                .eq(Workflow::getIsDeleted, false)
                .last("limit 1")
                .one();
        if (null == workflow) {
            throw new BaseException(ErrorEnum.A_WF_NOT_FOUND);
        }
        return workflow;
    }

    /**
     * 分页搜索工作流。
     *
     * @param keyword     关键词
     * @param isPublic    是否公开
     * @param isEnable    是否启用
     * @param currentPage 当前页
     * @param pageSize    页大小
     * @return 分页结果
     */
    public Page<WorkflowResp> search(String keyword, Boolean isPublic, Boolean isEnable, Integer currentPage, Integer pageSize) {
        User user = ThreadContext.getCurrentUser();
        Page<Workflow> page = ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(Workflow::getIsDeleted, false)
                .eq(null != isPublic, Workflow::getIsPublic, isPublic)
                .eq(null != isEnable, Workflow::getIsEnable, isEnable)
                .like(StringUtils.isNotBlank(keyword), Workflow::getTitle, keyword)
                .eq(!user.getIsAdmin(), Workflow::getUserId, user.getId())
                .orderByDesc(Workflow::getUpdateTime)
                .page(new Page<>(currentPage, pageSize));
        Page<WorkflowResp> result = new Page<>();
        List<Long> userIds = new ArrayList<>();
        MPPageUtil.convertToPage(page, result, WorkflowResp.class, (source, target) -> {
            fillNodesAndEdges(target);
            userIds.add(source.getUserId());
            return target;
        });
        fillUserInfos(userIds, result.getRecords());
        return result;
    }

    /**
     * 分页搜索公开工作流。
     *
     * @param keyword     关键词
     * @param currentPage 当前页
     * @param pageSize    页大小
     * @return 分页结果
     */
    public Page<WorkflowResp> searchPublic(String keyword, Integer currentPage, Integer pageSize) {
        Page<Workflow> page = ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(Workflow::getIsDeleted, false)
                .eq(Workflow::getIsPublic, true)
                .eq(Workflow::getIsEnable, true)
                .like(StringUtils.isNotBlank(keyword), Workflow::getTitle, keyword)
                .orderByDesc(Workflow::getUpdateTime)
                .page(new Page<>(currentPage, pageSize));
        Page<WorkflowResp> result = new Page<>();
        List<Long> userIds = new ArrayList<>();
        MPPageUtil.convertToPage(page, result, WorkflowResp.class, (source, target) -> {
            fillNodesAndEdges(target);
            userIds.add(source.getUserId());
            return target;
        });
        fillUserInfos(userIds, result.getRecords());
        return result;
    }

    /**
     * 软删除工作流。
     *
     * @param uuid 工作流 UUID
     */
    public void softDelete(String uuid) {
        PrivilegeUtil.checkAndDelete(uuid, this.query(), ChainWrappers.updateChain(baseMapper), ErrorEnum.A_WF_NOT_FOUND);
    }

    /**
     * 启用或禁用工作流。
     *
     * @param uuid   工作流 UUID
     * @param enable 是否启用
     */
    public void enable(String uuid, Boolean enable) {
        if (null == enable) {
            throw new BaseException(ErrorEnum.A_PARAMS_ERROR);
        }
        Workflow workflow = PrivilegeUtil.checkAndGetByUuid(uuid, this.query(), ErrorEnum.A_WF_NOT_FOUND);
        ChainWrappers.lambdaUpdateChain(baseMapper)
                .eq(Workflow::getId, workflow.getId())
                .eq(!ThreadContext.getCurrentUser().getIsAdmin(), Workflow::getUserId, ThreadContext.getCurrentUserId())
                .set(Workflow::getIsEnable, enable)
                .update();
    }

    /**
     * 将工作流实体转换为响应 DTO。
     *
     * @param workflow 工作流实体
     * @return 工作流响应
     */
    private WorkflowResp changeWorkflowToDTO(Workflow workflow) {
        WorkflowResp workflowResp = new WorkflowResp();
        BeanUtils.copyProperties(workflow, workflowResp);

        fillNodesAndEdges(workflowResp);
        User user = userService.getById(workflow.getUserId());
        if (null != user) {
            workflowResp.setUserUuid(user.getUuid());
            workflowResp.setUserName(user.getName());
        }
        return workflowResp;
    }

    /**
     * 填充节点与连线信息。
     *
     * @param workflowResp 工作流响应
     */
    private void fillNodesAndEdges(WorkflowResp workflowResp) {
        List<WfNodeDto> nodes = workflowNodeService.listDtoByWfId(workflowResp.getId());
        workflowResp.setNodes(nodes);
        List<WfEdgeReq> edges = workflowEdgeService.listDtoByWfId(workflowResp.getId());
        workflowResp.setEdges(edges);
    }

    /**
     * 填充用户信息。
     *
     * @param userIds 用户 ID 列表
     * @param resps   工作流响应列表
     */
    private void fillUserInfos(List<Long> userIds, List<WorkflowResp> resps) {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        Map<Long, User> users = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (s, a) -> s));
        for (WorkflowResp workflowResp : resps) {
            User user = users.get(workflowResp.getUserId());
            if (null != user) {
                workflowResp.setUserUuid(user.getUuid());
                workflowResp.setUserName(user.getName());
            }
        }
    }

    /**
     * 更新节点配置中涉及到其他节点的uuid
     *
     * @param tmpNode  要更新的节点
     * @param tmpNodes 工作流复制时的临时节点
     */
    private void updateNodeConfigToNewNode(TmpNode tmpNode, List<TmpNode> tmpNodes) {
        WorkflowComponent wfComponent = workflowComponentService.getComponent(tmpNode.getComponentId());
        if (wfComponent.getName().equals(WfComponentNameEnum.CLASSIFIER.getName())) {
            ClassifierNodeConfig classifierNodeConfig = JsonUtil.fromJson(tmpNode.getNodeConfig(), ClassifierNodeConfig.class);
            if (null == classifierNodeConfig || CollectionUtils.isEmpty(classifierNodeConfig.getCategories())) {
                log.warn("找不到问题分类器的配置,new uuid:{},old uuid:{}", tmpNode.getNewUuid(), tmpNode.getOldUuid());
                return;
            }
            classifierNodeConfig.getCategories().forEach(category -> {
                tmpNodes.stream()
                        .filter(innerItem -> innerItem.getOldUuid().equals(category.getTargetNodeUuid()))
                        .findFirst()
                        .ifPresent(innerItem -> category.setTargetNodeUuid(innerItem.getNewUuid()));
            });
            tmpNode.setNodeConfig((ObjectNode) JsonUtil.classToJsonNode(classifierNodeConfig));
        } else if (wfComponent.getName().equals(WfComponentNameEnum.SWITCHER.getName())) {
            SwitcherNodeConfig nodeConfig = JsonUtil.fromJson(tmpNode.getNodeConfig(), SwitcherNodeConfig.class);
            if (null == nodeConfig || CollectionUtils.isEmpty(nodeConfig.getCases())) {
                log.warn("找不到条件分支节点的配置,new uuid:{},old uuid:{}", tmpNode.getNewUuid(), tmpNode.getOldUuid());
                return;
            }
            nodeConfig.getCases().forEach(switcherCase -> {
                tmpNodes.forEach(innerItem -> {
                    String oldNodeUuid = innerItem.getOldUuid();
                    String newNodeUuid = innerItem.getNewUuid();
                    if (switcherCase.getTargetNodeUuid().equals(oldNodeUuid)) {
                        switcherCase.setTargetNodeUuid(newNodeUuid);
                    }
                    for (SwitcherCase.Condition condition : switcherCase.getConditions()) {
                        if (condition.getNodeUuid().equals(oldNodeUuid)) {
                            condition.setNodeUuid(newNodeUuid);
                        }
                    }
                });
            });
            tmpNodes.forEach(innerItem -> {
                String oldNodeUuid = innerItem.getOldUuid();
                String newNodeUuid = innerItem.getNewUuid();
                if (nodeConfig.getDefaultTargetNodeUuid().equals(oldNodeUuid)) {
                    nodeConfig.setDefaultTargetNodeUuid(newNodeUuid);
                }
            });
            tmpNode.setNodeConfig((ObjectNode) JsonUtil.classToJsonNode(nodeConfig));
        }
    }
}
