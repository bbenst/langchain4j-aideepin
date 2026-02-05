package com.moyz.adi.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ConvMsg数据传输对象
 */
@Data
public class ConvMsgDto {
    /**
     * 主键ID
     */
    @JsonIgnore
    private Long id;
    /**
     * 消息的uuid
     */
    @Schema(title = "消息的uuid")
    private String uuid;
    /**
     * 父级消息id
     */
    @Schema(title = "父级消息id")
    private Long parentMessageId;
    /**
     * 对话的消息
     */
    @Schema(title = "对话的消息")
    private String remark;
    /**
     * 思考的内容
     */
    @Schema(title = "思考的内容")
    private String thinkingContent;
    /**
     * 音频文件uuid
     */
    @Schema(title = "音频文件uuid")
    private String audioUuid;
    /**
     * 音频文件Url
     */
    @Schema(title = "音频文件Url")
    private String audioUrl;
    /**
     * 语音聊天时产生的音频时长，单位秒
     */
    @Schema(title = "语音聊天时产生的音频时长，单位秒")
    private Integer audioDuration;
    /**
     * 产生该消息的角色：1: 用户,2:系统,3:助手
     */
    @Schema(title = "产生该消息的角色：1: 用户,2:系统,3:助手")
    private Integer messageRole;
    /**
     * 消耗的token数量
     */
    @Schema(title = "消耗的token数量")
    private Integer tokens;
    /**
     * 创建时间
     */
    @Schema(title = "创建时间")
    private LocalDateTime createTime;
    /**
     * AI模型ID
     */
    @Schema(title = "model id")
    private Long aiModelId;
    /**
     * AI模型平台
     */
    @Schema(title = "model platform name")
    private String aiModelPlatform;
    /**
     * 附件地址
     */
    @Schema(title = "附件地址")
    private List<String> attachmentUrls;
    /**
     * 子级消息（一般指的是AI的响应）
     */
    @Schema(title = "子级消息（一般指的是AI的响应）")
    private List<ConvMsgDto> children;
    /**
     * 内容格式，2：文本；3：音频
     */
    @Schema(title = "内容格式，2：文本；3：音频")
    private Integer contentType;
    /**
     * 是否引用了向量库知识
     */
    @Schema(title = "是否引用了向量库知识")
    private Boolean isRefEmbedding;
    /**
     * 是否引用了图谱库知识
     */
    @Schema(title = "是否引用了图谱库知识")
    private Boolean isRefGraph;
}
