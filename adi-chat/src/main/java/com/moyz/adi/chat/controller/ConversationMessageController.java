package com.moyz.adi.chat.controller;

import com.moyz.adi.common.dto.AskReq;
import com.moyz.adi.common.dto.RefEmbeddingDto;
import com.moyz.adi.common.dto.RefGraphDto;
import com.moyz.adi.common.service.ConversationMessageRefEmbeddingService;
import com.moyz.adi.common.service.ConversationMessageRefGraphService;
import com.moyz.adi.common.service.ConversationMessageService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
/**
 * 对话消息相关接口控制器。
 */
@RestController
@RequestMapping("/conversation/message")
@Validated
public class ConversationMessageController {

    /**
     * 对话消息服务，负责消息发送与基础查询。
     */
    @Resource
    private ConversationMessageService conversationMessageService;

    /**
     * 消息向量引用服务，用于查询关联的向量检索结果。
     */
    @Resource
    private ConversationMessageRefEmbeddingService conversationMessageRefEmbeddingService;

    /**
     * 消息图谱引用服务，用于查询关联的图谱信息。
     */
    @Resource
    private ConversationMessageRefGraphService conversationMessageRefGraphService;

    /**
     * 发送提问请求并以 SSE 形式返回模型输出。
     *
     * @param askReq 提问请求
     * @return SSE 事件流
     */
    @Operation(summary = "发送一个prompt给模型")
    @PostMapping(value = "/process", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter ask(@RequestBody @Validated AskReq askReq) {
        return conversationMessageService.sseAsk(askReq);
    }

    /**
     * 通过音频 UUID 查询识别得到的文本。
     *
     * @param audioUuid 音频 UUID
     * @return 音频对应的文本
     */
    @Operation(summary = "根据音频uuid获取对应的文本")
    @GetMapping("/text/{audioUuid}")
    public String getTextByAudioUuid(@PathVariable String audioUuid) {
        return conversationMessageService.getTextByAudioUuid(audioUuid);
    }

    /**
     * 查询消息关联的向量引用列表。
     *
     * @param uuid 消息 UUID
     * @return 向量引用列表
     */
    @GetMapping("/embedding-ref/{uuid}")
    public List<RefEmbeddingDto> embeddingRef(@PathVariable String uuid) {
        return conversationMessageRefEmbeddingService.listRefEmbeddings(uuid);
    }

    /**
     * 查询消息关联的图谱引用信息。
     *
     * @param uuid 消息 UUID
     * @return 图谱引用信息
     */
    @GetMapping("/graph-ref/{uuid}")
    public RefGraphDto graphRef(@PathVariable String uuid) {
        return conversationMessageRefGraphService.getByMsgUuid(uuid);
    }

    /**
     * 逻辑删除指定消息。
     *
     * @param uuid 消息 UUID
     * @return 是否删除成功
     */
    @PostMapping("/del/{uuid}")
    public boolean softDelete(@PathVariable String uuid) {
        return conversationMessageService.softDelete(uuid);
    }

}
