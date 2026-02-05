package com.moyz.adi.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 会话统计信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvStatistic implements Serializable {
    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;
    /**
     * todayCreated
     */
    private Integer todayCreated;
    /**
     * total
     */
    private Integer total;
}
