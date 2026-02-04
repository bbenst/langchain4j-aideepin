package com.moyz.adi.chat.controller;

import com.moyz.adi.common.dto.AiSearchReq;
import com.moyz.adi.common.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
/**
 * AI 搜索接口控制器。
 */
@Tag(name = "AI search controller")
@RequestMapping("/ai-search/")
@RestController
public class SearchController {

    /**
     * 搜索服务，负责 AI 搜索请求处理。
     */
    @Resource
    private SearchService searchService;

    /**
     * 以 SSE 方式返回搜索结果。
     *
     * @param req 搜索请求
     * @return SSE 事件流
     */
    @Operation(summary = "sse process")
    @PostMapping(value = "/process", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sseAsk(@RequestBody @Validated AiSearchReq req) {
        return searchService.search(req);
    }
}
