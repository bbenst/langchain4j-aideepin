package com.moyz.adi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.dto.*;
import com.moyz.adi.common.service.PromptService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
/**
 * 提示词管理接口控制器。
 */
@RestController
@RequestMapping("/prompt")
@Validated
public class PromptController {

    /**
     * 提示词服务，负责提示词的查询与维护。
     */
    @Resource
    private PromptService promptService;

    /**
     * 查询当前用户的全部提示词。
     *
     * @return 提示词列表
     */
    @Operation(summary = "查询列表")
    @GetMapping(value = "/my/all")
    public List<PromptDto> myAll() {
        return promptService.getAll(ThreadContext.getCurrentUserId());
    }

    /**
     * 按更新时间查询当前用户提示词列表。
     *
     * @param minUpdateTime 最小更新时间
     * @return 提示词列表响应
     */
    @Operation(summary = "查询列表")
    @GetMapping(value = "/my/listByUpdateTime")
    public PromptListResp list(@RequestParam(required = false) LocalDateTime minUpdateTime) {
        return promptService.listByMinUpdateTime(minUpdateTime);
    }

    /**
     * 搜索当前用户提示词并分页返回。
     *
     * @param keyword 搜索关键词
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return 提示词分页列表
     */
    @Operation(summary = "搜索列表")
    @GetMapping(value = "/my/search")
    public Page<PromptDto> search(String keyword, @NotNull @Min(1) Integer currentPage, @NotNull @Min(10) Integer pageSize) {
        return promptService.search(keyword, currentPage, pageSize);
    }

    /**
     * 提示词自动补全查询。
     *
     * @param keyword 关键词
     * @return 匹配的提示词列表
     */
    @Operation(summary = "自动填充列表")
    @GetMapping(value = "/my/autocomplete")
    public List<PromptDto> autocomplete(String keyword) {
        return promptService.autocomplete(keyword);
    }

    /**
     * 保存提示词列表。
     *
     * @param savePromptsReq 保存请求
     * @return 保存结果映射
     */
    @Operation(summary = "保存列表")
    @PostMapping(value = "/save")
    public Map<String, Long> savePrompts(@RequestBody PromptsSaveReq savePromptsReq) {
        return promptService.savePrompts(savePromptsReq);
    }

    /**
     * 删除提示词（逻辑删除）。
     *
     * @param id 提示词 ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除")
    @PostMapping(value = "/del/{id}")
    public boolean softDelete(@PathVariable Long id) {
        return promptService.softDelete(id);
    }

    /**
     * 编辑提示词标题或备注。
     *
     * @param id 提示词 ID
     * @param promptEditReq 编辑请求
     * @return 是否编辑成功
     */
    @Operation(summary = "编辑")
    @PostMapping(value = "/edit/{id}")
    public boolean edit(@PathVariable Long id, @RequestBody PromptEditReq promptEditReq) {
        return promptService.edit(id, promptEditReq.getTitle(), promptEditReq.getRemark());
    }

    /**
     * 公共搜索接口，按关键词返回提示词列表。
     *
     * @param searchReq 搜索请求
     * @return 提示词列表
     */
    @Operation(summary = "search")
    @GetMapping(value = "/search")
    public List<PromptDto> search(@Validated SearchReq searchReq) {
        return promptService.search(searchReq.getKeyword());
    }
}
