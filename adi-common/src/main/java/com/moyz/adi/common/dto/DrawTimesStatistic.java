package com.moyz.adi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DrawTimesStatistic对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DrawTimesStatistic {
    /**
     * todayDrawTimes
     */
    private Integer todayDrawTimes;
    /**
     * monthDrawTimes
     */
    private Integer monthDrawTimes;
}
