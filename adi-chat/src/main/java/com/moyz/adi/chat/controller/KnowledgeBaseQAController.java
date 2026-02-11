package com.moyz.adi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.*;
import com.moyz.adi.common.entity.KnowledgeBase;
import com.moyz.adi.common.service.KnowledgeBaseQaRecordReferenceService;
import com.moyz.adi.common.service.KnowledgeBaseQaRefGraphService;
import com.moyz.adi.common.service.KnowledgeBaseQaService;
import com.moyz.adi.common.service.KnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
/**
 * 知识库问答接口控制器。
 */
@Tag(name = "知识库问答controller")
@RequestMapping("/knowledge-base/qa/")
@RestController
public class KnowledgeBaseQAController {

    /**
     * 知识库服务，提供知识库基础能力。
     */
    @Resource
    private KnowledgeBaseService knowledgeBaseService;

    /**
     * 知识库问答服务，负责问答记录管理。
     */
    @Resource
    private KnowledgeBaseQaService knowledgeBaseQaService;

    /**
     * 问答记录向量引用服务。
     */
    @Resource
    private KnowledgeBaseQaRecordReferenceService knowledgeBaseQaRecordReferenceService;

    /**
     * 问答记录图谱引用服务。
     */
    @Resource
    private KnowledgeBaseQaRefGraphService knowledgeBaseQaRefGraphService;

    /**
     * 新增知识库问答记录。
     *
     * @param kbUuid 知识库 UUID
     * @param req 问答请求
     * @return 问答记录
     */
    @PostMapping("/add/{kbUuid}")
    public KbQaDto add(@PathVariable String kbUuid, @RequestBody @Validated QARecordReq req) {
        KnowledgeBase knowledgeBase = knowledgeBaseService.getOrThrow(kbUuid);
        return knowledgeBaseQaService.add(knowledgeBase, req);
    }

    /**
     * 以 SSE 方式返回问答流式响应。
     *
     * @param qaRecordUuid 问答记录 UUID
     * @return SSE 事件流
     */
    @Operation(summary = "流式响应")
    @PostMapping(value = "/process/{qaRecordUuid}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sseAsk(@PathVariable String qaRecordUuid) {
        // 由服务层统一处理限额校验与异步推送，控制器只负责路由入口
        return knowledgeBaseService.sseAsk(qaRecordUuid);
    }

    /**
     * 搜索问答记录并分页返回。
     *
     * @param kbUuid 知识库 UUID
     * @param keyword 搜索关键词
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return 问答记录分页结果
     */
    @GetMapping("/search")
    public Page<KbQaDto> list(String kbUuid, String keyword, @NotNull @Min(1) Integer currentPage, @NotNull @Min(10) Integer pageSize) {
        return knowledgeBaseQaService.search(kbUuid, keyword, currentPage, pageSize);
    }

    /**
     * 删除指定问答记录（逻辑删除）。
     *
     * @param uuid 问答记录 UUID
     * @return 是否删除成功
     */
    @PostMapping("/del/{uuid}")
    public boolean recordDel(@PathVariable String uuid) {
        return knowledgeBaseQaService.softDelete(uuid);
    }

    /**
     * 查询问答记录关联的向量引用。
     *
     * @param uuid 问答记录 UUID
     * @return 向量引用列表
     */
    @GetMapping("/embedding-ref/{uuid}")
    public List<RefEmbeddingDto> embeddingRef(@PathVariable String uuid) {
        return knowledgeBaseQaRecordReferenceService.listRefEmbeddings(uuid);
    }

    /**
     * 查询问答记录关联的图谱引用。
     *
     * @param uuid 问答记录 UUID
     * @return 图谱引用信息
     */
    @GetMapping("/graph-ref/{uuid}")
    public RefGraphDto graphRef(@PathVariable String uuid) {
        return knowledgeBaseQaRefGraphService.getByQaUuid(uuid);
    }

    /**
     * 清空当前用户的问答记录。
     */
    @PostMapping("/clear")
    public void recordDel() {
        knowledgeBaseQaService.clearByCurrentUser();
    }
}
