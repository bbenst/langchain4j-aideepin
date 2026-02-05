package com.moyz.adi.common.vo;

import lombok.Data;

/**
 * 费用统计
 */
@Data
public class CostStat {
    /**
     * day
     */
    private int day;//天
    /**
     * text请求TimesByDay
     */
    private int textRequestTimesByDay; // 每日文本请求次数
    /**
     * textTokenCostByDay
     */
    private int textTokenCostByDay; // 每日文本令牌消耗
    /**
     * drawTimesByDay
     */
    private int drawTimesByDay; // 每日绘图次数
    /**
     * textTokenCostByMonth
     */
    private int textTokenCostByMonth; // 每月文本令牌消耗
    /**
     * text请求TimesByMonth
     */
    private int textRequestTimesByMonth; // 每月文本请求次数
    /**
     * drawTimesByMonth
     */
    private int drawTimesByMonth; // 每月绘图次数
    /**
     * 是否免费
     */
    private boolean isFree; // 是否免费
}
