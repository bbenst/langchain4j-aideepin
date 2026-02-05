package com.moyz.adi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户配额对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserQuota {
    /**
     * TokenByDay
     */
    private Integer tokenByDay;
    /**
     * TokenByMonth
     */
    private Integer tokenByMonth;
    /**
     * 请求TimesByDay
     */
    private Integer requestTimesByDay;
    /**
     * 请求TimesByMonth
     */
    private Integer requestTimesByMonth;
    /**
     * drawByDay
     */
    private Integer drawByDay;
    /**
     * drawByMonth
     */
    private Integer drawByMonth;
}
