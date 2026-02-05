package com.moyz.adi.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.cosntant.AdiConstant;
import com.moyz.adi.common.dto.*;
import com.moyz.adi.common.entity.*;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.mapper.ConversationMapper;
import com.moyz.adi.common.util.JsonUtil;
import com.moyz.adi.common.util.LocalCache;
import com.moyz.adi.common.util.MPPageUtil;
import com.moyz.adi.common.util.UuidUtil;
import com.moyz.adi.common.vo.AudioConfig;
import com.moyz.adi.common.vo.TtsSetting;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.moyz.adi.common.enums.ErrorEnum.*;
import static com.moyz.adi.common.util.LocalCache.MODEL_ID_TO_OBJ;

/**
 * 对话管理服务。
 */
@Slf4j
@Service
public class ConversationService extends ServiceImpl<ConversationMapper, Conversation> {

    /**
     * 自身代理对象（用于触发异步方法）。
     */
    @Lazy
    @Resource
    private ConversationService self;

    /**
     * 系统配置服务。
     */
    @Resource
    private SysConfigService sysConfigService;

    /**
     * 对话消息服务。
     */
    @Resource
    private ConversationMessageService conversationMessageService;

    /**
     * 预设对话服务。
     */
    @Resource
    private ConversationPresetService conversationPresetService;

    /**
     * 预设对话关联服务。
     */
    @Resource
    private ConversationPresetRelService conversationPresetRelService;

    /**
     * 用户 MCP 服务。
     */
    @Resource
    private UserMcpService userMcpService;

    /**
     * 知识库服务。
     */
    @Resource
    private KnowledgeBaseService knowledgeBaseService;

    /**
     * 文件服务。
     */
    @Resource
    private FileService fileService;

    /**
     * 模型服务。
     */
    @Resource
    private AiModelService aiModelService;

    /**
     * 按条件分页查询对话列表。
     *
     * @param convSearchReq 查询条件
     * @param currentPage   当前页
     * @param pageSize      页大小
     * @return 分页结果
     */
    public Page<ConvDto> search(ConvSearchReq convSearchReq, int currentPage, int pageSize) {
        Page<Conversation> page = this.lambdaQuery()
                .eq(Conversation::getIsDeleted, false)
                .like(!StringUtils.isBlank(convSearchReq.getTitle()), Conversation::getTitle, convSearchReq.getTitle())
                .orderByDesc(Conversation::getId)
                .page(new Page<>(currentPage, pageSize));
        return MPPageUtil.convertToPage(page, ConvDto.class);
    }

    /**
     * 查询当前用户的对话列表。
     *
     * @return 对话 DTO 列表
     */
    public List<ConvDto> listByUser() {
        User user = ThreadContext.getCurrentUser();
        List<Conversation> list = this.lambdaQuery()
                .eq(Conversation::getUserId, user.getId())
                .eq(Conversation::getIsDeleted, false)
                .orderByDesc(Conversation::getId)
                .last("limit " + sysConfigService.getConversationMaxNum())
                .list();
        return MPPageUtil.convertToList(list, ConvDto.class, (source, target) -> {
            setMcpToDto(source, target);
            setKbInfoToDto(source, target);
            return target;
        });
    }

