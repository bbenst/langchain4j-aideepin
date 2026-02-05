package com.moyz.adi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * ConvMsg列表响应
 */
@Data
@AllArgsConstructor
public class ConvMsgListResp {
    /**
     * 最小MsgUUID
     */
    private String minMsgUuid;
    /**
     * msg列表
     */
    private List<ConvMsgDto> msgList;
}
