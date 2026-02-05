package com.moyz.adi.common.dto.workflow;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * Wf节点数据传输对象
 */
@Validated
@Data
public class WfNodeDto {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * UUID
     */
    @NotBlank
    @Size(min = 32, max = 32)
    private String uuid;
    /**
     * workflowID
     */
    private Long workflowId;
    /**
     * workflow组件ID
     */
    @Min(1)
    private Long workflowComponentId;
    /**
     * 标题
     */
    @NotBlank
    private String title;
    /**
     * 描述
     */
    private String remark;
    /**
     * 输入配置
     */
    @NotNull
    private ObjectNode inputConfig;
    /**
     * 节点配置
     */
    @NotNull
    private ObjectNode nodeConfig;
    /**
     * positionX
     */
    @NotNull
    private Double positionX;
    /**
     * positionY
     */
    @NotNull
    private Double positionY;
}