    /**
     * 查询对话{@code uuid}的消息列表
     *
     * @param uuid       对话的uuid
     * @param maxMsgUuid 最大uuid（转换成id进行判断）
     * @param pageSize   每页数量
     * @return 列表
     * @throws BaseException 对话不存在或分页游标无效时抛出异常
     */
    public ConvMsgListResp detail(String uuid, String maxMsgUuid, int pageSize) {
        Conversation conversation = this.lambdaQuery().eq(Conversation::getUuid, uuid).one();
        if (null == conversation) {
            log.error("conversation not exist, uuid: {}", uuid);
            throw new BaseException(A_CONVERSATION_NOT_EXIST);
        }

        long maxId = Long.MAX_VALUE;
        if (StringUtils.isNotBlank(maxMsgUuid)) {
            ConversationMessage maxMsg = conversationMessageService.lambdaQuery()
                    .select(ConversationMessage::getId)
                    .eq(ConversationMessage::getUuid, maxMsgUuid)
                    .eq(ConversationMessage::getIsDeleted, false)
                    .one();
            if (null == maxMsg) {
                throw new BaseException(A_DATA_NOT_FOUND);
            }
            // 用 id 作为分页边界，避免 UUID 无序导致分页错乱
            maxId = maxMsg.getId();
        }

        List<ConversationMessage> questions = conversationMessageService.listQuestionsByConvId(conversation.getId(), maxId, pageSize);
        if (questions.isEmpty()) {
            return new ConvMsgListResp(StringUtils.EMPTY, Collections.emptyList());
        }
        // 取最小 id 的消息作为下一页游标，保证分页稳定
        String minUuid = questions.stream().reduce(questions.get(0), (a, b) -> {
            if (a.getId() < b.getId()) {
                return a;
            }
            return b;
        }).getUuid();
        // 组装问题消息内容
        List<ConvMsgDto> userMessages = MPPageUtil.convertToList(questions, ConvMsgDto.class, (source, target) -> {
            if (StringUtils.isNotBlank(source.getAttachments())) {
                // 统一转换为可访问 URL，避免前端拼路径
                List<String> urls = fileService.getUrls(Arrays.stream(source.getAttachments().split(",")).toList());
                target.setAttachmentUrls(urls);
            } else {
                target.setAttachmentUrls(Collections.emptyList());
            }
            if (StringUtils.isNotBlank(source.getAudioUuid())) {
                // 统一转换为可访问 URL，便于前端直接播放
                target.setAudioUrl(fileService.getUrl(source.getAudioUuid()));
            } else {
                target.setAudioUrl("");
            }
            return target;
        });
        ConvMsgListResp result = new ConvMsgListResp(minUuid, userMessages);

        // 组装回答消息内容
        List<Long> parentIds = questions.stream().map(ConversationMessage::getId).toList();
        // 批量查询子消息并分组，减少多次查询开销
        List<ConversationMessage> childMessages = conversationMessageService
                .lambdaQuery()
                .in(ConversationMessage::getParentMessageId, parentIds)
                .eq(ConversationMessage::getIsDeleted, false)
                .list();
        Map<Long, List<ConversationMessage>> idToMessages = childMessages.stream().collect(Collectors.groupingBy(ConversationMessage::getParentMessageId));

        // 将 AI 回答填充到对应的问题下
        result.getMsgList().forEach(item -> {
            List<ConvMsgDto> children = MPPageUtil.convertToList(idToMessages.get(item.getId()), ConvMsgDto.class);
            if (children.size() > 1) {
                // 重新生成场景下按时间倒序，优先展示最新回答
                children = children.stream().sorted(Comparator.comparing(ConvMsgDto::getCreateTime).reversed()).toList();
            }

            for (ConvMsgDto convMsgDto : children) {
                AiModel aiModel = MODEL_ID_TO_OBJ.get(convMsgDto.getAiModelId());
                // 前置填充模型平台，减少前端二次查询
                convMsgDto.setAiModelPlatform(null == aiModel ? "" : aiModel.getPlatform());
                if (StringUtils.isNotBlank(convMsgDto.getAudioUuid())) {
                    // 填充音频访问 URL，方便前端直接使用
                    convMsgDto.setAudioUrl(fileService.getUrl(convMsgDto.getAudioUuid()));
                } else {
                    convMsgDto.setAudioUrl("");
                }
            }
            item.setChildren(children);
        });
        return result;
    }

    /**
     * 创建默认对话。
     *
     * @param userId 用户 ID
     * @return 新增记录数
     */
    public int createDefault(Long userId) {
        Conversation conversation = new Conversation();
        conversation.setUuid(UuidUtil.createShort());
        conversation.setUserId(userId);
        conversation.setTitle(AdiConstant.ConversationConstant.DEFAULT_NAME);
        return baseMapper.insert(conversation);
    }

