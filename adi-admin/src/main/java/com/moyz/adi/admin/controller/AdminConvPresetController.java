package com.moyz.adi.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.ConvPresetAddReq;
import com.moyz.adi.common.dto.ConvPresetEditReq;
import com.moyz.adi.common.dto.ConvPresetSearchReq;
import com.moyz.adi.common.entity.ConversationPreset;
import com.moyz.adi.common.service.ConversationPresetService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 会话预设管理接口控制器。
 */
@RestController
@RequestMapping("/admin/conv-preset")
@Validated
public class AdminConvPresetController {

    /**
     * 会话预设服务，用于管理预设会话。
     */
    @Resource
    private ConversationPresetService conversationPresetService;

    /**
     * 搜索预设会话并分页返回。
     *
     * @param keyword 搜索条件
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return 预设会话分页结果
     */
    @PostMapping("/search")
    public Page<ConversationPreset> page(@RequestBody ConvPresetSearchReq keyword, @NotNull @Min(1) Integer currentPage, @NotNull @Min(10) Integer pageSize) {
        return conversationPresetService.search(keyword.getTitle(), currentPage, pageSize);
    }

    /**
     * 新增预设会话。
     *
     * @param presetAddReq 新增请求
     * @return 新增后的预设会话
     */
    @PostMapping("/addOne")
    public ConversationPreset addOne(@RequestBody ConvPresetAddReq presetAddReq) {
        return conversationPresetService.addOne(presetAddReq);
    }

    /**
     * 编辑预设会话。
     *
     * @param uuid 预设会话 UUID
     * @param editReq 编辑请求
     * @return 是否编辑成功
     */
    @PostMapping("/edit/{uuid}")
    public boolean edit(@PathVariable String uuid, @RequestBody ConvPresetEditReq editReq) {
        return conversationPresetService.edit(uuid, editReq);
    }

    /**
     * 删除预设会话（逻辑删除）。
     *
     * @param uuid 预设会话 UUID
     */
    @PostMapping("/del/{uuid}")
    public void delete(@PathVariable String uuid) {
        conversationPresetService.softDel(uuid);
    }
}
