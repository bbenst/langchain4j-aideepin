package com.moyz.adi.common.dto;

import com.moyz.adi.common.vo.TokenCostStatistic;
import lombok.Data;

/**
 * CostStat响应
 */
@Data
public class CostStatResp {
    /**
     * 免费TokenCost
     */
    private TokenCostStatistic freeTokenCost;
    /**
     * paidTokenCost
     */
    private TokenCostStatistic paidTokenCost;
    /**
     * paid请求Times
     */
    private RequestTimesStatistic paidRequestTimes;
    /**
     * 免费请求Times
     */
    private RequestTimesStatistic freeRequestTimes;
    /**
     * paidDrawTimes
     */
    private DrawTimesStatistic paidDrawTimes;
    /**
     * 免费DrawTimes
     */
    private DrawTimesStatistic freeDrawTimes;
}