    /**
     * 根据第一条消息创建对话。
     *
     * @param userId 用户 ID
     * @param uuid   对话 UUID
     * @param title  标题
     * @return 对话实体
     */
    public Conversation createByFirstMessage(Long userId, String uuid, String title) {
        Conversation conversation = new Conversation();
        conversation.setUuid(uuid);
        conversation.setUserId(userId);
        // 标题过长会影响列表展示与检索性能，因此在写入前做截断
        conversation.setTitle(StringUtils.substring(title, 0, 45));
        baseMapper.insert(conversation);

        return this.lambdaQuery().eq(Conversation::getUuid, uuid).oneOpt().orElse(null);
    }

    /**
     * 新增对话。
     *
     * @param convAddReq 新增请求
     * @return 对话 DTO
     */
    public ConvDto add(ConvAddReq convAddReq) {
        Conversation conversation = this.lambdaQuery()
                .eq(Conversation::getUserId, ThreadContext.getCurrentUserId())
                .eq(Conversation::getTitle, convAddReq.getTitle())
                .eq(Conversation::getIsDeleted, false)
                .one();
        if (null != conversation) {
            throw new BaseException(A_CONVERSATION_TITLE_EXIST);
        }

        List<Long> filteredMcpIds = filterEnableMcpIds(convAddReq.getMcpIds());
        List<Long> filteredKbIds = filterEnableKbIds(ThreadContext.getCurrentUser(), convAddReq.getKbIds());

        String uuid = UuidUtil.createShort();
        Conversation one = new Conversation();
        BeanUtils.copyProperties(convAddReq, one);
        one.setUuid(uuid);
        one.setUserId(ThreadContext.getCurrentUserId());
        one.setMcpIds(StringUtils.join(filteredMcpIds, ","));
        one.setKbIds(StringUtils.join(filteredKbIds, ","));
        if (null != convAddReq.getAudioConfig()) {
            one.setAudioConfig(convAddReq.getAudioConfig());
        }
        baseMapper.insert(one);

        Conversation conv = this.lambdaQuery().eq(Conversation::getUuid, uuid).one();
        ConvDto dto = MPPageUtil.convertTo(conv, ConvDto.class);
        setMcpToDto(conv, dto);
        setKbInfoToDto(conv, dto);
        return dto;
    }

    /**
     * 组装 MCP 信息。
     *
     * @param conversation 对话信息
     * @param dto          对话 DTO
     */
    private void setMcpToDto(Conversation conversation, ConvDto dto) {
        if (StringUtils.isNotBlank(conversation.getMcpIds())) {
            dto.setMcpIds(Arrays.stream(conversation.getMcpIds().split(","))
                    .map(Long::parseLong)
                    .toList());
        } else {
            dto.setMcpIds(new ArrayList<>());
        }
    }

    /**
     * 组装已关联的知识库信息。
     *
     * @param conv 对话信息
     * @param dto  对话 DTO
     */
    private void setKbInfoToDto(Conversation conv, ConvDto dto) {
        //组装已关联的知识库信息
        List<Long> kids = new ArrayList<>();
        List<ConvKnowledge> convKnowledgeList = new ArrayList<>();
        if (StringUtils.isNotBlank(conv.getKbIds())) {
            List<Long> kbIds = Arrays.stream(conv.getKbIds().split(","))
                    .map(Long::parseLong)
                    .toList();
            knowledgeBaseService.listByIds(kbIds).forEach(kb -> {
                ConvKnowledge convKnowledge = convertToConvKbDto(ThreadContext.getCurrentUser(), kb);
                // 如果不是本人且不是公开知识库，则标记不可用
                if (!convKnowledge.getIsMine() && !convKnowledge.getIsPublic()) {
                    convKnowledge.setKbInfo(null);
                    convKnowledge.setIsEnable(false);
                }
                convKnowledgeList.add(convKnowledge);
                kids.add(kb.getId());
            });
        }
        dto.setKbIds(kids);
        dto.setConvKnowledgeList(convKnowledgeList);
    }

