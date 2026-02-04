package com.moyz.adi.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.PromptsSaveReq;
import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.dto.PromptDto;
import com.moyz.adi.common.dto.PromptListResp;
import com.moyz.adi.common.entity.Prompt;
import com.moyz.adi.common.mapper.PromptMapper;
import com.moyz.adi.common.util.LocalDateTimeUtil;
import com.moyz.adi.common.util.MPPageUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提示词管理服务。
 */
@Slf4j
@Service
public class PromptService extends ServiceImpl<PromptMapper, Prompt> {

    /**
     * 获取用户全部提示词。
     *
     * @param userId 用户 ID
     * @return 提示词列表
     */
    public List<PromptDto> getAll(long userId) {
        List<Prompt> prompts = this.lambdaQuery().eq(Prompt::getUserId, userId).eq(Prompt::getIsDeleted, false).list();
        return MPPageUtil.convertToList(prompts, PromptDto.class);
    }

    /**
     * 分页搜索提示词。
     *
     * @param keyword     关键词
     * @param currentPage 当前页
     * @param pageSize    页大小
     * @return 分页结果
     */
    public Page<PromptDto> search(String keyword, int currentPage, int pageSize) {
        Page<Prompt> promptPage;
        if (StringUtils.isNotBlank(keyword)) {
            promptPage = this.lambdaQuery()
                    .eq(Prompt::getUserId, ThreadContext.getCurrentUserId())
                    .eq(Prompt::getIsDeleted, false)
                    .like(Prompt::getAct, keyword)
                    .page(new Page<>(currentPage, pageSize));
        } else {
            promptPage = this.lambdaQuery()
                    .eq(Prompt::getUserId, ThreadContext.getCurrentUserId())
                    .eq(Prompt::getIsDeleted, false)
                    .page(new Page<>(currentPage, pageSize));
        }
        return MPPageUtil.convertToPage(promptPage, new Page<>(), PromptDto.class);
    }

    /**
     * 自动补全提示词列表。
     *
     * @param keyword 关键词
     * @return 提示词列表
     */
    public List<PromptDto> autocomplete(String keyword) {
        List<Prompt> promptPage;
        if (StringUtils.isNotBlank(keyword)) {
            promptPage = this.lambdaQuery()
                    .eq(Prompt::getUserId, ThreadContext.getCurrentUserId())
                    .eq(Prompt::getIsDeleted, false)
                    .like(Prompt::getAct, keyword)
                    .last("limit 10")
                    .list();
        } else {
            promptPage = this.lambdaQuery()
                    .eq(Prompt::getUserId, ThreadContext.getCurrentUserId())
                    .eq(Prompt::getIsDeleted, false)
                    .last("limit 10")
                    .list();
        }
        return MPPageUtil.convertToList(promptPage, PromptDto.class);
    }

    /**
     * 查询指定更新时间之后的提示词列表。
     *
     * @param minUpdateTime 最小更新时间
     * @return 列表响应
     */
    public PromptListResp listByMinUpdateTime(LocalDateTime minUpdateTime) {
        LocalDateTime tmpUpdatTime = minUpdateTime;
        if (null == tmpUpdatTime) {
            tmpUpdatTime = LocalDateTime.of(2023, 1, 1, 1, 1);
        }
        PromptListResp resp = new PromptListResp();
        List<Prompt> list = this.lambdaQuery()
                .eq(Prompt::getUserId, ThreadContext.getCurrentUserId())
                .gt(Prompt::getUpdateTime, tmpUpdatTime)
                .orderByAsc(Prompt::getUpdateTime, Prompt::getId)
                .last("limit 100")
                .list();
        if (list.isEmpty()) {
            resp.setMaxUpdateTime(LocalDateTimeUtil.format(LocalDateTime.now()));
            resp.setPrompts(new ArrayList<>());
            return resp;
        }
        LocalDateTime maxUpdateTime = list.stream().reduce((a, b) -> {
            if (a.getUpdateTime().isAfter(b.getUpdateTime())) {
                return a;
            }
            return b;
        }).get().getUpdateTime();
        List<PromptDto> promptDtos = MPPageUtil.convertToList(list, PromptDto.class);
        resp.setMaxUpdateTime(LocalDateTimeUtil.format(maxUpdateTime));
        resp.setPrompts(promptDtos);
        return resp;
    }

    /**
     * 批量保存提示词。
     *
     * @param savePromptsReq 保存请求
     * @return 标题到 ID 的映射
     */
    public Map<String, Long> savePrompts(PromptsSaveReq savePromptsReq) {
        Map<String, Long> titleToId = new HashMap<>();

        Long userId = ThreadContext.getCurrentUserId();
        for (PromptDto promptDto : savePromptsReq.getPrompts()) {

            String title = promptDto.getAct();
            Prompt prompt = new Prompt();

            Prompt existOne = this.lambdaQuery()
                    .eq(Prompt::getUserId, userId)
                    .eq(Prompt::getAct, title)
                    .eq(Prompt::getIsDeleted, false)
                    .one();
            if (null != existOne) {
                // 更新已有记录
                prompt.setId(existOne.getId());
                prompt.setUserId(userId);
                prompt.setAct(title);
                prompt.setPrompt(promptDto.getPrompt());
                this.updateById(prompt);
                titleToId.put(title, existOne.getId());
            } else {
                // 新建记录
                prompt.setUserId(userId);
                prompt.setAct(title);
                prompt.setPrompt(promptDto.getPrompt());
                this.save(prompt);

                Prompt addedOne = this.lambdaQuery()
                        .eq(Prompt::getUserId, userId)
                        .eq(Prompt::getAct, title)
                        .one();
                titleToId.put(title, addedOne.getId());
            }
        }
        return titleToId;
    }

    /**
     * 软删除提示词。
     *
     * @param id 提示词 ID
     * @return 是否删除成功
     */
    public boolean softDelete(Long id) {
        Prompt prompt = this.lambdaQuery()
                .eq(Prompt::getUserId, ThreadContext.getCurrentUserId())
                .eq(Prompt::getId, id)
                .eq(Prompt::getIsDeleted, false)
                .one();
        if (null == prompt) {
            return false;
        }
        Prompt updateOne = new Prompt();
        updateOne.setId(id);
        updateOne.setIsDeleted(true);
        return this.updateById(updateOne);
    }

    /**
     * 编辑提示词。
     *
     * @param id     提示词 ID
     * @param title  标题
     * @param remark 内容
     * @return 是否更新成功
     */
    public boolean edit(Long id, String title, String remark) {
        Prompt prompt = this.lambdaQuery()
                .eq(Prompt::getId, id)
                .eq(Prompt::getUserId, ThreadContext.getCurrentUserId())
                .eq(Prompt::getIsDeleted, false)
                .one();
        if (null == prompt) {
            return false;
        }
        Prompt updateOne = new Prompt();
        updateOne.setId(id);
        updateOne.setAct(title);
        updateOne.setPrompt(remark);
        return this.updateById(updateOne);
    }

    /**
     * 搜索提示词（最多 10 条）。
     *
     * @param keyword 关键词
     * @return 提示词列表
     */
    public List<PromptDto> search(String keyword) {
        List<Prompt> prompts = this.lambdaQuery()
                .eq(Prompt::getUserId, ThreadContext.getCurrentUserId())
                .like(Prompt::getAct, keyword)
                .last("limit 10")
                .list();
        return MPPageUtil.convertToList(prompts, PromptDto.class);
    }
}
