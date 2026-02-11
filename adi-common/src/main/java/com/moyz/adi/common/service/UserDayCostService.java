package com.moyz.adi.common.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moyz.adi.common.entity.User;
import com.moyz.adi.common.entity.UserDayCost;
import com.moyz.adi.common.mapper.UserDayCostMapper;
import com.moyz.adi.common.util.LocalDateTimeUtil;
import com.moyz.adi.common.vo.CostStat;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户每日消耗统计服务。
 */
@Slf4j
@Service
public class UserDayCostService extends ServiceImpl<UserDayCostMapper, UserDayCost> {

    /**
     * 自身代理对象（用于触发异步方法）。
     */
    @Lazy
    @Resource
    private UserDayCostService self;

    /**
     * 累加用户的 token 消耗。
     *
     * @param user   用户
     * @param tokens token 数量
     * @param isFree 消耗的是否免费额度
     * @return 无
     */
    public void appendCostToUser(User user, int tokens, boolean isFree) {
        log.info("用户{}增加消耗token数量:{}", user.getName(), tokens);
        // 非正数直接返回，避免无效写入
        if (tokens <= 0) {
            return;
        }
        // 先查询当天记录，存在则累加，不存在则新增
        UserDayCost userDayCost = getTodayCost(user, isFree);
        UserDayCost saveOrUpdateInst = new UserDayCost();
        if (null == userDayCost) {
            saveOrUpdateInst.setUserId(user.getId());
            saveOrUpdateInst.setDay(LocalDateTimeUtil.getToday());
            saveOrUpdateInst.setTokens(tokens);
            saveOrUpdateInst.setRequestTimes(1);
        } else {
            saveOrUpdateInst.setId(userDayCost.getId());
            saveOrUpdateInst.setTokens(userDayCost.getTokens() + tokens);
            saveOrUpdateInst.setRequestTimes(userDayCost.getRequestTimes() + 1);
        }
        saveOrUpdateInst.setIsFree(isFree);
        self.saveOrUpdate(saveOrUpdateInst);
    }

    /**
     * 统计用户当月与当日消耗。
     *
     * @param userId 用户 ID
     * @param isFree 是否免费额度
     * @return 消耗统计
     */
    public CostStat costStatByUser(long userId, boolean isFree) {
        CostStat result = new CostStat();

        int today = LocalDateTimeUtil.getIntDay(LocalDateTime.now());
        int start = LocalDateTimeUtil.getIntDay(LocalDateTime.now().withDayOfMonth(1));
        int end = LocalDateTimeUtil.getIntDay(LocalDateTime.now().plusMonths(1).withDayOfMonth(1).minusDays(1));

        List<UserDayCost> userDayCostList = this.lambdaQuery()
                .eq(UserDayCost::getUserId, userId)
                .between(UserDayCost::getDay, start, end)
                .eq(UserDayCost::getIsFree, isFree)
                .list();
        for (UserDayCost userDayCost : userDayCostList) {
            result.setTextTokenCostByMonth(result.getTextTokenCostByMonth() + userDayCost.getTokens());
            result.setTextRequestTimesByMonth(result.getTextRequestTimesByMonth() + userDayCost.getRequestTimes());
            result.setDrawTimesByMonth(result.getDrawTimesByMonth() + userDayCost.getDrawTimes());
            if (userDayCost.getDay() == today) {
                result.setTextTokenCostByDay(userDayCost.getTokens());
                result.setTextRequestTimesByDay(userDayCost.getRequestTimes());
                result.setDrawTimesByDay(userDayCost.getDrawTimes());
            }
            result.setFree(userDayCost.getIsFree());
        }
        return result;
    }

    /**
     * 获取用户当天消耗记录。
     *
     * @param user   用户
     * @param isFree 是否免费额度
     * @return 消耗记录
     */
    public UserDayCost getTodayCost(User user, boolean isFree) {
        return this.lambdaQuery()
                .eq(UserDayCost::getUserId, user.getId())
                .eq(UserDayCost::getDay, LocalDateTimeUtil.getToday())
                .eq(UserDayCost::getIsFree, isFree)
                .one();
    }

    /**
     * 统计当天总消耗。
     *
     * @return 总消耗
     */
    public Integer sumTodayCost() {
        int today = LocalDateTimeUtil.getToday();
        return baseMapper.sumCostByDay(today).intValue();
    }

    /**
     * 统计当月总消耗。
     *
     * @return 总消耗
     */
    public Integer sumCurrentMonthCost() {
        int start = LocalDateTimeUtil.getIntDay(LocalDateTime.now().withDayOfMonth(1));
        int end = LocalDateTimeUtil.getIntDay(LocalDateTime.now().plusMonths(1).withDayOfMonth(1).minusDays(1));
        return baseMapper.sumCostByDayPeriod(start, end).intValue();
    }
}
