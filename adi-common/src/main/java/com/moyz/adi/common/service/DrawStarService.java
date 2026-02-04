package com.moyz.adi.common.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.entity.DrawStar;
import com.moyz.adi.common.mapper.DrawStarMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.moyz.adi.common.cosntant.RedisKeyConstant.USER_INFO;

/**
 * 绘图收藏服务。
 */
@Slf4j
@Service
public class DrawStarService extends ServiceImpl<DrawStarMapper, DrawStar> {

    /**
     * 查询当前用户收藏记录。
     *
     * @param maxId    最大 ID
     * @param pageSize 页大小
     * @return 收藏列表
     */
    public List<DrawStar> listByCurrentUser(Long maxId, int pageSize) {
        return this.lambdaQuery()
                .eq(DrawStar::getUserId, ThreadContext.getCurrentUserId())
                .eq(DrawStar::getIsDeleted, false)
                .lt(DrawStar::getId, maxId)
                .orderByDesc(DrawStar::getId)
                .last("limit " + pageSize)
                .list();
    }

    /**
     * 切换收藏状态。
     *
     * @param drawId 绘图 ID
     * @param userId 用户 ID
     * @return 是否操作成功
     */
    @CacheEvict(cacheNames = USER_INFO, condition = "#drawId>0 && #userId>0", key = "'star:'+#drawId+':'+#userId")
    public boolean toggle(Long drawId, Long userId) {
        DrawStar drawStar = this.lambdaQuery()
                .eq(DrawStar::getDrawId, drawId)
                .eq(DrawStar::getUserId, userId)
                .one();
        if (null != drawStar) {
            return this.lambdaUpdate()
                    .set(DrawStar::getIsDeleted, !drawStar.getIsDeleted())
                    .eq(DrawStar::getId, drawStar.getId())
                    .update();
        } else {
            DrawStar newObj = new DrawStar();
            newObj.setDrawId(drawId);
            newObj.setUserId(userId);
            baseMapper.insert(newObj);
            return true;
        }
    }

    /**
     * 判断是否已收藏。
     *
     * @param drawId 绘图 ID
     * @param userId 用户 ID
     * @return 是否已收藏
     */
    @Cacheable(cacheNames = USER_INFO, condition = "#drawId>0 && #userId>0", key = "'star:'+#drawId+':'+#userId")
    public boolean isStarred(Long drawId, Long userId) {
        DrawStar drawStar = this.lambdaQuery()
                .eq(DrawStar::getDrawId, drawId)
                .eq(DrawStar::getUserId, userId)
                .one();
        return null != drawStar && !drawStar.getIsDeleted();
    }

}
