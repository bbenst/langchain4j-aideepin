package com.moyz.adi.chat.controller;

import com.moyz.adi.common.dto.ConvAddReq;
import com.moyz.adi.common.dto.ConvDto;
import com.moyz.adi.common.dto.ConvEditReq;
import com.moyz.adi.common.dto.ConvMsgListResp;
import com.moyz.adi.common.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 对话相关接口控制器。
 */
@Tag(name = "对话controller", description = "对话controller")
@RequestMapping("/conversation")
@RestController
@Validated
public class ConversationController {

    /**
     * 对话服务，负责会话的增删改查与消息读取。
     */
    @Resource
    private ConversationService conversationService;

    /**
     * 获取当前用户的全部对话列表。
     *
     * @return 对话列表
     */
    @Operation(summary = "获取当前用户所有的对话")
    @GetMapping("/list")
    public List<ConvDto> list() {
        return conversationService.listByUser();
    }

    /**
     * 查询指定对话的消息列表。
     *
     * @param uuid 对话唯一标识
     * @param maxMsgUuid 最大消息 UUID，用于分页游标
     * @param pageSize 每页条数
     * @return 对话与消息列表响应
     */
    @Operation(summary = "查询某个对话的信息列表")
    @GetMapping("/{uuid}")
    public ConvMsgListResp detail(
            @Parameter(name = "对话uuid") @PathVariable @NotBlank(message = "对话uuid不能为空") String uuid
            , @Parameter(name = "最大uuid") @RequestParam String maxMsgUuid
            , @Parameter(name = "每页数量") @RequestParam @Min(1) @Max(100) int pageSize) {
        return conversationService.detail(uuid, maxMsgUuid, pageSize);
    }

    /**
     * 新建对话。
     *
     * @param convAddReq 新建对话请求
     * @return 新建后的对话信息
     */
    @PostMapping("/add")
    public ConvDto add(@RequestBody @Validated ConvAddReq convAddReq) {
        return conversationService.add(convAddReq);
    }

    /**
     * 基于预设会话创建用户对话。
     *
     * @param presetUuid 预设会话 UUID
     * @return 新建后的对话信息
     */
    @Operation(summary = "根据预设会话创建用户自己的会话")
    @PostMapping("/addByPreset")
    public ConvDto addByPreset(@Length(min = 32, max = 32) @RequestParam String presetUuid) {
        return conversationService.addByPresetConv(presetUuid);
    }

    /**
     * 编辑对话信息。
     *
     * @param uuid 对话 UUID
     * @param convEditReq 编辑请求
     * @return 是否编辑成功
     */
    @PostMapping("/edit/{uuid}")
    public boolean edit(@PathVariable String uuid, @RequestBody ConvEditReq convEditReq) {
        return conversationService.edit(uuid, convEditReq);
    }

    /**
     * 逻辑删除对话。
     *
     * @param uuid 对话 UUID
     * @return 是否删除成功
     */
    @PostMapping("/del/{uuid}")
    public boolean softDel(@PathVariable String uuid) {
        return conversationService.softDel(uuid);
    }
}
