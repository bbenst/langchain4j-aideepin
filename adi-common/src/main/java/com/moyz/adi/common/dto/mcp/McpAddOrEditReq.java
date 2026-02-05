package com.moyz.adi.common.dto.mcp;

import com.moyz.adi.common.annotation.ElementInArray;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.moyz.adi.common.cosntant.AdiConstant.McpConstant.*;

/**
 * MCP新增或编辑请求
 */
@Data
@Validated
public class McpAddOrEditReq {
    /**
     * UUID
     */
    private String uuid;
    /**
     * 标题
     */
    @NotBlank
    private String title;
    /**
     * transport类型
     */
    @ElementInArray(acceptedValues = {
            TRANSPORT_TYPE_SSE,
            TRANSPORT_TYPE_STDIO
    }, required = true)
    private String transportType;
    /**
     * SSE地址
     */
    private String sseUrl;
    /**
     * SSE超时时间
     */
    private Integer sseTimeout;
    /**
     * stdio命令
     */
    private String stdioCommand;
    /**
     * stdio参数
     */
    private String stdioArg;
    /**
     * 预设参数
     */
    private List<McpCommonParam> presetParams;
    /**
     * 自定义参数Definitions
     */
    private List<McpCustomizedParamDefinition> customizedParamDefinitions;
    /**
     * install类型
     */
    @ElementInArray(acceptedValues = {
            INSTALL_TYPE_DOCKER,
            INSTALL_TYPE_LOCAL,
            INSTALL_TYPE_REMOTE,
            INSTALL_TYPE_WASM
    }, required = true)
    private String installType;
    /**
     * 仓库地址
     */
    private String repoUrl;
    /**
     * 描述
     */
    private String remark;
    /**
     * 是否启用
     */
    private Boolean isEnable;
}
