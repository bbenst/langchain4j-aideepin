package com.moyz.adi.common.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户的统计信息
 */
@Data
public class UserStatistic implements Serializable {
    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;
    /**
     * todayCreated
     */
    private Integer todayCreated;
    /**
     * todayActivated
     */
    private Integer todayActivated;
    /**
     * totalNormal
     */
    private Integer totalNormal;
}
