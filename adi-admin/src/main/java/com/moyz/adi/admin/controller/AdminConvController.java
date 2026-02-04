package com.moyz.adi.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.ConvDto;
import com.moyz.adi.common.dto.ConvEditReq;
import com.moyz.adi.common.dto.ConvSearchReq;
import com.moyz.adi.common.dto.UserEditReq;
import com.moyz.adi.common.service.ConversationService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 聊天会话管理接口控制器。
 */
@RestController
@RequestMapping("/admin/conv")
@Validated
public class AdminConvController {

    /**
     * 会话服务，用于后台查询与维护会话。
     */
    @Resource
    private ConversationService conversationService;

    /**
     * 分页搜索会话列表。
     *
     * @param searchReq 搜索条件
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return 会话分页列表
     */
    @PostMapping("/search")
    public Page<ConvDto> search(@RequestBody ConvSearchReq searchReq, @NotNull @Min(1) Integer currentPage, @NotNull @Min(10) Integer pageSize) {
        return conversationService.search(searchReq, currentPage, pageSize);
    }

    /**
     * 编辑会话信息。
     *
     * @param uuid 会话 UUID
     * @param editReq 编辑请求
     */
    @PostMapping("/edit/{uuid}")
    public void edit(@PathVariable String uuid, @Validated @RequestBody ConvEditReq editReq) {
        conversationService.edit(uuid, editReq);
    }

    /**
     * 删除会话（逻辑删除）。
     *
     * @param uuid 会话 UUID
     * @return 是否删除成功
     */
    @PostMapping("/del/{uuid}")
    public boolean softDel(@PathVariable String uuid) {
        return conversationService.softDel(uuid);
    }
}
