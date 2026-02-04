package com.moyz.adi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.dto.UserMcpDto;
import com.moyz.adi.common.dto.mcp.UserMcpUpdateReq;
import com.moyz.adi.common.service.UserMcpService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
/**
 * 用户 MCP 配置接口控制器。
 */
@RestController
@RequestMapping("/user/mcp")
public class UserMcpController {

    /**
     * 用户 MCP 服务，负责用户启用的 MCP 列表管理。
     */
    @Resource
    private UserMcpService userMcpService;

    /**
     * 获取当前用户启用的 MCP 列表。
     *
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return MCP 分页列表
     */
    @Operation(summary = "当前登录用户启用的MCP列表")
    @GetMapping(value = "/list")
    public Page<UserMcpDto> listByUserId(@NotNull @Min(1) Integer currentPage, @NotNull @Min(10) Integer pageSize) {
        return userMcpService.searchByUserId(ThreadContext.getCurrentUserId(), currentPage, pageSize);
    }

    /**
     * 新增或更新用户 MCP 配置。
     *
     * @param userMcpUpdateReq 更新请求
     * @return 更新后的 MCP 配置
     */
    @PostMapping("/saveOrUpdate")
    public UserMcpDto saveOrUpdate(@Validated @RequestBody UserMcpUpdateReq userMcpUpdateReq) {
        return userMcpService.saveOrUpdate(userMcpUpdateReq);
    }

}
