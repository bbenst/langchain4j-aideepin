package com.moyz.adi.common.dto;

import com.moyz.adi.common.vo.*;
import lombok.Data;

/**
 * Statistic数据传输对象
 */
@Data
public class StatisticDto {
    /**
     * 用户Statistic
     */
    private UserStatistic userStatistic;
    /**
     * kbStatistic
     */
    private KbStatistic kbStatistic;
    /**
     * TokenCostStatistic
     */
    private TokenCostStatistic tokenCostStatistic;
    /**
     * convStatistic
     */
    private ConvStatistic convStatistic;
    /**
     * 图片CostStatistic
     */
    private ImageCostStatistic imageCostStatistic;
}
