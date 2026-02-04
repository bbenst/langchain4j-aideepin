package com.moyz.adi.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.dto.mcp.McpAddOrEditReq;
import com.moyz.adi.common.dto.mcp.McpSearchReq;
import com.moyz.adi.common.entity.Mcp;
import com.moyz.adi.common.service.McpService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * MCP 管理接口控制器，仅系统管理员可维护。
 */
@RestController
@RequestMapping("/admin/mcp")
@Validated
public class AdminMcpController {

    /**
     * MCP 服务，负责 MCP 资源管理。
     */
    @Resource
    private McpService mcpService;

    /**
     * 搜索 MCP 列表并分页返回。
     *
     * @param req 搜索条件
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return MCP 分页结果
     */
    @Operation(summary = "搜索列表")
    @PostMapping(value = "/search")
    public Page<Mcp> search(@RequestBody McpSearchReq req, @NotNull @Min(1) Integer currentPage, @NotNull @Min(10) Integer pageSize) {
        return mcpService.search(req, currentPage, pageSize, true);
    }

    /**
     * 新增 MCP。
     *
     * @param commentAddReq 新增或编辑请求
     * @return 新增后的 MCP
     */
    @PostMapping("/add")
    public Mcp save(@Validated @RequestBody McpAddOrEditReq commentAddReq) {
        return mcpService.addOrUpdate(commentAddReq, ThreadContext.getCurrentUser().getIsAdmin());
    }

    /**
     * 编辑 MCP。
     *
     * @param commentAddReq 新增或编辑请求
     * @return 更新后的 MCP
     */
    @PostMapping("/edit")
    public Mcp edit(@Validated @RequestBody McpAddOrEditReq commentAddReq) {
        return mcpService.addOrUpdate(commentAddReq, ThreadContext.getCurrentUser().getIsAdmin());
    }

    /**
     * 启用或停用 MCP。
     *
     * @param uuid MCP UUID
     * @param isEnable 是否启用
     * @return 是否操作成功
     */
    @PostMapping("/enable")
    public boolean enable(@RequestParam String uuid, @RequestParam Boolean isEnable) {
        mcpService.enable(uuid, isEnable);
        return true;
    }

    /**
     * 删除 MCP（逻辑删除）。
     *
     * @param uuid MCP UUID
     * @return 是否删除成功
     */
    @PostMapping("/del/{uuid}")
    public boolean del(@PathVariable String uuid) {
        mcpService.softDelete(uuid);
        return true;
    }
}
