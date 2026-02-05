package com.moyz.adi.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * LLM相关的统计
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenCostStatistic implements Serializable {
    /**
     * 序列化版本号
     */
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * todayTokenCost
     */
    private Integer todayTokenCost;
    /**
     * monthTokenCost
     */
    private Integer monthTokenCost;
}
