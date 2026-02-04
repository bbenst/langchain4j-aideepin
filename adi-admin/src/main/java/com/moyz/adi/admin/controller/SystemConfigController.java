package com.moyz.adi.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.SysConfigDto;
import com.moyz.adi.common.dto.SysConfigEditDto;
import com.moyz.adi.common.dto.SysConfigSearchReq;
import com.moyz.adi.common.entity.SysConfig;
import com.moyz.adi.common.service.SysConfigService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置后台管理接口控制器。
 */
@RestController
@RequestMapping("/admin/sys-config")
@Validated
public class SystemConfigController {

    /**
     * 系统配置服务，用于配置检索与维护。
     */
    @Resource
    private SysConfigService sysConfigService;

    /**
     * 搜索系统配置并分页返回。
     *
     * @param searchReq 搜索条件
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return 配置分页结果
     */
    @PostMapping("/search")
    public Page<SysConfigDto> search(@RequestBody SysConfigSearchReq searchReq, @NotNull @Min(1) Integer currentPage, @NotNull @Min(10) Integer pageSize) {
        return sysConfigService.search(searchReq, currentPage, pageSize);
    }

    /**
     * 编辑系统配置。
     *
     * @param sysConfigDto 编辑请求
     * @return 是否编辑成功
     */
    @PostMapping("/edit")
    public boolean edit(@Validated @RequestBody SysConfigEditDto sysConfigDto) {
        return sysConfigService.edit(sysConfigDto) > 0;
    }

    /**
     * 删除系统配置（逻辑删除）。
     *
     * @param id 配置 ID
     * @return 是否删除成功
     */
    @PostMapping("/del/{id}")
    public boolean delete(@PathVariable Long id) {
        return sysConfigService.softDelete(id);
    }
}
