package com.moyz.adi.common.dto;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moyz.adi.common.workflow.WfNodeInputConfig;
import lombok.Data;

/**
 * Tmp节点对象
 */
@Data
public class TmpNode {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * oldUUID
     */
    private String oldUuid;
    /**
     * newUUID
     */
    private String newUuid;
    /**
     * 组件ID
     */
    private Long componentId;
    /**
     * 输入配置
     */
    private WfNodeInputConfig inputConfig;
    /**
     * 节点配置
     */
    private ObjectNode nodeConfig;
}
