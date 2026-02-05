package com.moyz.adi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 请求TimesStatistic对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestTimesStatistic implements Serializable {
    /**
     * 序列化版本号
     */
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * today请求Times
     */
    private Integer todayRequestTimes;
    /**
     * month请求Times
     */
    private Integer monthRequestTimes;
}
