package com.moyz.adi.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 图片CostStatistic对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageCostStatistic implements Serializable {
    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;
    /**
     * todayCost
     */
    private Integer todayCost;
    /**
     * monthCost
     */
    private Integer monthCost;
}
