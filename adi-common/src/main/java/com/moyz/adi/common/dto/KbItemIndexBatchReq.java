package com.moyz.adi.common.dto;

import lombok.Data;

/**
 * KbItem索引Batch请求
 */
@Data
public class KbItemIndexBatchReq {
    /**
     * uuids
     */
    private String[] uuids;
    /**
     * 索引类型
     */
    private String[] indexTypes;
}
