package com.moyz.adi.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.ModelPlatformSearchReq;
import com.moyz.adi.common.entity.ModelPlatform;
import com.moyz.adi.common.service.ModelPlatformService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 模型平台后台管理接口控制器。
 */
@RestController
@RequestMapping("/admin/model-platform/")
@Validated
public class ModelPlatformController {

    /**
     * 模型平台服务，用于平台管理。
     */
    @Resource
    private ModelPlatformService modelPlatformService;

    /**
     * 搜索模型平台并分页返回。
     *
     * @param searchReq 搜索条件
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return 模型平台分页结果
     */
    @PostMapping("/search")
    public Page<ModelPlatform> page(@RequestBody ModelPlatformSearchReq searchReq, @NotNull @Min(1) Integer currentPage, @NotNull @Min(10) Integer pageSize) {
        return modelPlatformService.search(searchReq, currentPage, pageSize);
    }

    /**
     * 新增模型平台。
     *
     * @param modelPlatform 平台信息
     * @return 新增后的平台
     */
    @PostMapping("/add")
    public ModelPlatform addOne(@RequestBody ModelPlatform modelPlatform) {
        return modelPlatformService.addOne(modelPlatform);
    }

    /**
     * 编辑模型平台。
     *
     * @param modelPlatform 平台信息
     */
    @PostMapping("/edit")
    public void edit(@RequestBody ModelPlatform modelPlatform) {
        modelPlatformService.edit(modelPlatform);
    }

    /**
     * 删除模型平台（逻辑删除）。
     *
     * @param id 平台 ID
     */
    @PostMapping("/del/{id}")
    public void delete(@PathVariable Long id) {
        modelPlatformService.softDelete(id);
    }
}
