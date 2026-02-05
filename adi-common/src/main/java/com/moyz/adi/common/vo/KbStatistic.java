package com.moyz.adi.common.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 知识库的统计信息
 */
@Data
public class KbStatistic implements Serializable {
    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;
    /**
     * kbTodayCreated
     */
    private Integer kbTodayCreated;
    /**
     * itemTodayCreated
     */
    private Integer itemTodayCreated;
    /**
     * kbTotal
     */
    private Integer kbTotal;
    /**
     * itemTotal
     */
    private Integer itemTotal;
}
