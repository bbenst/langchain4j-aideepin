package com.moyz.adi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.mcp.McpListReq;
import com.moyz.adi.common.dto.mcp.McpSearchReq;
import com.moyz.adi.common.entity.Mcp;
import com.moyz.adi.common.enums.ErrorEnum;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.service.McpService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * MCP 资源查询接口控制器。
 */
@RestController
@RequestMapping("/mcp")
@Validated
public class McpController {

    /**
     * MCP 服务，负责资源检索与列表查询。
     */
    @Resource
    private McpService mcpService;

    /**
     * 搜索公开 MCP 列表。
     *
     * @param keyword 关键词
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return MCP 分页列表
     */
    @Operation(summary = "搜索列表")
    @GetMapping(value = "/public/search")
    public Page<Mcp> search(@RequestParam String keyword, @NotNull @Min(1) Integer currentPage, @NotNull @Min(10) Integer pageSize) {
        return mcpService.search(McpSearchReq.builder().title(keyword).build(), currentPage, pageSize, false);
    }

    /**
     * 按 ID 列表获取公开 MCP 记录。
     *
     * @param mcpListReq 查询请求
     * @return MCP 列表
     */
    @Operation(summary = "MCP列表")
    @GetMapping(value = "/public/list")
    public List<Mcp> list(@RequestBody McpListReq mcpListReq) {
        if (CollectionUtils.isEmpty(mcpListReq.getIds())) {
            return List.of();
        }
        if (mcpListReq.getIds().size() > 1000) {
            // 限制单次查询量，避免过大列表导致数据库与内存压力
            throw new BaseException(ErrorEnum.A_PARAMS_INVALID_BY_, "最多只能查询1000条数据");
        }
        return mcpService.listByIds(mcpListReq.getIds(), false);
    }

}
