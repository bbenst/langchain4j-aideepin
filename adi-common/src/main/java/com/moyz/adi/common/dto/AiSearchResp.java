package com.moyz.adi.common.dto;

import lombok.Data;

import java.util.List;

/**
 * AI搜索响应
 */
@Data
public class AiSearchResp {
    /**
     * 最小ID
     */
    private Long minId;
    /**
     * records
     */
    private List<AiSearchRecordResp> records;
}
