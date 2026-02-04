package com.moyz.adi.common.service;

import com.moyz.adi.common.entity.User;
import com.moyz.adi.common.enums.UserStatusEnum;
import com.moyz.adi.common.vo.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static com.moyz.adi.common.cosntant.RedisKeyConstant.*;

/**
 * 统计信息服务。
 */
@Slf4j
@Service
public class StatisticService {

    /**
     * 用户服务。
     */
    @Resource
    private UserService userService;

    /**
     * 用户日消耗统计服务。
     */
    @Resource
    private UserDayCostService userDayCostService;

    /**
     * 知识库服务。
     */
    @Resource
    private KnowledgeBaseService knowledgeBaseService;

    /**
     * 知识点服务。
     */
    @Resource
    private KnowledgeBaseItemService knowledgeBaseItemService;

    /**
     * 对话服务。
     */
    @Resource
    private ConversationService conversationService;

    /**
     * 绘图服务。
     */
    @Resource
    private DrawService drawService;

    /**
     * 统计用户注册与激活信息。
     *
     * @return 用户统计信息
     */
    @Cacheable(value = STATISTIC + ":" + STATISTIC_USER)
    public UserStatistic calUserStat() {
        UserStatistic result = new UserStatistic();
        LocalDate today = LocalDate.now();
        int todayCreated = userService.lambdaQuery()
                .gt(User::getCreateTime, today)
                .count()
                .intValue();
        result.setTodayCreated(todayCreated);

        int todayActivated = userService.lambdaQuery()
                .gt(User::getCreateTime, today)
                .eq(User::getUserStatus, UserStatusEnum.NORMAL)
                .count()
                .intValue();
        int totalNormal = userService.lambdaQuery()
                .eq(User::getUserStatus, UserStatusEnum.NORMAL)
                .count()
                .intValue();

        result.setTodayCreated(todayCreated);
        result.setTotalNormal(totalNormal);
        result.setTodayActivated(todayActivated);
        return result;
    }

    /**
     * 统计 Token 消耗信息。
     *
     * @return Token 消耗统计
     */
    @Cacheable(value = STATISTIC + ":" + STATISTIC_TOKEN_COST)
    public TokenCostStatistic calTokenCostStat() {
        Integer todayCost = userDayCostService.sumTodayCost();
        Integer currentMonthCost = userDayCostService.sumCurrentMonthCost();
        TokenCostStatistic aiModelStat = new TokenCostStatistic();
        aiModelStat.setTodayTokenCost(todayCost);
        aiModelStat.setMonthTokenCost(currentMonthCost);
        return aiModelStat;
    }

    /**
     * 统计图片生成消耗信息。
     *
     * @return 图片消耗统计
     */
    @Cacheable(value = STATISTIC + ":" + STATISTIC_IMAGE_COST)
    public ImageCostStatistic calImageCostStat() {
        return ImageCostStatistic.builder()
                .todayCost(drawService.sumTodayCost())
                .monthCost(drawService.sumCurrMonthCost())
                .build();
    }

    /**
     * 统计知识库与知识点信息。
     *
     * @return 知识库统计
     */
    @Cacheable(value = STATISTIC + ":" + STATISTIC_KNOWLEDGE_BASE)
    public KbStatistic calKbStat() {
        int kbTodayCreated = knowledgeBaseService.countTodayCreated();
        int kbTotal = knowledgeBaseService.countAllCreated();
        int itemTodayCreated = knowledgeBaseItemService.countTodayCreated();
        int itemTotal = knowledgeBaseItemService.countAllCreated();
        KbStatistic stat = new KbStatistic();
        stat.setKbTodayCreated(kbTodayCreated);
        stat.setKbTotal(kbTotal);
        stat.setItemTotal(itemTotal);
        stat.setItemTodayCreated(itemTodayCreated);
        return stat;
    }

    /**
     * 统计会话信息。
     *
     * @return 会话统计
     */
    @Cacheable(value = STATISTIC + ":" + STATISTIC_CONVERSATION)
    public ConvStatistic calConvStatistic() {
        return ConvStatistic.builder()
                .todayCreated(conversationService.countTodayCreated())
                .total(conversationService.countAllCreated())
                .build();
    }

}
