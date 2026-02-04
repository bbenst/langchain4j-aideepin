package com.moyz.adi.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.AiModelDto;
import com.moyz.adi.common.dto.AiModelSearchReq;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.interfaces.AiModelAddGroup;
import com.moyz.adi.common.interfaces.AiModelEditGroup;
import com.moyz.adi.common.service.AiModelService;
import com.moyz.adi.common.util.AiModelUtil;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.moyz.adi.common.enums.ErrorEnum.A_PARAMS_ERROR;

/**
 * AI 模型管理接口控制器。
 */
@RestController
@RequestMapping("/admin/model")
@Validated
public class AdminModelController {

    /**
     * 模型服务，用于后台管理模型信息。
     */
    @Resource
    private AiModelService aiModelService;

    /**
     * 搜索模型并分页返回。
     *
     * @param aiModelSearchReq 搜索条件
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return 模型分页结果
     */
    @PostMapping("/search")
    public Page<AiModelDto> page(@RequestBody AiModelSearchReq aiModelSearchReq, @NotNull @Min(1) Integer currentPage, @NotNull @Min(10) Integer pageSize) {
        return aiModelService.search(aiModelSearchReq, currentPage, pageSize);
    }

    /**
     * 新增模型。
     *
     * @param aiModelDto 模型信息
     * @return 新增后的模型
     */
    @PostMapping("/addOne")
    public AiModelDto addOne(@Validated(AiModelAddGroup.class) @RequestBody AiModelDto aiModelDto) {
        check(aiModelDto.getType());
        return aiModelService.addOne(aiModelDto);
    }

    /**
     * 编辑模型。
     *
     * @param aiModelDto 模型信息
     */
    @PostMapping("/edit")
    public void edit(@Validated(AiModelEditGroup.class) @RequestBody AiModelDto aiModelDto) {
        check(aiModelDto.getType());
        aiModelService.edit(aiModelDto);
    }

    /**
     * 删除模型（逻辑删除）。
     *
     * @param id 模型 ID
     */
    @PostMapping("/del/{id}")
    public void delete(@PathVariable Long id) {
        aiModelService.softDelete(id);
    }

    /**
     * 校验模型类型是否合法。
     *
     * @param type 模型类型
     */
    private void check(String type) {
        if (StringUtils.isNotBlank(type) && !AiModelUtil.checkModelType(type)) {
            throw new BaseException(A_PARAMS_ERROR);
        }
    }
}
