package com.moyz.adi.admin.controller;

import com.moyz.adi.common.dto.StatisticDto;
import com.moyz.adi.common.service.StatisticService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计信息接口控制器。
 */
@RestController
@RequestMapping("/admin/statistic")
@Validated
public class StatisticController {

    /**
     * 统计服务，用于计算各类业务指标。
     */
    @Resource
    private StatisticService statisticService;

    /**
     * 获取统计汇总信息。
     *
     * @return 统计结果
     */
    @GetMapping("/info")
    public StatisticDto statistic() {
        StatisticDto result = new StatisticDto();
        // 分别计算各模块统计信息并汇总返回
        result.setKbStatistic(statisticService.calKbStat());
        result.setUserStatistic(statisticService.calUserStat());
        result.setTokenCostStatistic(statisticService.calTokenCostStat());
        result.setConvStatistic(statisticService.calConvStatistic());
        result.setImageCostStatistic(statisticService.calImageCostStat());
        return result;
    }
}