    /**
     * 根据预设会话创建当前用户会话。
     *
     * @param presetConvUuid 预设会话 UUID
     * @return 对话 DTO
     */
    public ConvDto addByPresetConv(String presetConvUuid) {
        ConversationPreset presetConv = this.conversationPresetService.lambdaQuery()
                .eq(ConversationPreset::getUuid, presetConvUuid)
                .eq(ConversationPreset::getIsDeleted, false)
                .oneOpt()
                .orElseThrow(() -> new BaseException(A_PRESET_CONVERSATION_NOT_EXIST));
        ConversationPresetRel presetRel = this.conversationPresetRelService.lambdaQuery()
                .eq(ConversationPresetRel::getUserId, ThreadContext.getCurrentUserId())
                .eq(ConversationPresetRel::getPresetConvId, presetConv.getId())
                .eq(ConversationPresetRel::getIsDeleted, false)
                .oneOpt()
                .orElse(null);
        if (null != presetRel) {
            Conversation conv = this.getById(presetRel.getUserConvId());
            return MPPageUtil.convertTo(conv, ConvDto.class);
        }
        ConvAddReq convAddReq = ConvAddReq.builder()
                .title(presetConv.getTitle())
                .remark(presetConv.getRemark())
                .aiSystemMessage(presetConv.getAiSystemMessage())
                .build();
        ConvDto convDto = self.add(convAddReq);
        conversationPresetRelService.save(
                ConversationPresetRel.builder()
                        .presetConvId(presetConv.getId())
                        .userConvId(convDto.getId())
                        .userId(ThreadContext.getCurrentUserId())
                        .build()
        );
        return convDto;
    }

    /**
     * 编辑对话。
     *
     * @param uuid        对话 UUID
     * @param convEditReq 编辑请求
     * @return 是否更新成功
     */
    public boolean edit(String uuid, ConvEditReq convEditReq) {
        Conversation conversation = getOrThrow(uuid);
        Conversation one = new Conversation();
        BeanUtils.copyProperties(convEditReq, one);
        one.setId(conversation.getId());
        if (null != convEditReq.getUnderstandContextEnable()) {
            one.setUnderstandContextEnable(convEditReq.getUnderstandContextEnable());
        }
        if (null != convEditReq.getMcpIds()) {
            List<Long> filteredMcpIds = filterEnableMcpIds(convEditReq.getMcpIds());
            if (filteredMcpIds.isEmpty()) {
                // 过滤后为空时显式清空，避免保留无效配置
                one.setMcpIds(StringUtils.join(filteredMcpIds, ","));
            }
        }
        if (null != convEditReq.getKbIds()) {
            if (convEditReq.getKbIds().isEmpty()) {
                one.setKbIds("");
            } else {
                List<Long> filteredKbIds = filterEnableKbIds(ThreadContext.getCurrentUser(), convEditReq.getKbIds());
                one.setKbIds(StringUtils.join(filteredKbIds, ","));
            }
        }
        if (null != convEditReq.getAudioConfig()) {
            one.setAudioConfig(convEditReq.getAudioConfig());
        }
        return baseMapper.updateById(one) > 0;
    }

    /**
     * 软删除对话。
     *
     * @param uuid 对话 UUID
     * @return 是否删除成功
     */
    @Transactional
    public boolean softDel(String uuid) {
        Conversation conversation = getOrThrow(uuid);
        conversationPresetRelService.softDelBy(conversation.getUserId(), conversation.getId());
        return this.lambdaUpdate()
                .eq(Conversation::getId, conversation.getId())
                .set(Conversation::getIsDeleted, true)
                .update();
    }

