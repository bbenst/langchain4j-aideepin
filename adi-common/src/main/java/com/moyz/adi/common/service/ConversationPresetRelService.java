package com.moyz.adi.common.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moyz.adi.common.dto.ConvPresetRelDto;
import com.moyz.adi.common.entity.ConversationPresetRel;
import com.moyz.adi.common.mapper.ConversationPresetRelMapper;
import com.moyz.adi.common.util.MPPageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 预设对话关联服务。
 */
@Slf4j
@Service
public class ConversationPresetRelService extends ServiceImpl<ConversationPresetRelMapper, ConversationPresetRel> {

    /**
     * 查询用户的预设对话关联列表。
     *
     * @param userId 用户 ID
     * @param limit  最大数量
     * @return 关联列表
     */
    public List<ConvPresetRelDto> listByUser(Long userId, Integer limit) {
        List<ConversationPresetRel> list = this.lambdaQuery()
                .eq(ConversationPresetRel::getUserId, userId)
                .eq(ConversationPresetRel::getIsDeleted, false)
                .last("limit " + limit)
                .list();
        return MPPageUtil.convertToList(list, ConvPresetRelDto.class);
    }

    /**
     * 软删除用户与对话的预设关联。
     *
     * @param userId 用户 ID
     * @param convId 对话 ID
     * @return 是否删除成功
     */
    public boolean softDelBy(Long userId, Long convId) {
        return this.lambdaUpdate()
                .eq(ConversationPresetRel::getUserId, userId)
                .eq(ConversationPresetRel::getUserConvId, convId)
                .set(ConversationPresetRel::getIsDeleted, true)
                .update();
    }
}
