package com.moyz.adi.common.dto;

import com.moyz.adi.common.enums.EmbeddingStatusEnum;
import com.moyz.adi.common.enums.GraphicalStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * KbItem数据传输对象
 */
@Data
public class KbItemDto {
    /**
     * kbID
     */
    private Long kbId;
    /**
     * kbUUID
     */
    private String kbUuid;
    /**
     * source文件ID
     */
    private Long sourceFileId;
    /**
     * 主键ID
     */
    private Long id;
    /**
     * UUID
     */
    private String uuid;
    /**
     * 标题
     */
    private String title;
    /**
     * brief
     */
    private String brief;
    /**
     * 描述
     */
    private String remark;
    /**
     * 向量状态
     */
    private EmbeddingStatusEnum embeddingStatus;
    /**
     * 向量状态Change时间
     */
    private LocalDateTime embeddingStatusChangeTime;
    /**
     * graphical状态
     */
    private GraphicalStatusEnum graphicalStatus;
    /**
     * graphical状态Change时间
     */
    private LocalDateTime graphicalStatusChangeTime;
    /**
     * source文件名称
     */
    private String sourceFileName;
    /**
     * source文件UUID
     */
    private String sourceFileUuid;
    /**
     * source文件URL
     */
    private String sourceFileUrl;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
