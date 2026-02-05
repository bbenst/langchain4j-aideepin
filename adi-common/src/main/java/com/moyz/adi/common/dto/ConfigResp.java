package com.moyz.adi.common.dto;

import lombok.Data;

/**
 * 配置信息
 */
@Data
public class ConfigResp {
    /**
     * 上下文MsgPair数量
     */
    private Integer contextMsgPairNum;
    /**
     * 用户配额
     */
    private UserQuota userQuota;
    /**
     * 配额Cost
     */
    private CostStatResp quotaCost;
}
