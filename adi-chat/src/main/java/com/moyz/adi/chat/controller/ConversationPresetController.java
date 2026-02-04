package com.moyz.adi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.ConvPresetSearchReq;
import com.moyz.adi.common.entity.ConversationPreset;
import com.moyz.adi.common.service.ConversationPresetService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * 会话预设（角色）相关接口控制器。
 */
@RequestMapping("/conversation-preset")
@RestController
public class ConversationPresetController {

    /**
     * 会话预设服务，用于检索与管理预设会话。
     */
    @Resource
    private ConversationPresetService conversationPresetService;

    /**
     * 按标题关键字搜索预设会话并分页返回。
     *
     * @param searchReq 搜索条件
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return 分页后的预设会话列表
     */
    @Operation(summary = "搜索预设会话(角色)")
    @PostMapping("/search")
    public Page<ConversationPreset> page(@RequestBody ConvPresetSearchReq searchReq, @NotNull @Min(1) Integer currentPage, @NotNull @Min(10) Integer pageSize) {
        return conversationPresetService.search(searchReq.getTitle(), currentPage, pageSize);
    }
}