    /**
     * 统计当天创建的对话数量。
     *
     * @return 数量
     */
    public int countTodayCreated() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime beginTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0);
        LocalDateTime endTime = beginTime.plusDays(1);
        return baseMapper.countCreatedByTimePeriod(beginTime, endTime);
    }

    /**
     * 统计全部对话数量。
     *
     * @return 数量
     */
    public int countAllCreated() {
        return baseMapper.countAllCreated();
    }

    /**
     * 按 UUID 获取对话，不存在或无权限则抛异常。
     *
     * @param uuid 对话 UUID
     * @return 对话实体
     */
    private Conversation getOrThrow(String uuid) {
        Conversation conversation = this.lambdaQuery()
                .eq(Conversation::getUuid, uuid)
                .eq(Conversation::getIsDeleted, false)
                .one();
        if (null == conversation) {
            throw new BaseException(A_CONVERSATION_NOT_EXIST);
        }
        if (!conversation.getUserId().equals(ThreadContext.getCurrentUserId()) && !ThreadContext.getCurrentUser().getIsAdmin()) {
            throw new BaseException(A_USER_NOT_AUTH);
        }
        return conversation;
    }

    /**
     * 过滤出有效的 MCP 服务 ID 列表。
     *
     * @param mcpIdsInReq 请求中传入的 MCP 服务 ID 列表
     * @return 有效的 MCP 服务 ID 列表
     */
    private List<Long> filterEnableMcpIds(List<Long> mcpIdsInReq) {
        List<Long> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(mcpIdsInReq)) {
            return result;
        }
        List<UserMcp> userMcpList = userMcpService.searchEnableByUserId(ThreadContext.getCurrentUserId());

        for (Long mcpIdInReq : mcpIdsInReq) {
            if (userMcpList.stream().anyMatch(item -> item.getMcpId().equals(mcpIdInReq))) {
                result.add(mcpIdInReq);
            } else {
                // 无权限或已禁用时仅记录日志，避免阻断整体创建流程
                log.warn("User mcp id {} not found or disabled in user mcp list, userId: {}, mcpId:{}", mcpIdInReq, ThreadContext.getCurrentUserId(), mcpIdInReq);
            }
        }
        return result;
    }

    /**
     * 过滤出有效的知识库 ID 列表。
     *
     * @param user      当前用户
     * @param kbIdsInReq 请求中的知识库 ID 列表
     * @return 有效知识库 ID 列表
     */
    public List<Long> filterEnableKbIds(User user, List<Long> kbIdsInReq) {
        if (CollectionUtils.isEmpty(kbIdsInReq)) {
            return Collections.emptyList();
        }
        List<KbInfoResp> validKbList = filterEnableKb(user, kbIdsInReq);
        return validKbList.stream().map(KbInfoResp::getId).toList();
    }

    /**
     * 过滤出有效的知识库列表。
     * 如果知识库是别人的且不是公开的，则不属于有效的可以关联的知识库
     *
     * @param user 当前用户
     * @param ids  知识库 ID 列表
     * @return 有效的知识库列表
     */
    public List<KbInfoResp> filterEnableKb(User user, List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        // 仅保留公开或当前用户拥有的知识库，避免越权关联
        return knowledgeBaseService.listByIds(ids).stream()
                .filter(item -> item.getIsPublic() || user.getUuid().equals(item.getOwnerUuid()))
                .toList();
    }

    /**
     * 将知识库信息转换为对话关联对象。
     *
     * @param user   当前用户
     * @param kbInfo 知识库信息
     * @return 对话知识库信息
     */
    private ConvKnowledge convertToConvKbDto(User user, KbInfoResp kbInfo) {
        ConvKnowledge result = new ConvKnowledge();
        BeanUtils.copyProperties(kbInfo, result);
        result.setKbInfo(kbInfo);
        result.setIsMine(user.getUuid().equals(kbInfo.getOwnerUuid()));
        return result;
    }
}
