package com.moyz.adi.chat.controller;

import com.moyz.adi.common.dto.AiSearchResp;
import com.moyz.adi.common.service.AiSearchRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
/**
 * AI 搜索记录接口控制器。
 */
@Tag(name = "Ai search record controller")
@RequestMapping("/ai-search-record/")
@RestController
public class SearchRecordController {

    /**
     * 搜索记录服务，负责搜索历史查询与删除。
     */
    @Resource
    private AiSearchRecordService aiSearchRecordService;

    /**
     * 按最大 ID 与关键词查询搜索记录。
     *
     * @param maxId 最大 ID 游标
     * @param keyword 搜索关键词
     * @return 搜索记录响应
     */
    @Operation(summary = "List by max id")
    @GetMapping(value = "/list")
    public AiSearchResp list(@RequestParam(defaultValue = "0") Long maxId, String keyword) {
        return aiSearchRecordService.listByMaxId(maxId, keyword);
    }

    /**
     * 删除指定搜索记录（逻辑删除）。
     *
     * @param uuid 记录 UUID
     * @return 是否删除成功
     */
    @PostMapping("/del/{uuid}")
    public boolean recordDel(@PathVariable String uuid) {
        return aiSearchRecordService.softDelete(uuid);
    }
}
